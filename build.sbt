organization := "info.dynagoya"

name := "yoyoma"

version := "0.1.0"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.0",
  "net.databinder" %% "unfiltered-jetty" % "0.6.0",
  "org.clapper" %% "avsl" % "0.3.6",
  "net.databinder" %% "unfiltered-spec" % "0.6.0" % "test",
  "org.scalaz" %% "scalaz-core" % "6.0.4",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.3.0"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)

seq(Revolver.settings: _*)




