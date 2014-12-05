name := "presto-scala-client"

version := "0.1"

libraryDependencies ++= Seq(
  "com.stackmob" %% "newman" % "1.3.5",
  "com.facebook.presto" % "presto-client" % "0.85",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2",
  "org.slf4j" % "slf4j-api" % "1.7.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.1",  // for any java classes looking for this
  "ch.qos.logback" % "logback-classic" % "1.0.3",
  "org.scalatest" % "scalatest_2.10" % "2.2.2" % "test"
)
