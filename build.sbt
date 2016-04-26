name := "PlayScalaCassandraSimpleExercises"

version := "1.0"

lazy val `root` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test,
  "com.datastax.cassandra"  % "cassandra-driver-core" % "3.0.0",
  "org.webjars" % "angularjs" % "1.5.5",
  "org.webjars.bower" % "angular-animate" % "1.5.5",
  "org.webjars.bower" % "angular-touch" % "1.5.5",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.2")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"



routesGenerator := InjectedRoutesGenerator