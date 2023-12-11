package com.example

import org.slf4j.{Logger, LoggerFactory}

object Demo extends App {
  val logger: Logger = LoggerFactory.getLogger("app")

  val s = Session(30, User(42, "jack", 34), "token")

  val selector = Session.user / User.name
  val r        = selector =:= "jack" apply s
  logger.warn(s"$r")

  val r0 = ((Session.user / User.age) =:= 10).apply(s)
  logger.warn(s"$r0")

  val r1 = ((Session.user / User.age) >> 10).apply(s)
  logger.warn(s"$r1")

}
