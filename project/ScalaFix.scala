import sbt.*
import sbt.Keys.{scalaBinaryVersion, semanticdbEnabled, semanticdbVersion}
import scalafix.sbt.ScalafixPlugin

//ScalaFix
//https://github.com/spotify/scio/blob/cec4e12b0ef28321e44c77575585db2869d52659/scalafix/rules/src/main/scala/fix/v0_8_0/FixTensorflow.scala#L7

// Extra scalafix configuration and dependencies
object ScalaFix extends AutoPlugin {

  override def requires = ScalafixPlugin
  override def trigger  = allRequirements

  import ScalafixPlugin.autoImport._

  override lazy val projectSettings = Seq(
    semanticdbEnabled := true,                        // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    ThisBuild / scalafixDependencies ++= Seq(
      "org.scala-lang" %% "scala-rewrites" % "0.1.5"
    ),
    ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0",
    ThisBuild / scalafixScalaBinaryVersion                     := scalaBinaryVersion.value
  )
}
