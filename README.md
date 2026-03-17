### Query dsl for ADTs 

#### Example
```scala

  val s = Session(30, User(42, "jack", 34), "token")

  val selector = Session.user / User.name

  val r = selector.endsWith("ck").apply(s)
  println(s"$r")

  selector =:= "jack" apply s
  selector =:= "jack" apply s

  ((Session.user / User.age) =:= 10).apply(s)
  
  ((Session.user / User.age) >> 10).apply(s)
  
  val age = Session.user / User.age
  (age >> 18).and(age << 30).apply(s)


  val (a, b, optInt) = PathAccessor[Row].columns
  optInt.nonEmpty.and(optInt.>>?(6)).apply(Row("aaaaa", 3, Some(7)))
  a.%("aaa").or((b >> 0).and(b << 3)).apply(Row("aaaaa", 3, None))

  val (id, usr, _) = OpsAccessor[Session].columns
  (usr / User.age).>>(18).and((usr / User.name).%("ja")).apply(s)
  
  
  val pmAch = PaymentMethod.ACH("111", bankCode = "aa")
  val alice = BankUser("alice", Profile(pmAch, "123"))

  val pmCC = PaymentMethod.CreditCard("123456", 1, 1)
  val bob  = BankUser("bob", Profile(pmCC, "321"))

  val bankCode = (BankUser.profile / Profile.paymentMethod)./?(PaymentMethod.ACH.schema)./(PaymentMethod.ACH.bankCode)
  (bankCode =:= "aa").apply(alice)

  val ccNumber =
    (BankUser.profile./(Profile.paymentMethod)./?(PaymentMethod.CreditCard.schema)) / PaymentMethod.CreditCard.number

  ccNumber.startsWith("123").apply(bob)
  
  val dv  = PaymentMethod.ACH.schema.toDynamic(pmAch)
  val dv0 = PaymentMethod.ACH.DTOR.apply(dv)
  val dv1 = PaymentMethod.ACH.DTOR.fromValue(pmAch)


```



--------------------------------
TRY THIS: https://github.com/vasiliybondarenko/simple-validator/blob/main/src/test/scala/example/validator/MacrosValidatorTest.scala
--------------------------------


https://youtu.be/Rz6q4jc2yTE?list=PLvdARMfvom9DCDLfWzukYMiY9nY4nrNDz
https://github.com/skmuiruri/type-safe-decoders-without-boilerplate/blob/main/src/main/scala/com/skm/parsers/usingdynamicvalue/SimpleParser.scala

Zymposium - ZIO Schema: https://youtu.be/UqQj1i3mDjo

Zymposium - Distributed Actors with ZIO HTTP: https://youtu.be/5uCDCVcrci8?t=1856
Zymposium - The Next Chapter Of Distributed Actors: https://youtu.be/v8jmAZXK0Rk


https://github.com/search?q=def+makePrism+language%3AScala&type=code
https://github.com/thinkharderdev/zio-cache/blob/d6ebbdf75224448174c35fe29096046b9ac5fb63/zio-cache/shared/src/main/scala/zio/cache/Query.scala#L64


kuzminki-ec
https://github.com/karimagnusson/kuzminki-ec/blob/main/kuzminki-ec/src/main/scala/filter/TypeMethods.scala
https://github.com/karimagnusson/kuzminki-ec/blob/main/kuzminki-ec/src/main/scala/filter/JsonbMethods.scala

Magnolia
https://blog.michal.pawlik.dev/posts/scala/scala-derivations-show/
https://blog.michal.pawlik.dev/posts/scala/scala-derivations/


Zio schema
https://youtu.be/Ic7q9Bjg0oI?list=PLvdARMfvom9DCDLfWzukYMiY9nY4nrNDz
https://github.com/sviezypan/fs_2023_talk/blob/main/src/main/scala/fstalk/InsertExample.scala#L92


https://github.com/sviezypan/fs_2023_talk/blob/main/src/main/scala/fstalk/ClientProjectExamples.scala


ZIO Apache Parquet
https://github.com/grouzen/zio-apache-parquet
https://mnedokushev.me/2024/09/05/unpacking-zio-schema-accessors.html


TODO:
Generate 
1) kenavro (named typles)
2) kyo-dat Recorda

