Common.appSettings

lazy val api = (project in file("modules/api")).enablePlugins(PlayScala)

lazy val main = (project in file(".")).enablePlugins(PlayScala).aggregate(api).dependsOn(api)



libraryDependencies ++= Common.commonDependencies
