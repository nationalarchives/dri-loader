ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % "3.0.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-jms" % "3.0.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "3.0.4",
  "javax.jms" % "javax.jms-api" % "2.0.1",
  "uk.gov.tna.dri.catalogue" % "dri-catalogue-schema" % "1.36.33-SNAPSHOT",
  "org.apache.activemq" % "activemq-core" % "5.7.0",
  "org.scalactic" %% "scalactic" % "3.2.14",
  "org.scalatest" %% "scalatest" % "3.2.14" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.20" % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.20" % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,
  "com.typesafe.play" %% "play-json" % "2.10.0-RC6",
  "com.whisk" %% "docker-testkit-scalatest" % "0.11.0",
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.9" % Test
)

scalaxbVararg in (Compile, scalaxb) := false

scalaxbPackageName in (Compile, scalaxb) := "uk.gov.nationalarchives.dri.catalogue.api.generated"

scalaxbXsdSource in (Compile, scalaxb) := {
  val targetDir = (target in Universal).value
  targetDir / "xsd"
}

val copyResourcesFromCatalogue = TaskKey[Unit](
  "copyResourcesFromCatalogue",
  "Copy xsd resources from catalogue-schema"
)
// https://stackoverflow.com/questions/31406471/get-resource-file-from-dependency-in-sbt
copyResourcesFromCatalogue := {
  val dependencies = (dependencyClasspath in Compile).value
  val destDir = (scalaxbXsdSource in (Compile, scalaxb)).value
  def copyResourceFromJar(classpathEntry: Attributed[File],
                          jarName: String,
                          resourceName: String) = {
    classpathEntry.get(artifact.key) match {
      case Some(entryArtifact) => {
        // searching artifact
        if (entryArtifact.name.startsWith(jarName)) {
          // unpack artifact's jar to tmp directory
          val jarFile = classpathEntry.data
          IO.withTemporaryDirectory { tmpDir =>
            IO.unzip(jarFile, tmpDir)
            // copy to project's target directory
            // Instead of copying you can do any other stuff here
            IO.copyFile(tmpDir / resourceName, destDir / resourceName)
          }
        }
      }
      case _ =>
    }
  }
  for (entry <- dependencies) yield {
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_common.xsd")
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_DMS.xsd")
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_Result.xsd")
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_Closure.xsd")
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_Ingest.xsd")
    copyResourceFromJar(entry, "dri-catalogue-schema", "API_Query.xsd")
  }
}

(Compile / scalaxb) := ((Compile / scalaxb) dependsOn copyResourcesFromCatalogue).value


val circeVersion = "0.14.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeVersion)


enablePlugins(UniversalPlugin,JavaAppPackaging,ScalaxbPlugin)


Universal / mappings += {
  val conf = ( Compile / resourceDirectory ).value / "application.conf"
  conf -> "conf/application.conf"
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
