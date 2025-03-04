package com.example

import org.slf4j.LoggerFactory
import zio.schema.codec.JsonCodec
import zio.schema.{DynamicValue, Schema, TypeId}

object Demo extends App {

  val logger = LoggerFactory.getLogger(getClass)

  val s = Session(30, User(42, "jack", 28), "token")

  val selector = Session.user / User.name

  val r = selector.endsWith("ck").apply(s)
  println(s"$r")

  selector =:= "jack" apply s
  selector =:= "jack" apply s

  val r0 = ((Session.user / User.age) =:= 10).apply(s)
  logger.warn(s"$r0")

  val r1 = ((Session.user / User.age) >> 10).apply(s)
  logger.warn(s"$r1")

  val age = Session.user / User.age
  val r2  = (age >> 18).and(age << 30).apply(s)
  logger.warn(s"**$r2")

  val pmAch = PaymentMethod.ACH("111", bankCode = "aa")
  val alice = BankUser("alice", Profile(pmAch, "123"))

  val pmCC = PaymentMethod.CreditCard("123456", 1, 1)
  val bob  = BankUser("bob", Profile(pmCC, "321"))

  val bankCode =
    (BankUser.profile / Profile.paymentMethod)./?(PaymentMethod.ACH.schema)./(PaymentMethod.ACH.bankCode)
  (bankCode =:= "aa").apply(alice)

  val ccNumber =
    (BankUser.profile./(Profile.paymentMethod)./?(PaymentMethod.CreditCard.schema)) / PaymentMethod.CreditCard.number

  (ccNumber startsWith "123").apply(bob)
  (ccNumber % "123").apply(bob)

  val Codec = JsonCodec.schemaBasedBinaryCodec(Schema.dynamicValue)

  val dv: DynamicValue        = PaymentMethod.ACH.schema.toDynamic(pmAch)
  val bts                     = Codec.encode(dv)
  val dvFromBts: DynamicValue = Codec.decode(bts).getOrElse(throw new Exception("Boom !!!"))

  val recoveredPm =
    dvFromBts match {
      case DynamicValue.Record(recId, _) =>
        val pmName: String = classOf[PaymentMethod].getSimpleName()
        recId match {
          case TypeId.Nominal(_, IndexedSeq(`pmName`), termName) =>
            PaymentMethod.fromDynamicValue(termName, dvFromBts)
          case other =>
            Left(s"Expected Nominal($pmName) but found $other")
        }
      case other =>
        Left(s"Expected DynamicValue.Record but found $other")
    }

  val recovered: PaymentMethod.ACH = PaymentMethod.ACH.DTOR.apply(dv).getOrElse(???)
  val recovered1                   = PaymentMethod.ACH.DTOR.fromValue(pmAch)

  PaymentMethod.ACH.DTOR.fromValue(pmCC) // "Deconstruction error
  val dv1 = PaymentMethod.CreditCard.schema.toDynamic(pmCC)
  PaymentMethod.CreditCard.DTOR.apply(dv1)

  val (a, b, optC) = PathAccessor[Row].columns

  optC.nonEmpty.and(optC.>>?(6)).apply(Row("aaaaa", 3, Some(7)))
  a.%("aaa").or((b >> 0).and(b << 3)).apply(Row("aaaaa", 3, None))
  optC.>>?(2).apply(Row("aaaaa", 3, Some(3))) // true

  val (id, usr, _) = OpsAccessor[Session].columns
  (usr / User.age).>>(18).and((usr / User.name).%("ja")).apply(s)

}
