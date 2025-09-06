name := "adt-query-dsl"

version := "1.0"
scalaVersion := "2.13.16"

//"1.7.2"
val schemaV = "1.7.4"

//export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home
lazy val javaVersion = sys.props("java.specification.version")
//https://repo1.maven.org/maven2/com/lihaoyi/
lazy val AmmoniteVersion = "3.0.2"

//show scalacOptions
Compile / scalacOptions ++= Seq(
  //https://github.com/scala/scala/releases/tag/v2.13.16
  //Under -Xsource:3, allow importing given, for cross-building
  "-Xsource:3",
  s"-target:${javaVersion}",
  s"-release:${javaVersion}",
  "-Ylog-classpath",  //
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlog-reflective-calls",
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Xlint",
  "-Wconf:cat=other-match-analysis:error", //Transform exhaustivity warnings into errors.
  "-Wconf:msg=lambda-parens:s",
  "-Xmigration", //Emit migration warnings under -Xsource:3 as fatal warnings, not errors; -Xmigration disables fatality (Demote the errors to warnings)
  "-Xfatal-warnings",
)

// show javacOptions
javacOptions ++= Seq("-source", javaVersion, "-target", javaVersion)
javaHome := Some(file(s"/Library/Java/JavaVirtualMachines/jdk-${javaVersion}.jdk/Contents/Home/"))


libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "org.scala-lang"  %  "scala-reflect" % scalaVersion.value,

  "dev.zio" %% "zio-schema" % schemaV,
  "dev.zio" %% "zio-schema-derivation" % schemaV,
  "dev.zio" %% "zio-schema-json" % schemaV,

  //https://zio.dev/zio-prelude/
  //"dev.zio" %% "zio-prelude" % "1.0.0-RC21",
  
  "com.lihaoyi" % "ammonite" % AmmoniteVersion % "test" cross CrossVersion.full
)

semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalafmtOnCompile := true

//zio.elasticsearch
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")


addCommandAlias("c", "compile;fix;fmt")
addCommandAlias("r", "reload")

//test:run
Test / sourceGenerators += Def.task {
  val file = (Test / sourceManaged).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue


//run / fork := false //true