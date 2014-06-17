package com.cctrader.data

import java.io.File

import akka.testkit.TestActorRef
import com.cctrader.UnitTest
import com.typesafe.config.{Config, ConfigFactory}

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}


/**
 * Before this test can run the database must be set up and the file src/test/resources/testConfig
 * must be configurated correctly.
 */
class BitcoinChartsToDBActorSpec extends UnitTest {

  val myConfigFile = new File("src/test/resources/testConfig")
  val fileConfig: Config = ConfigFactory.parseFile(myConfigFile)
  val config = ConfigFactory.load(fileConfig)

  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.dbname"), driver = config.getString("postgres.driver"), user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  // only do new like this in tests.
  val actorRef = TestActorRef(new BitcoinChartsToDBActor(false, false,
    true, true, "src/test/resources/bitstampUSD1400588707506.csv", databaseFactory.createSession()))


  val actor = actorRef.underlyingActor

  implicit val session: Session = databaseFactory.createSession()

  val tickTable = TableQuery[TickTable]
  val min1Table = TableQuery[Min1Table]
  val min2Table = TableQuery[Min2Table]
  val min5Table = TableQuery[Min5Table]
  val min10Table = TableQuery[Min10Table]
  val min15Table = TableQuery[Min15Table]
  val min30Table = TableQuery[Min30Table]
  val hour1Table = TableQuery[Hour1Table]
  val hour2Table = TableQuery[Hour2Table]
  val hour5Table = TableQuery[Hour5Table]
  val hour10Table = TableQuery[Hour12Table]
  val hour24Table = TableQuery[DayTable]

  test("Check the TickTable gets correctly imported.") {

    val result1 = tickTable.filter(x => x.timestamp === 1315922024).list
    assert(result1.size == 1)
    assert(result1(0).price == 5.830000000000)
    assert(result1(0).amount == 3.000000000000)

    val result2 = tickTable.filter(x => x.timestamp === 1333296140).list
    assert(result2.size == 2)
    assert(result2(0).price == 4.770000000000)
    assert(result2(0).amount == 0.042386830000)
  }

}
