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
      exchange = Exchange.bitstamp
    )
    intercept[Exception] {
      val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)
    }
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
      exchange = Exchange.bitstamp
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
