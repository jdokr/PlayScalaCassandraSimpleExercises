name := "PlayScalaCassandraSimpleExercises"

version := "1.0"

lazy val `playscalacassandrasimpleexercises` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test, "com.datastax.cassandra"  % "cassandra-driver-core" % "3.0.0")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  