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


  // only do new like this in tests.
  val dataActorRef = TestActorRef(new DataActor("src/test/resources/testConfig"))
  val actor = dataActorRef.underlyingActor

  test("startDate and endDate should be set to the first and last entry, respectfully, in the test-cvs.") {
    println("Start:" + actor.startTime)
    assert(actor.startTime.compareTo(new Date(1315922016L * 1000L)) == 0)
    assert(actor.endTime.compareTo(new Date(1335225477L * 1000L)) == 0)
  }

  test("readFromDB should not return ant data outside of the specified time interval, " +
    "for Granularity.min1") {

    val marketDataSet = actor.getDataFromDB(RequestData(
      new Date(1325922016L * 1000L),
      100,
      Granularity.min1,
      CurrencyPair.BTC_USD,
      Exchange.bitstamp))

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

  test("readFromDB should not return ant data outside of the specified time interval, " +
    "for Granularity.min30") {

    val marketDataSet = actor.getDataFromDB(RequestData(
      new Date(1325922016L * 1000L),
      100,
      Granularity.min30,
      CurrencyPair.BTC_USD,
      Exchange.bitstamp))

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

  test("readFromDB should not return ant data outside of the specified time interval, " +
    "for Granularity.day") {

    val marketDataSet = actor.getDataFromDB(RequestData(
      new Date(1325922016L * 1000L),
      30,
      Granularity.day,
      CurrencyPair.BTC_USD,
      Exchange.bitstamp))

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

  test("The returned Datapoints from request data should be the numberOfHistorical points before") {
    val marketDataSet = actor.getDataFromDB(RequestData(
      new Date(1335225456L * 1000L),
      10,
      Granularity.min1,
      CurrencyPair.BTC_USD,
      Exchange.bitstamp)
    )

    val iterator = marketDataSet.iterator

    var lastId: Int = iterator.next().id.toString.substring(5, 11).toInt - 1
    while (iterator.hasNext) {
      assert(iterator.next().id.toString.substring(5, 11).toInt == lastId)
      lastId = lastId - 1
    }
  }
}
