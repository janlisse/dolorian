name := "toolbelt"

Common.settings

lazy val api = (project in file("modules/api")).enablePlugins(PlayScala)

lazy val main = (project in file(".")).enablePlugins(PlayScala).dependsOn(api).aggregate(api)



