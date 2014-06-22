package com.cctrader

import java.util.Date

import akka.testkit.TestActorRef
import com.cctrader.data._

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * For this tests to run data must be written to the table bitstampTest.
 * Can be don by running the test BitcoinChartsToDBActorSpec one time.
 */
class DataActorSpec extends UnitTest {

  val dataActorRef = TestActorRef[DataActor]
  val actor = dataActorRef.underlyingActor

  "startDate and endDate" should
    "be set to the first and last entry, respectfully, in the test-cvs." in {
    println("Start:" + actor.startTime)
    assert(actor.startTime.compareTo(new Date(1315922016L * 1000L)) == 0)
    assert(actor.endTime.after(new Date(1403173902L * 1000L)))
  }

  "readFromDB" should
    "not return ant data outside of the specified time interval, for Granularity.min1" in {
    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1325922016L * 1000L),
      numberOfHistoricalPoints = 100,
      granularity = Granularity.min1,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    assert(marketDataSet(0).date.before(new Date(1332922016L * 1000L)))
    assert(marketDataSet.last.date.after(new Date((1325922016L - 60 * 101) * 1000L)))
    assert(marketDataSet.last.date.before(new Date((1325922016L - 60 * 99) * 1000L)))
    assert(marketDataSet(0).date.compareTo(new Date(1325881368L * 1000L)) >= 0)
    assert(marketDataSet(0).date.compareTo(new Date(1325960059L * 1000L)) < 0)

    assert(marketDataSet(0).close == 6)
    assert(marketDataSet(0).volume == 0)

    assert(marketDataSet.last.date.compareTo(new Date(1325916036L * 1000L)) == 0)
    assert(marketDataSet.last.close == 6)
    assert(marketDataSet.last.volume == 0)


  }

  "readFromDB" should
    "not return ant data outside of the specified time interval, for Granularity.min30" in {
    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1325922016L * 1000L),
      numberOfHistoricalPoints = 100,
      granularity = Granularity.min30,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    assert(marketDataSet(0).date.before(new Date(1332922016L * 1000L)))
    assert(marketDataSet.last.date.after(new Date((1325922016L - 1800 * 101) * 1000L)))
    assert(marketDataSet.last.date.before(new Date((1325922016L - 1800 * 99) * 1000L)))
    assert(marketDataSet(0).date.compareTo(new Date(1325881368L * 1000L)) >= 0)
    assert(marketDataSet(0).date.compareTo(new Date(1325960059L * 1000L)) < 0)

    assert(marketDataSet(0).close == 6)
    assert(marketDataSet(0).volume == 0)

    assert(marketDataSet.last.date.compareTo(new Date(1325742816L * 1000L)) == 0)
    assert(marketDataSet.last.close == 5.75)
    assert(marketDataSet.last.volume == 0)

  }

  "readFromDB" should
    "not return ant data outside of the specified time interval, for Granularity.day" in {

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1325922016L * 1000L),
      numberOfHistoricalPoints = 30,
      granularity = Granularity.day,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    assert(marketDataSet(0).date.before(new Date(1332922016L * 1000L)))
    assert(marketDataSet.last.date.after(new Date((1325922016L - 86400 * 31) * 1000L)))
    assert(marketDataSet.last.date.before(new Date((1325922016L - 86400 * 29) * 1000L)))
    assert(marketDataSet(0).date.compareTo(new Date(1325858016L * 1000L)) == 0)
    assert(marketDataSet(0).date.compareTo(new Date(1325960059L * 1000L)) < 0)

    assert(marketDataSet(0).close == 6.9)
    assert(marketDataSet(0).volume == 41.83067004)

    assert(marketDataSet.last.date.compareTo(new Date(1323352416L * 1000L)) == 0)
    assert(marketDataSet.last.close == 3.03)
    assert(marketDataSet.last.volume == 155.65351909)

  }

  "The returned DataPoints from request data" should "be the numberOfHistorical points before" in {

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1335225456L * 1000L),
      numberOfHistoricalPoints = 10,
      granularity = Granularity.min1,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings
    )

    val iterator = marketDataSet.iterator

    var lastId: Int = iterator.next().id.toString.substring(5, 11).toInt - 1
    while (iterator.hasNext) {
      assert(iterator.next().id.toString.substring(5, 11).toInt == lastId)
      lastId = lastId - 1
    }
  }
}
