import sbtassembly.MergeStrategy

def commonSettings(module: String) = List(
  name := s"football-time-machine-$module",
  organization := "com.gu",
  description:= "Stores pa feed on s3 to replay them later",
  version := "1.0",
  scalaVersion := "2.12.19",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-release:21",
    "-Ywarn-dead-code"
  ),
  assembly / assemblyMergeStrategy := {
    case "META-INF/MANIFEST.MF" => MergeStrategy.discard
    case _ => MergeStrategy.first
  },
  Compile / packageDoc / publishArtifact := false,
  assemblyJarName := s"${name.value}.jar"
)

val awsSdk2Version = "2.21.21"

lazy val archive = project
  .settings(commonSettings("archive"))
  .settings(
    resolvers ++= Seq(
      "Guardian GitHub Releases" at "https://guardian.github.com/maven/repo-releases",
      "Guardian GitHub Snapshots" at "https://guardian.github.com/maven/repo-snapshots"
    ),
      libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.gu" %% "pa-client" % "7.0.9",
      "com.gu" %% "simple-configuration-ssm" % "1.5.7",
      "software.amazon.awssdk" % "autoscaling" % awsSdk2Version,
      "software.amazon.awssdk" % "ec2" % awsSdk2Version,
      "software.amazon.awssdk" % "ssm" % awsSdk2Version,
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.311",
      "com.typesafe" % "config" % "1.3.1",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "io.netty" % "netty-codec-http" % "4.1.100.Final",
      "io.netty" % "netty-common" % "4.1.100.Final"
    )
  )

lazy val api = project
  .settings(commonSettings("api"))
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.307",
      "com.typesafe" % "config" % "1.3.1"
    )
  )

lazy val download = project.settings(
  libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.307"
)

lazy val root = project.in(file(".")).aggregate(archive, api)
