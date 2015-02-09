Common.serviceSettings("api")

// Add here the specific settings for this module


libraryDependencies ++= Common.commonDependencies ++: Seq(
	jdbc,
  anorm,
  "com.amazonaws" % "aws-java-sdk" % "1.3.11"
)
