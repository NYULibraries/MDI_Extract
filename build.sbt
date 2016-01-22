scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

assemblyJarName in assembly := "mdi-fileidentify.jar"

mainClass in assembly := Some("edu.nyu.dlts.mdi.extract.Main")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.12",
  "com.typesafe" % "config" % "1.2.1",
  "com.rabbitmq" % "amqp-client" % "3.4.3",
  "org.json4s" % "json4s-jackson_2.11" % "3.2.10",
  "joda-time" % "joda-time" % "2.3",
  "org.apache.tika" % "tika-core" % "1.6",
  "org.apache.tika" % "tika-parsers" % "1.6",
  "org.apache.tika" % "tika-xmp" % "1.6",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)
