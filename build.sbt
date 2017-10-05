enablePlugins(RiffRaffArtifact, AssemblyPlugin)

name := "atom-event-forwarder"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-java-sdk-sts" % "1.11.208",
  "com.amazonaws" % "amazon-kinesis-client" % "1.8.5"
)

// https://mvnrepository.com/artifact/com.gu/content-api-models-scala
libraryDependencies += "com.gu" %% "content-api-models-scala" % "11.31"

// https://mvnrepository.com/artifact/com.gu/content-api-models-json
libraryDependencies += "com.gu" %% "content-api-models-json" % "11.31"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.12
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.9.1"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.9.1"

libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"

lazy val app = (project in file(".")).settings(
  organization := "com.theguardian",
  assemblyJarName in assembly := "atom-event-forwarder.jar",

)

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
