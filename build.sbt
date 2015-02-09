Common.appSettings


lazy val common = (project in file("modules/common")).enablePlugins(PlayScala)

lazy val api = (project in file("modules/api")).enablePlugins(PlayScala).dependsOn(common)

lazy val web = (project in file("modules/web")).enablePlugins(PlayScala).dependsOn(common)

lazy val root = (project in file(".")).enablePlugins(PlayScala).aggregate(common, api, web).dependsOn(common, api, web)


libraryDependencies ++= Common.commonDependencies
