name := "akka-talgo"

version := "1.0"

scalaVersion := "2.11.12"
val akkaVersion = "2.4.20"
val akkaHttpVersion = "10.0.11"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",

  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.scalaz" %% "scalaz-core" % "7.2.20",
  "org.slf4j" % "slf4j-api" % "1.7.22"
)


