package com.example

import zio.schema.Schema

trait PathAccessor {
  type Columns
  val columns: Columns
}

object PathAccessor {

  type Lens[F, S, A]   = PathSelectorBuilder.Lens[F, S, A]
  type Prism[F, S, A]  = PathSelectorBuilder.Prism[F, S, A]
  type Traversal[S, A] = PathSelectorBuilder.Traversal[S, A]

  type Aux[Columns0] = PathAccessor {
    type Columns = Columns0
  }

  def apply[A](implicit S: Schema[A]): PathAccessor.Aux[S.Accessors[Lens, Prism, Traversal]] =
    new PathAccessor {
      val accessorBuilder = PathSelectorBuilder

      override type Columns =
        S.Accessors[accessorBuilder.Lens, accessorBuilder.Prism, accessorBuilder.Traversal]

      override val columns: Columns =
        S.makeAccessors(accessorBuilder)
    }
}
