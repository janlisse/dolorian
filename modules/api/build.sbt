name := "api"

Common.settings

libraryDependencies ++= Seq(
	jdbc,
  anorm,
  cache,
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11"
)
