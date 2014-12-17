/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

releaseSettings

