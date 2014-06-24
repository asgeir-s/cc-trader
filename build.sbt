organization  := "com.cctrader"

name := "CCTrader"

version := "1.0"

scalaVersion  := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.3"
  Seq(
    "com.typesafe.akka"       %%  "akka-actor"                  % akkaV,
    "com.typesafe.akka"       %%  "akka-testkit"                % akkaV               % "test",
    "org.scalatest"           %%  "scalatest"                   % "2.1.6"             % "test",
    "com.typesafe.slick"      %%  "slick"                       % "2.1.0-M2",
    "org.slf4j"               %   "slf4j-nop"                   % "1.6.4",
    "org.postgresql"          %   "postgresql"                  % "9.3-1101-jdbc41",
    "org.encog"               %   "encog-core"                  % "3.2.0",
    "org.apache.commons"      %   "commons-math3"               % "3.3"
  )
}


