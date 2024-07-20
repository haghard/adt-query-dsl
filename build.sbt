name := "adt-query-dsl"

version := "1.0"

scalaVersion := "2.13.14"

Compile / scalacOptions ++= Seq(
  "-Xsource:3-cross",
  "-release:17",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlog-reflective-calls",
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Xlint",
  "-Wconf:cat=other-match-analysis:error", //Transform exhaustivity warnings into errors.
  "-Wconf:msg=lambda-parens:s",
  "-Xmigration" //Emit migration warnings under -Xsource:3 as fatal warnings, not errors; -Xmigration disables fatality (Demote the errors to warnings)
)

//https://repo1.maven.org/maven2/com/lihaoyi/ammonite-compiler_3.3.1/3.0.0-M2-3-b5eb4787/
val AmmoniteVersion = "3.0.0-M2-3-b5eb4787"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "org.scala-lang"  %  "scala-reflect" % scalaVersion.value,

  "dev.zio" %% "zio-schema" % "1.3.0",
  "dev.zio" %% "zio-schema-derivation" % "1.3.0",

  //https://zio.dev/zio-prelude/
  "dev.zio" %% "zio-prelude" % "1.0.0-RC21",
  
  "com.lihaoyi" % "ammonite" % AmmoniteVersion % "test" cross CrossVersion.full
)

scalafmtOnCompile := true

//zio.elasticsearch
addCommandAlias("check", "fixCheck; fmtCheck; headerCheck")
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")


addCommandAlias("c", "compile")
addCommandAlias("r", "reload")

//test:run
Test / sourceGenerators += Def.task {
  val file = (Test / sourceManaged).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue


//run / fork := false //true