enablePlugins(RiffRaffArtifact, AssemblyPlugin)

name := "atom-event-forwarder"

version := "0.1"

scalaVersion := "2.12.3"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-java-sdk-sts" % "1.11.208",
  "com.amazonaws" % "amazon-kinesis-client" % "1.8.5",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.11.210"
)

// https://mvnrepository.com/artifact/com.gu/content-api-models-scala
libraryDependencies += "com.gu" %% "content-api-models-scala" % "11.31"

// https://mvnrepository.com/artifact/com.gu/content-api-models-json
libraryDependencies += "com.gu" %% "content-api-models-json" % "11.31"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.12
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

// https://mvnrepository.com/artifact/com.amazonaws/aws-lambda-java-log4j
libraryDependencies += "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0"

//logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.9.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.9.1",
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.7.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.4"
)

lazy val app = (project in file(".")).settings(
  organization := "com.theguardian",
  assemblyJarName in assembly := "atom-event-forwarder.jar",
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

/* don't run tests in the assembly stage, in CI these are run as a seperate stage */
test in assembly := {}

assemblyMergeStrategy in assembly := {
  case "shared.thrift" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestBranch := sys.env.getOrElse("CIRCLE_BRANCH","unknown")
riffRaffManifestRevision := sys.env.getOrElse("CIRCLE_BUILD_NUM","SNAPSHOT")
riffRaffManifestVcsUrl := sys.env.getOrElse("CIRCLE_BUILD_URL", "")
riffRaffBuildIdentifier := sys.env.getOrElse("CIRCLE_BUILD_NUM", "SNAPSHOT")

riffRaffManifestProjectName := "atom-event-forwarder"
