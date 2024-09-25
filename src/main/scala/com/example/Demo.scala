package com.example

import org.slf4j.LoggerFactory
import zio.schema.{DynamicValue, Schema, TypeId}
import zio.schema.codec.JsonCodec

object Demo extends App {

  val logger = LoggerFactory.getLogger(getClass)

  val s = Session(30, User(42, "jack", 34), "token")

  val selector = Session.user / User.name

  val r = selector.endsWith("ck").apply(s)
  println(s"$r")

  selector =:= "jack" apply s
  selector =:= "jack" apply s

  val r0 = ((Session.user / User.age) =:= 10).apply(s)
  logger.warn(s"$r0")

  val r1 = ((Session.user / User.age) >> 10).apply(s)
  logger.warn(s"$r1")

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
}
