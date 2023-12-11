package com.example

import zio.schema.{DeriveSchema, Schema}

final case class User(id: Long, name: String, age: Int)

object User {
  val schema: Schema[User] = DeriveSchema.gen[User]

  val (
    id: Selector[User, Long] @unchecked,
    name: Selector[User, String] @unchecked,
    age: Selector[User, Int] @unchecked
  ) =
    schema.makeAccessors(BuilderSelector): @unchecked
}

final case class Session(id: Long, user: User, token: String)

object Session {
  val schema: Schema[Session] = DeriveSchema.gen[Session]

  val (
    id: Selector[Session, Long] @unchecked,
    user: Selector[Session, User] @unchecked,
    token: Selector[Session, User] @unchecked
  ) =
    schema.makeAccessors(BuilderSelector): @unchecked
}
