package com.cctrader.data

import java.util.Date

import akka.testkit.{TestActorRef, TestProbe}
import com.cctrader.{MarketDataSettings, RequestLiveBTData, UnitTest}
import com.typesafe.config.ConfigFactory

import scala.slick.jdbc.JdbcBackend._
import scala.slick.jdbc.ResultSetConcurrency

/**
 *
 */
class LiveDataActorSpec extends UnitTest {
  val tsCoordinator = TestProbe()

  val config = ConfigFactory.load()
  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.dbname"),
    driver = config.getString("postgres.driver"),
    user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  implicit val session: Session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(8L),
    numberOfHistoricalPoints = 8,
    granularity = Granularity.min5,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp,
    PriceChangeScale = 50,
    VolumeChangeScale = 1000,
    MinPrice = 0,
    MaxPrice = 1500,
    MinVolume = 0,
    MaxVolume = 10000
  )

  val liveDataActorRef = TestActorRef(new LiveDataActor(session, marketDataSettings))
  val dummyTSCoordinatorActor = liveDataActorRef.underlyingActor

  "When receiving RequestLiveBTData it" should "return data from given date and the number of points" in {
    val askDate = new Date(1339539816L * 1000L)
    tsCoordinator.send(liveDataActorRef, RequestLiveBTData(askDate, 100))
    val result1 = tsCoordinator.expectMsgType[DataPoint]
    assert(result1.date.compareTo(askDate) >= 0)
    val result2 = tsCoordinator.expectMsgType[DataPoint]
    assert(result2.date.compareTo(result1.date) >= 0)
    val result3 = tsCoordinator.expectMsgType[DataPoint]
    assert(result3.date.compareTo(result2.date) >= 0)
    val result4 = tsCoordinator.expectMsgType[DataPoint]
    assert(result4.date.compareTo(result3.date) >= 0)
    val result5 = tsCoordinator.expectMsgType[DataPoint]
    assert(result5.date.compareTo(result4.date) >= 0)
    val result6 = tsCoordinator.expectMsgType[DataPoint]
    assert(result6.date.compareTo(result5.date) >= 0)
    val result7 = tsCoordinator.expectMsgType[DataPoint]
    assert(result7.date.compareTo(result6.date) >= 0)
    tsCoordinator.receiveN(93)
  }

}
