def commonSettings(module: String) = List(
  name := s"football-time-machine-$module",
  organization := "com.gu",
  description:= "Stores pa feed on s3 to replay them later",
  version := "1.0",
  scalaVersion := "2.11.8",
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
  assemblyJarName := s"${name.value}.jar",
  riffRaffPackageType := assembly.value,
  riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
  riffRaffUploadManifestBucket := Option("riffraff-builds"),
  riffRaffArtifactResources += (file("archive-cfn.yaml"), s"${name.value}-cfn/cfn.yaml")
)

lazy val archive = project
  .enablePlugins(RiffRaffArtifact)
  .settings(commonSettings("archive"))
  .settings(
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.gu" %% "pa-client" % "6.0.2",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.128",
      "com.typesafe" % "config" % "1.3.1"
    )
)

lazy val root = project.in(file(".")).aggregate(archive)