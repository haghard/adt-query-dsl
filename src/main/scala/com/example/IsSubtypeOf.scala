package com.example

import scala.annotation.implicitNotFound

@implicitNotFound("${A} is not a subtype of ${B}")
trait Is[-A, +B] extends (A => B)
object Is {
  implicit def refl[A]: Is[A, A] = (v1: A) => v1
}

@implicitNotFound("\nThis operator requires ${In} to be a subtype of ${Out}")
trait IsSubtypeOf[-In, +Out] {
  def narrow(in: In): Out // narrow a subtype to supertype
}

object IsSubtypeOf {
  implicit def mk[A]: IsSubtypeOf[A, A] = (in: A) => in
}

//Type constraint for type inequality in scala [duplicate]
abstract class <:!<[A, B] extends Serializable
object <:!< {
  implicit def nsub[A, B]: A <:!< B = new <:!<[A, B] {}

  @scala.annotation.implicitAmbiguous("Cannot add ${B} to ${A} !")
  implicit def nsubAmbig1[A, B >: A]: A <:!< B = sys.error("Unexpected call")
  implicit def nsubAmbig2[A, B >: A]: A <:!< B = sys.error("Unexpected call")
}
