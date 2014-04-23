import sbt._
import Keys._
import com.gu.deploy.MagentaArtifact._
import sbtassembly.Plugin._
import AssemblyKeys._
import play.Project._
//import Dependencies._

object ApplicationBuild extends Build {
  val appName 		= "football-time-machine"

  val appVersion 	= "1.0-SNAPSHOT"

  val appDependencies = Nil

  val main = play.Project(appName, appVersion, appDependencies)
  .settings(magentaArtifactSettings: _*)

}