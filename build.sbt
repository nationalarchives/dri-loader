ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"


libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "3.0.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-jms" % "3.0.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-jms" % "3.0.4",
  "javax.jms" % "javax.jms-api" % "2.0.1",
  "org.apache.activemq" % "activemq-core" % "5.7.0",
  "org.scalactic" %% "scalactic" % "3.2.14",
  "org.scalatest" %% "scalatest" % "3.2.14" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.20" % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
  "com.typesafe.play" %% "play-json" % "2.10.0-RC6",
  "com.whisk" %% "docker-testkit-scalatest" % "0.11.0",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.9" % Test
)


val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)


enablePlugins(UniversalPlugin)
enablePlugins(JavaAppPackaging)

Universal / mappings += {
  val conf = ( Compile / resourceDirectory ).value / "application.conf"
  conf -> "conf/application.conf"
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
