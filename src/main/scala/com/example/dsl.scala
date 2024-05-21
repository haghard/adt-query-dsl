package com.example

import org.slf4j.LoggerFactory
import zio.schema.{DynamicValue, Schema, TypeId}

import scala.annotation.{nowarn, tailrec}

final case class Discriminator[S, A](sum: Schema.Enum[S], termOfSum: Schema[A]) {

  def fromValue(v: S): Either[String, A] =
    termOfSum match {
      case term: Schema.Record[A] =>
        sum.caseOf(term.id.name) match {
          case Some(rec) =>
            // scala.util.Try(rec0.deconstruct(v))
            rec
              .deconstructOption(v)
              .map {
                case v: A @nowarn => Right(v)
                case o            => Left("Found " + o.getClass().getName())
              }
              .getOrElse(Left("Deconstruction error"))
          // Right(s.deconstruct(v).asInstanceOf[A])
          case None =>
            Left(s"${termOfSum} is not a term of ${sum}")
        }
      case _ =>
        Left("Unexpected")
    }

  def apply(dv: DynamicValue): Either[String, A] =
    termOfSum match {
      case term: Schema.Record[_] =>
        // term.fromDynamic(dv).map(Some(_))
        sum.caseOf(term.id.name) match {
          case Some(_) =>
            term.fromDynamic(dv)
          case None =>
            Left(s"${termOfSum} is not a term of ${sum}")
        }
      case _ =>
        Left("Unexpected")
    }
}

final case class PathSelector[S, A](
  schema: Schema.Record[S],
  path: List[String],
  sums: Map[String, TypeId] = Map.empty
) {
  self =>

  private val logger = LoggerFactory.getLogger(self.getClass())

  // def asCaseOf[A1 <: A: ClassTag]: Predicate[S, A1] = Predicate.CaseOf[S, A, A1](self, schema, implicitly[ClassTag[A1]])

  // def isCaseOf[A1 <: A: ClassTag]: Predicate[S, A1] = Predicate.CaseOf[S, A, A1](self, implicitly[ClassTag[A1]])

  // def isCaseOf[A1: ClassTag](implicit ev: A1 IsSubtypeOf /*<:<*/ A): Predicate[S, A1] = Predicate.CaseOf[S, A, A1](self, implicitly[ClassTag[A1]], pairs)

  // def /?[A1](that: Discriminator2[A, A1]): Selector[S, A, A1] = discriminate(that)

  // def /??[A1](term: Schema[A1]): Predicate[S, A1] = Predicate.Discriminator[S, A, A1](self, term)

  // def should[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  // def /?[A1 <: A: Schema](sumTerm: Schema[A1])(implicit ev: A `≠` A1): PathSelector[S, A1] = {

  def /?[A1](
    sumTerm: Schema[A1]
  )(implicit @nowarn ev: A `≠` A1, @nowarn ev1: A1 IsSubtypeOf A): PathSelector[S, A1] = {
    val typeId: TypeId =
      sumTerm match {
        case record: Schema.Record[_] => record.id
        case enum: Schema.Enum[_]     => enum.id
        case other                    => throw new IllegalArgumentException(s"Unexpected argument $other")
      }
    // TODO: improve it
    self.copy(sums = self.sums + (self.path.last -> typeId))
  }

  // def discriminate[A1](d: Discriminator[A, A1]): Selector[S, A, A1] = Selector[S, A, A1](self, d)
  // def discriminate[A1](d: Discriminator2[A, A1]): Selector[S, A, A1] = Selector[S, A, A1](self, d)

  def =:=(that: A)(implicit ord: Ordering[A]): Predicate[S, A] =
    Predicate.Eq(self, that, ord)

  // private def widen[A1](implicit ev: A <:< A1): Selector[S, A1] = self.asInstanceOf[Selector[S, A1]]

  private def coerce[A1](implicit ev: A =:= A1): PathSelector[S, A1] =
    self.asInstanceOf[PathSelector[S, A1]]

  def endsWith(that: A)(implicit ev: A =:= String): Predicate[S, A] =
    Predicate.EndsWith(self.coerce, ev(that))

  def startsWith(that: A)(implicit ev: A =:= String): Predicate[S, A] =
    %(that)

  def %(that: A)(implicit ev: A =:= String): Predicate[S, A] =
    Predicate.StartsWith(self.coerce, ev(that))

  def >>(that: A)(implicit num: Numeric[A]): Predicate[S, A] =
    Predicate.GreaterThan(self, that, num)

  def <<(that: A)(implicit num: Numeric[A]): Predicate[S, A] =
    Predicate.LessThan(self, that, num)

  def /[A1](that: PathSelector[A, A1]): PathSelector[S, A1] =
    self.copy(path = self.path ++ that.path)

  def apply(v: S): Either[String, A] = {
    @tailrec
    def go(rec: DynamicValue.Record, paths: List[String], visited: Vector[String]): Either[String, A] =
      paths match {
        case segment :: rest =>
          if (rec.values.contains(segment)) {
            rec.values(segment) match {
              case enum: DynamicValue.Enumeration =>
                val (eName, dv) = enum.value
                dv match {
                  case rec0: DynamicValue.Record =>
                    val expTypeId = sums(segment)
                    if (expTypeId == rec0.id)
                      go(rec0, rest, visited :+ segment :+ eName)
                    else
                      Left(s"Expected $expTypeId but ${rec0.id} found !")
                  case _: DynamicValue.Enumeration =>
                    ???
                  case _ =>
                    Left("Cannot select from non-record type")
                }
              case rec0: DynamicValue.Record =>
                go(rec0, rest, visited :+ segment)
              case prim: DynamicValue.Primitive[A] @unchecked =>
                logger.warn(s"read ${visited.mkString("/")}:$segment : ${prim.value}")
                Right(prim.value)
              case _ =>
                Left(s"$segment not found inside ${path.mkString(",")} !")
            }
          } else Left(s"$segment not found in ${path.mkString(",")} !!")
        case Nil =>
          Left("Empty segments!")
      }

    schema.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        go(rec, path, Vector.empty[String])
      case _ =>
        Left("Cannot select from non-record type")
    }
  }

  /*
  def applyTerm[A1](v: S, term: Schema[A1]): Either[String, A1] = {
    def go(rec: DynamicValue.Record, paths: List[String]): Either[String, A1] =
      paths match {
        case segment :: segments =>
          if (rec.values.contains(segment)) {
            rec.values(segment) match {
              case enum: DynamicValue.Enumeration =>
                term.fromDynamic(enum.value._2)
              case rec0: DynamicValue.Record =>
                go(rec0, segments)
              case _ =>
                Left(s"$segment not found inside $path !")
            }
          } else Left(s"$segment not found inside $path !")
        case Nil =>
          Left("Empty segments!")
      }

    schema.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        go(rec, path)
      case _ =>
        Left("Cannot select from non-record type")
    }
  }

  def apply1(v: S): Either[String, ClassTag[A]] =
    schema.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        rec.values(path.head) match {
          case enum: DynamicValue.Enumeration =>
            enum.value._2 match {
              case term: DynamicValue.Record =>
                val tr = term.id match {
                  case TypeId.Structural =>
                    ???
                  case TypeId.Nominal(packageName, objectNames, typeName) =>
                    val line = packageName.mkString(".") + "." + objectNames.mkString(".") + "$" + typeName
                    ClassTag.apply[A](Class.forName(line))
                }
                Right(tr)
              case _ =>
                Left("Cannot select from non-record type")
            }
          case _ =>
            Left("")
        }
      case _ =>
        Left("Cannot select from non-record type")
    }

  def apply(v: S): Either[String, A] = {
    @tailrec
    def go(rec: DynamicValue.Record, paths: List[String]): Either[String, A] =
      paths match {
        case segment :: segments =>
          if (rec.values.contains(segment)) {
            rec.values(segment) match {
              case rec0: DynamicValue.Record =>
                go(rec0, segments)
              case prim: DynamicValue.Primitive[A] @unchecked =>
                logger.warn(s"read ${path.mkString("/")} : ${prim.value}")
                Right(prim.value)
              /*case enum: DynamicValue.Enumeration =>
                enum.value._2 match {
                  case term: DynamicValue.Record =>
                    println(term.id)
                    val tr =
                      term.id match {
                        case TypeId.Structural                                  => ???
                        case TypeId.Nominal(packageName, objectNames, typeName) =>
                          // Class.forName(packageName.mkString(".")+"."+objectNames.mkString(".")+"."+typeName)
                          Class.forName(packageName.mkString(".") + "." + objectNames.mkString("."))
                      }
                    println(tr)
                    Right(???)
                  case _ =>
                    Left("Cannot select from non-record type")
                }*/
              case _ =>
                Left(s"$segment not found inside $path !")
            }
          } else Left(s"$segment not found inside $path !")
        case Nil =>
          Left("Empty segments!")
      }

    schema.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        go(rec, path)
      case _ =>
        Left("Cannot select from non-record type")
    }
  }*/
}

object BuilderSelector extends zio.schema.AccessorBuilder {
  type Lens[F, S, A]  = PathSelector[S, A]
  type Prism[F, S, A] = Unit
  /*EnumSelector*/
  /*Discriminator[S, A]*/
  type Traversal[S, A] = Unit

  override def makeLens[F, S, A](schema: Schema.Record[S], term: Schema.Field[S, A]): PathSelector[S, A] =
    PathSelector[S, A](schema, List(term.name))

  override def makePrism[F, S, A](
    sum: Schema.Enum[S],
    term: Schema.Case[S, A]
  ): Unit /*EnumSelector*/
  /*Discriminator[S, A]*/ = ()
  // Discriminator[S, A](sum, term.schema)
  // term.deconstructOption(whole)
  // EnumSelector[S, A](sum, term)

  override def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Unit = ()
}

sealed trait Predicate[S, A] {
  def apply(v: S): Either[String, Boolean]
}

object Predicate {
  final case class Eq[S, A](
    selector: PathSelector[S, A],
    that: A,
    ord: Ordering[A]
  ) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A =>
        ord.compare(r, that) == 0
      }
  }

  final case class GreaterThan[S, A](
    selector: PathSelector[S, A],
    that: A,
    num: Numeric[A]
  ) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A => num.gt(r, that) }
  }

  final case class LessThan[S, A](
    selector: PathSelector[S, A],
    that: A,
    num: Numeric[A]
  ) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A => num.lt(r, that) }
  }

  final case class EndsWith[S, A](
    selector: PathSelector[S, String],
    that: String
  ) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map(_.endsWith(that))
  }

  final case class StartsWith[S, A](
    selector: PathSelector[S, String],
    that: String
  ) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map(_.startsWith(that))
  }

  /*final case class CaseOf[S, A, A1](
    selector: PathSegment[S, A],
    ct: ClassTag[A1],
    pairs: Map[String, Schema[_]]
  ) extends Predicate[S, A1](pairs) {
    def apply(v: S): Either[String, Boolean] =
      selector.apply1(v).map(_.runtimeClass.isAssignableFrom(ct.runtimeClass))
  }*/

}

/*
final case class Discriminator[S, A](sum: Schema.Enum[S], subtype: /*Schema.Case[S, A]*/ Schema[A]) { self =>
  def apply(s: S): Either[String, Option[A]] =
    sum.toDynamic(s) match {
      case DynamicValue.Enumeration(id, (_, value)) =>
        if (id == sum.id) {
          subtype
            .fromDynamic(value)
            .map(Some(_))
        } else {
          Left(s"Cannot find " + sum.id)
        }
      case _ =>
        Left(s"Cannot cast subtype on non-enum type")
    }
}

final case class Selector[S, A, A1](s: PathSelector[S, A], d: Discriminator2[A, A1]) {
  @tailrec
  def go(rec: DynamicValue.Record, paths: List[String]): Either[String, A1] =
    paths match {
      case segment :: segments =>
        if (rec.values.contains(segment)) {
          rec.values(segment) match {
            case rec0: DynamicValue.Record =>
              go(rec0, segments)
            case enum: DynamicValue.Enumeration =>
              // if(enum.id.name == enum.id) {
              d(enum.value._2).flatMap {
                case Some(v) => Right(v)
                case None    => Left("None")
              }
            // } else Left(s"Boom !")
            case _ =>
              Left(s"$segment not found inside ${s.path} !")
          }
        } else Left(s"$segment not found inside ${s.path} !")
      case Nil =>
        Left("Empty segments!")
    }
  def apply(v: S): Either[String, A1] =
    s.schema.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        go(rec, s.path)
      case _ =>
        Left("Cannot select from non-record type")
    }
}
 */
