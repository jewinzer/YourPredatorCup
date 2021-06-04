name := """play"""
organization := "winzer"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.mindrot" % "jbcrypt" % "0.4"
)
Compile / herokuProcessTypes := Map(
  "web" -> "target/universal/stage/bin/play-test -Dhttp.port=$PORT",
)

Compile / herokuJdkVersion:= "11"






// Adds additional packages into Twirl
//TwirlKeys.templateImports += "winzer.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "winzer.binders._"
