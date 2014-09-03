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
      startDate = new Date(1366343016L * 1000L),
      numberOfHistoricalPoints = 100,
      granularity = Granularity.min1,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    println(marketDataSet)
    println("From Date:" + marketDataSet.fromDate.getTime/1000)
    println("To Date:" + marketDataSet.toDate.getTime/1000)

    println("First Date:" + marketDataSet.first.date.getTime/1000)
    println("Last Date:" + marketDataSet.last.date.getTime/1000)

    assert(marketDataSet.first.date.before(new Date(1366343016L * 1000L)))
    assert(marketDataSet.last.date.before(new Date((1366343016L + 100) * 1000L)))
    assert(marketDataSet.last.date.after(new Date((1366343016L - 100) * 1000L)))
    assert(marketDataSet.first.date.compareTo(new Date((1366343016L - 60 * 101) * 1000L)) >= 0)
    assert(marketDataSet.first.date.compareTo(new Date((1366343016L - 60 * 98) * 1000L)) < 0) // ikke 99 pga avrunding?

    assert(marketDataSet.first.close == 114)
    assert(marketDataSet.first.volume.toInt == 31)

    assert(marketDataSet.last.date.compareTo(new Date(1366343016L * 1000L)) == 0)
    assert(marketDataSet.last.close == 109.5)
    assert(marketDataSet.last.volume.toInt == 6)

  }

  "readFromDB" should
    "not return ant data outside of the specified time interval, for Granularity.min30" in {
    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1325922016L * 1000L),
      numberOfHistoricalPoints = 100,
      granularity = Granularity.min30,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    assert(marketDataSet.first.date.before(new Date(1332922016L * 1000L)))
    assert(marketDataSet.last.date.after(new Date((1325922016L - 1800) * 1000L)))
    assert(marketDataSet.last.date.before(new Date((1325922016L + 1800) * 1000L)))
    assert(marketDataSet.first.date.compareTo(new Date((1325922016L - 1800 * 101) * 1000L)) >= 0)
    assert(marketDataSet.first.date.compareTo(new Date((1325922016L - 1800 * 99) * 1000L)) < 0)

    assert(marketDataSet.last.close == 6)
    assert(marketDataSet.last.volume == 0)

    assert(marketDataSet.first.date.compareTo(new Date(1325742816L * 1000L)) == 0)
    assert(marketDataSet.first.close == 5.75)
    assert(marketDataSet.first.volume == 0)

  }

  "readFromDB" should
    "not return ant data outside of the specified time interval, for Granularity.day" in {

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1325922016L * 1000L),
      numberOfHistoricalPoints = 30,
      granularity = Granularity.day,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    assert(marketDataSet.last.date.before(new Date(1332922016L * 1000L)))
    assert(marketDataSet.first.date.after(new Date((1325922016L - 86400 * 31) * 1000L)))
    assert(marketDataSet.first.date.before(new Date((1325922016L - 86400 * 29) * 1000L)))
    assert(marketDataSet.last.date.compareTo(new Date(1325858016L * 1000L)) == 0)
    assert(marketDataSet.last.date.compareTo(new Date(1325960059L * 1000L)) < 0)

    assert(marketDataSet.last.close == 6.9)
    assert(marketDataSet.last.volume == 41.83067004)

    assert(marketDataSet.first.date.compareTo(new Date(1323352416L * 1000L)) == 0)
    assert(marketDataSet.first.close == 3.03)
    assert(marketDataSet.first.volume == 155.65351909)

  }

  "The returned DataPoints from request data" should "be the numberOfHistorical points before" in {

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1335225456L * 1000L),
      numberOfHistoricalPoints = 10,
      granularity = Granularity.min1,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp
    )

    val marketDataSet = actor.getDataFromDB(marketDataSettings)

    val iterator = marketDataSet.iterator

    var lastId: Int = iterator.next().id.toString.substring(5, 11).toInt + 1
    while (iterator.hasNext) {
      assert(iterator.next().id.toString.substring(5, 11).toInt == lastId)
      lastId = lastId + 1
    }
  }
}
