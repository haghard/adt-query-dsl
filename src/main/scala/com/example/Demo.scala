package com.example

object Demo extends App {

  val s = Session(30, User(42, "jack", 34), "token")

  val selector = Session.user / User.name

  val r = selector.endsWith("ck").apply(s)
  println(s"$r")

  selector =:= "jack" apply s
  selector =:= "jack" apply s

  ((Session.user / User.age) =:= 10).apply(s)
  // logger.warn(s"$r0")

  ((Session.user / User.age) >> 10).apply(s)
  // logger.warn(s"$r1")

  val pm  = PaymentMethod.CreditCard("11", 1, 1)
  val ach = PaymentMethod.ACH("111", bankCode = "aa")

  val bu = BankUser("a", Profile.apply(ach, ""))

  // val r3 = BankUser.pm.isCaseOf[PaymentMethod.CreditCard].apply(bu)
  // val r4 = BankUser.pm.isCaseOf[PaymentMethod.ACH].apply(bu)

  // Discriminator(PaymentMethod.schema, PaymentMethod.ACH.schema)
  // println(BankUser.profile./(Profile.paymentMethod)./?(PaymentMethod.ACH.dis).apply(bu))

  val ps = (BankUser.profile / Profile.paymentMethod)./?(PaymentMethod.ACH.schema)./(PaymentMethod.ACH.bankCode)

  val ps1 =
    (BankUser.profile./(Profile.paymentMethod)./?(PaymentMethod.CreditCard.schema)) / PaymentMethod.CreditCard.number

  println((ps =:= "aa").apply(bu))
  println((ps1 =:= "aa").apply(bu))

}

/*
https://github.com/search?q=def+makePrism+language%3AScala&type=code
https://github.com/thinkharderdev/zio-cache/blob/d6ebbdf75224448174c35fe29096046b9ac5fb63/zio-cache/shared/src/main/scala/zio/cache/Query.scala#L64
 */
