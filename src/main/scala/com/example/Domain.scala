package com.example

import zio.schema.{DeriveSchema, Schema}

final case class User(id: Long, name: String, age: Int)

object User {
  val schema: Schema[User] = DeriveSchema.gen[User]

  val (
    id: PathSelector[User, Long] @unchecked,
    name: PathSelector[User, String] @unchecked,
    age: PathSelector[User, Int] @unchecked
  ) =
    schema.makeAccessors(BuilderSelector): @unchecked
}

final case class Session(id: Long, user: User, token: String)

object Session {
  val schema: Schema[Session] = DeriveSchema.gen[Session]

  val (
    id: PathSelector[Session, Long] @unchecked,
    user: PathSelector[Session, User] @unchecked,
    token: PathSelector[Session, User] @unchecked
  ) =
    schema.makeAccessors(BuilderSelector): @unchecked
}

sealed trait PaymentMethod

object PaymentMethod {

  // Schema[PaymentMethod]
  implicit val schema: Schema.Enum3[CreditCard, WireTransfer, ACH, PaymentMethod] =
    DeriveSchema.gen[PaymentMethod]

  final case class CreditCard(number: String, expirationMonth: Int, expirationYear: Int) extends PaymentMethod
  object CreditCard {
    implicit val schema: Schema[CreditCard] = DeriveSchema.gen[CreditCard]
    val (
      number: PathSelector[CreditCard, String] @unchecked,
      expirationMonth: PathSelector[CreditCard, Int] @unchecked,
      expirationYear: PathSelector[CreditCard, Int] @unchecked
    ) = schema.makeAccessors(BuilderSelector): @unchecked

    val DTOR = Discriminator[PaymentMethod, PaymentMethod.CreditCard](PaymentMethod.schema, CreditCard.schema)
  }

  final case class WireTransfer(accountNumber: String, bankCode: Option[String]) extends PaymentMethod
  object WireTransfer {
    implicit val schema: Schema[WireTransfer] = DeriveSchema.gen[WireTransfer]
    val (
      accountNumber: PathSelector[WireTransfer, String] @unchecked,
      bankCode: PathSelector[WireTransfer, Option[String]] @unchecked
    ) = schema.makeAccessors(BuilderSelector): @unchecked

    val DTOR = Discriminator[PaymentMethod, PaymentMethod.WireTransfer](PaymentMethod.schema, WireTransfer.schema)
  }

  final case class ACH(accountNumber: String, bankCode: String) extends PaymentMethod
  object ACH {
    implicit val schema: Schema[ACH] = DeriveSchema.gen[ACH]
    val (
      accountNumber: PathSelector[ACH, String] @unchecked,
      bankCode: PathSelector[ACH, String] @unchecked
    ) = schema.makeAccessors(BuilderSelector): @unchecked

    val DTOR = Discriminator[PaymentMethod, PaymentMethod.ACH](PaymentMethod.schema, ACH.schema)
  }

}

final case class Profile(paymentMethod: PaymentMethod, snn: String)
object Profile {
  val schema: Schema[Profile] = DeriveSchema.gen[Profile]

  val (
    paymentMethod: PathSelector[Profile, PaymentMethod] @unchecked,
    snn: PathSelector[Profile, String] @unchecked
  ) = schema.makeAccessors(BuilderSelector): @unchecked
}

final case class BankUser(name: String, info: Profile)

object BankUser {

  val schema: Schema[BankUser] = DeriveSchema.gen[BankUser]

  val (
    name: PathSelector[BankUser, String] @unchecked,
    profile: PathSelector[BankUser, Profile] @unchecked
  ) = schema.makeAccessors(BuilderSelector): @unchecked

}
