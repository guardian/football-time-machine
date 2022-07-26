import com.gu.riffraff.artifact.RiffRaffArtifact.autoImport._

def commonSettings(module: String) = List(
  name := s"football-time-machine-$module",
  organization := "com.gu",
  description:= "Stores pa feed on s3 to replay them later",
  version := "1.0",
  scalaVersion := "2.12.16",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Ywarn-dead-code"
  ),
  assembly / assemblyMergeStrategy := {
    case "META-INF/MANIFEST.MF" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly /assemblyMergeStrategy).value
      oldStrategy(x)
  },
  Compile / packageDoc / publishArtifact := false,
  packageDoc / publishArtifact := false,
  assemblyJarName := s"${name.value}.jar"
)

lazy val archive = project
  .settings(commonSettings("archive"))
  .settings(
    resolvers ++= Seq(
      "Guardian GitHub Releases" at "https://guardian.github.com/maven/repo-releases",
      "Guardian GitHub Snapshots" at "https://guardian.github.com/maven/repo-snapshots"
    ),
      libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.gu" %% "pa-client" % "7.0.5",
      "com.gu" %% "simple-configuration-ssm" % "1.5.6",
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