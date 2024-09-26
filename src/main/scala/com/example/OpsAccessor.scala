package com.example

import zio.schema.Schema

trait OpsAccessor {
  type Columns
  val columns: Columns
}

object OpsAccessor {

  type Lens[F, S, A]   = OpsAccessorBuilder#Lens[F, S, A]
  type Prism[F, S, A]  = OpsAccessorBuilder#Prism[F, S, A]
  type Traversal[S, A] = OpsAccessorBuilder#Traversal[S, A]

  type Aux[Columns0] = OpsAccessor {
    type Columns = Columns0
  }

  def apply[A](implicit S: Schema[A]): Aux[S.Accessors[Lens, Prism, Traversal]] =
    new OpsAccessor {
      val accessorBuilder: OpsAccessorBuilder =
        S match {
          case record: Schema.Record[A] =>
            // new FieldAccessorBuilder()
            new OpsAccessorBuilder(record.fields.toList)
          case _ =>
            throw new Exception("Schema.Record expected!")
        }

      override type Columns =
        S.Accessors[accessorBuilder.Lens, accessorBuilder.Prism, accessorBuilder.Traversal]

      override val columns: Columns =
        S.makeAccessors(accessorBuilder)
    }
}
