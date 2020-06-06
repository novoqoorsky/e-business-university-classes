name := "ex-2"

version := "0.1"

scalaVersion := "2.12.8"
val silhouetteVersion = "7.0.0"

resolvers ++= Seq(
  "atlassian-maven" at "https://maven.atlassian.com/content/repositories/atlassian-public"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "org.xerial"        %  "sqlite-jdbc" % "3.30.1",

  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-totp" % silhouetteVersion,

  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.iheart" %% "ficus" % "1.4.3"
)
libraryDependencies ++= Seq(guice)

libraryDependencies += filters

lazy val root = (project in file(".")).enablePlugins(PlayScala)