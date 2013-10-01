import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "dolorian"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.webjars" %% "webjars-play" % "2.1.0-2",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.webjars" % "bootstrap-timepicker" % "0.2.3",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "com.lowagie" % "itext" % "2.1.7",
    "net.sf.jodreports" % "jodreports" % "2.4.0",
    "com.amazonaws" % "aws-java-sdk" % "1.3.11",
    "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test"

 )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    testOptions in Test := Nil
  )

}
