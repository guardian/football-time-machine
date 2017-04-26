name := "football-time-machine"

organization := "com.gu"

description:= "Stores pa feed on s3 to replay them later"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.gu" %% "pa-client" % "6.0.2",
  "com.amazonaws" % "aws-java-sdk" % "1.11.123",
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.play" %% "play-ws" % "2.5.14"
)

enablePlugins(RiffRaffArtifact)

assemblyMergeStrategy in assembly := {
  case _ => MergeStrategy.first
}

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), s"${name.value}-cfn/cfn.yaml")