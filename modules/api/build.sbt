Common.serviceSettings("api")

// Add here the specific settings for this module


libraryDependencies ++= Common.commonDependencies ++: Seq(
	jdbc,
  anorm,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11"
)
