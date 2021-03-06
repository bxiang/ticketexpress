import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "ticketexpress"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.novus" % "salat_2.10" % "1.9.2",
      "se.radley" %% "play-plugins-salat" % "1.2",
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0",
      "com.typesafe.akka" %% "akka-actor" % "2.1.2"      
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      routesImport += "se.radley.plugin.salat.Binders._",
      templatesImport += "org.bson.types.ObjectId",
      resolvers += Resolver.sonatypeRepo("snapshots")
    )

}
