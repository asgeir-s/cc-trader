package com.cctrader

import java.util.Date

import akka.testkit.{TestActorRef, TestProbe}
import com.cctrader.data.{MarketDataSet, Exchange, CurrencyPair, Granularity}
import org.scalatest.Inside

/**
 *
 */
class DataActorITest extends ItTest{

  val testProbe = TestProbe()
  val dataActorRef = TestActorRef[DataActor]

  test("Send MarketDataSettings to DataActor en get back the corresponding MarketDataSet") {

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

    testProbe.send(dataActorRef, marketDataSettings)

    val marketDataSet = testProbe.expectMsgType[MarketDataSet]

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

}
