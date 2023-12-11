package com.example

import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import zio.schema.{DynamicValue, Schema}

final case class Selector[S, A](s: Schema[S], path: List[String]) { self =>

  private val logger = LoggerFactory.getLogger(self.getClass())

  def =:=(that: A)(implicit ord: Ordering[A]): Predicate[S, A] =
    Predicate.Eq(self, that, ord)

  def >>(that: A)(implicit num: Numeric[A]): Predicate[S, A] =
    Predicate.GreaterThan(self, that, num)

  def <<(that: A)(implicit num: Numeric[A]): Predicate[S, A] =
    Predicate.LessThan(self, that, num)

  def /[A1](that: Selector[A, A1]): Selector[S, A1] =
    self.copy(path = self.path ++ that.path)

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
                logger.warn(s"read path: ${path.mkString("/")} = ${prim.value}")
                Right(prim.value)
              case _ =>
                Left(s"$segment not found inside $path !")
            }
          } else Left(s"$segment not found inside $path !")
        case Nil =>
          Left("Empty segments!")
      }

    s.toDynamic(v) match {
      case rec: DynamicValue.Record =>
        go(rec, path)
      case _ =>
        Left("Cannot select from non-record type")
    }
  }
}

object BuilderSelector extends zio.schema.AccessorBuilder {
  type Lens[F, S, A]   = Selector[S, A]
  type Prism[F, S, A]  = Unit
  type Traversal[S, A] = Unit

  override def makeLens[F, S, A](schema: Schema.Record[S], term: Schema.Field[S, A]): Selector[S, A] =
    Selector[S, A](schema, List(term.name))

  override def makePrism[F, S, A](sum: Schema.Enum[S], term: Schema.Case[S, A]): Unit = ()

  override def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Unit = ()
}

sealed trait Predicate[S, A] {
  def apply(v: S): Either[String, Boolean]
}

object Predicate {
  final case class Eq[S, A](selector: Selector[S, A], that: A, ord: Ordering[A]) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A => ord.compare(r, that) == 0 }
  }

  final case class GreaterThan[S, A](selector: Selector[S, A], that: A, num: Numeric[A]) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A => num.gt(r, that) }
  }

  final case class LessThan[S, A](selector: Selector[S, A], that: A, num: Numeric[A]) extends Predicate[S, A] {
    def apply(v: S): Either[String, Boolean] =
      selector.apply(v).map { r: A => num.lt(r, that) }
  }
}
