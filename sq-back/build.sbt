name := """sq-back"""
organization := "com.murdix"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += caffeine
libraryDependencies += ws

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

import com.typesafe.sbt.packager.docker._

dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerExposedPorts ++= Seq(9000)
dockerExposedVolumes := Seq("/opt/docker/run", "/opt/docker/logs")
dockerBaseImage := "openjdk:13"
dockerEntrypoint := Seq("/opt/docker/bin/sq-back", "-Dpidfile.path=/opt/docker/run/RUNNING_PID")
dockerCommands += ExecCmd("RUN", "touch", "/opt/docker/logs/application.log")
