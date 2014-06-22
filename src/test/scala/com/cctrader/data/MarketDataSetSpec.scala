package com.cctrader.data

import java.util.Date

import com.cctrader.{MarketDataSettings, UnitTest}

/**
 *
 */
class MarketDataSetSpec extends UnitTest {

  val dataPointList = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 550D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 610D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 400D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 410D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 405D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D)
  )


  "Exception" should "be thrown when the data set is larger then MaxPoints" in {
    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1339539816 * 1000),
      numberOfHistoricalPoints = 3,
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
    intercept[Exception] {
      val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)
    }
  }

  "Normalizer: change output test" should "!output test!" in {
    println("# Normalizer: change output test")

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1339539816 * 1000),
      numberOfHistoricalPoints = 10,
      granularity = Granularity.min5,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)

    println("Change:" + (marketDataSet(1).open - marketDataSet(0).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(0, 1)(0))
    println("Change:" + (marketDataSet(2).open - marketDataSet(1).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(1, 2)(0))
    println("Change:" + (marketDataSet(3).open - marketDataSet(2).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(2, 3)(0))
    println("Change:" + (marketDataSet(4).open - marketDataSet(3).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(3, 4)(0))
    println("Change:" + (marketDataSet(5).open - marketDataSet(4).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(4, 5)(0))
    println("Change:" + (marketDataSet(6).open - marketDataSet(5).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(5, 6)(0))
    println("Change:" + (marketDataSet(7).open - marketDataSet(6).open) + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedChangeArray(6, 7)(0))
    println()
  }

  "Normalizer: absolute output test" should "!output test!" in {
    println("# Normalizer: absolute output test")

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1339539816 * 1000),
      numberOfHistoricalPoints = 10,
      granularity = Granularity.min5,
      currencyPair = CurrencyPair.BTC_USD,
      exchange = Exchange.bitstamp,
      PriceChangeScale = 70,
      VolumeChangeScale = 1000,
      MinPrice = 0,
      MaxPrice = 1500,
      MinVolume = 0,
      MaxVolume = 10000
    )

    val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)

    println("Value:" + marketDataSet(0).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(0)(0))
    println("Value:" + marketDataSet(1).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(1)(0))
    println("Value:" + marketDataSet(2).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(2)(0))
    println("Value:" + marketDataSet(3).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(3)(0))
    println("Value:" + marketDataSet(4).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(4)(0))
    println("Value:" + marketDataSet(5).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(5)(0))
    println("Value:" + marketDataSet(6).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(6)(0))
    println("Value:" + marketDataSet(7).open + ", normalized:" + marketDataSet.dataPointSigmoidNormalizedAbsoluteArray(7)(0))
    println()
  }

  "When more dataPoints then maxPoint (numberOfHistoricalPoints in settings) is added the list" should
    "not grow, but insted remove the oldest point" in {
    val smallDataPointList = List(
      DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D),
      DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 550D, 5D, 5D, 5D, 50D)
    )

    val marketDataSettings = MarketDataSettings(
      startDate = new Date(1339539816 * 1000),
      numberOfHistoricalPoints = 3,
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

    val marketDataSet = MarketDataSet(smallDataPointList, marketDataSettings)
    marketDataSet.size should be(2)
    marketDataSet.addDataPoint(DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    marketDataSet.size should be(3)
    marketDataSet.addDataPoint(DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 400D, 5D, 5D, 5D, 50D))
    marketDataSet.size should be(3)
    marketDataSet.addDataPoint(DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 300D, 5D, 5D, 5D, 50D))
    marketDataSet.size should be(3)

  }

}
