name := "dolorian"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    "org.webjars" %% "webjars-play" % "2.2.0",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.webjars" % "bootstrap-timepicker" % "0.2.3",
    "org.webjars" % "requirejs" % "2.1.8",
    "org.webjars" % "angularjs" % "1.1.5-1",
    "org.postgresql" % "postgresql" % "9.4.1212",
    "net.sf.jodreports" % "jodreports" % "2.4.0",
    "com.amazonaws" % "aws-java-sdk" % "1.3.11",
    "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test",
    "com.google.inject" % "guice" % "3.0",
    "com.tzavellas" % "sse-guice" % "0.7.1"
 )
 
play.Project.playScalaSettings

org.scalastyle.sbt.ScalastylePlugin.Settings

scalacOptions ++= Seq("-unchecked", "-deprecation","-feature", "-language:postfixOps")