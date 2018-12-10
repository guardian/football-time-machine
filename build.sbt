import com.gu.riffraff.artifact.RiffRaffArtifact.autoImport.riffRaffArtifactResources

def commonSettings(module: String) = List(
  name := s"football-time-machine-$module",
  organization := "com.gu",
  description:= "Stores pa feed on s3 to replay them later",
  version := "1.0",
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Ywarn-dead-code"
  ),
  assemblyMergeStrategy in assembly := {
    case "META-INF/MANIFEST.MF" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  assemblyJarName := s"${name.value}.jar"
)

lazy val archive = project
  .settings(commonSettings("archive"))
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.gu" %% "pa-client" % "6.0.2",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.158",
      "com.typesafe" % "config" % "1.3.1"
    )
  )

lazy val api = project
  .settings(commonSettings("api"))
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.158",
      "com.typesafe" % "config" % "1.3.1"
    )
  )

lazy val download = project.settings(
  libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.158"
)

lazy val root = project.in(file(".")).aggregate(archive, api)
  .enablePlugins(RiffRaffArtifact)
  .settings(
    name := "football-time-machine",
    riffRaffPackageType := file(".nothing"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactResources += (assembly in api).value -> s"${(name in api).value}/${(assembly in api).value.getName}",
    riffRaffArtifactResources += (assembly in archive).value -> s"${(name in archive).value}/${(assembly in archive).value.getName}"
  )