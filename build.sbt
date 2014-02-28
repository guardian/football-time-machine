name := "football-time-machine"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.joda" % "joda-convert" % "1.2",
  "joda-time" % "joda-time" % "2.2",
  "commons-io" % "commons-io" % "2.4",
  "com.gu" %% "pa-client" % "4.2-SNAPSHOT"
)

play.Project.playScalaSettings

ideaExcludeFolders += "data"
