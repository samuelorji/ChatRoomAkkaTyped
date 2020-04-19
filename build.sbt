name := "akkatyped"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.4",
   "com.typesafe.akka"      %% "akka-slf4j"           % "2.6.4" ,
  "ch.qos.logback"         %  "logback-classic"      % "1.2.1",
  "ch.qos.logback"         %  "logback-core"         % "1.2.1")

