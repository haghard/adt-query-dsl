package com.example

abstract class `≠`[A, B] extends Serializable

object `≠` {
  implicit def nsub[A, B]: A `≠` B = new `≠`[A, B] {}

  @scala.annotation.implicitAmbiguous("Schema constraint violation: Provide schema of a term of ${A} type")
  implicit def nsubAmbig1[A, B >: A]: A `≠` B = sys.error("Unexpected call")

  implicit def nsubAmbig2[A, B >: A]: A `≠` B = sys.error("Unexpected call")
}
