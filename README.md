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