import sbt._
import Keys._
import play.PlayImport._
import com.typesafe.sbt.web.SbtWeb.autoImport.{Assets, pipelineStages}
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.rjs.Import.{rjs, RjsKeys}
import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.gzip.Import.gzip

object Common {
	def appName = "toolbelt"
	
	// Common settings for every project
	def settings (theName: String) = Seq(
		name := theName,
		version := "1.0-SNAPSHOT",
		scalaVersion := "2.11.1",
		doc in Compile <<= target.map(_ / "none"),
    scalacOptions ++= Seq("-unchecked", "-deprecation","-feature", "-language:postfixOps")
	)
	// Settings for the app, i.e. the root project
	val appSettings = settings(appName)
	// Settings for every module, i.e. for every subproject
	def moduleSettings (module: String) = settings(module) ++: Seq(
		javaOptions in Test += s"-Dconfig.resource=application.conf"
	)
	// Settings for every service, i.e. for admin and web subprojects
	def serviceSettings (module: String) = moduleSettings(module) ++: Seq(
		includeFilter in (Assets, LessKeys.less) := "*.less",
		excludeFilter in (Assets, LessKeys.less) := "_*.less",
		pipelineStages := Seq(rjs, digest, gzip),
		RjsKeys.mainModule := s"main-$module"
	)
	
	val commonDependencies = Seq("org.scalatest" % "scalatest_2.11" % "2.2.4" % "test")
}