import sbt.Keys._
import sbt._

object Common {

  val settings: Seq[Setting[_]] = Seq(
    //version := "1.2.3-SNAPSHOT",
    scalaVersion := "2.11.5",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:postfixOps", "-language:reflectiveCalls"," -language:implicitConversions")
  )
}