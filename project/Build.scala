import sbt._

object Build extends Build {
  lazy val BitcoinPlatform =
    Project("Bitcoin-Platform", file("."))
      .configs( IntegrationTest )
      .settings( Defaults.itSettings : _*)
}