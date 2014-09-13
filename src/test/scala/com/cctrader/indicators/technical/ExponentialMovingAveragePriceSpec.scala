package com.cctrader.indicators.technical

import java.util.Date

import com.cctrader.data._
import com.cctrader.{MarketDataSettings, UnitTest}

/**
 *
 */
class ExponentialMovingAveragePriceSpec extends UnitTest {

  val dataPointList = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 150, 5D, 5D, 150),
    DataPoint(None, None, (new Date(2L).getTime / 1000).toInt, 550D, 50, 5D, 5D, 50),
    DataPoint(None, None, (new Date(3L).getTime / 1000).toInt, 610D, 100, 5D, 5D, 100),
    DataPoint(None, None, (new Date(4L).getTime / 1000).toInt, 500D, 100, 5D, 5D, 100),
    DataPoint(None, None, (new Date(5L).getTime / 1000).toInt, 400D, 150, 5D, 5D, 150),
    DataPoint(None, None, (new Date(6L).getTime / 1000).toInt, 410D, 200, 5D, 5D, 200),
    DataPoint(None, None, (new Date(7L).getTime / 1000).toInt, 405D, 250, 5D, 5D, 250),
    DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 500D, 200, 5D, 5D, 500)
  )

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(new Date(0L).getTime / 1000),
    numberOfHistoricalPoints = 8,
    instrument = "bitstamp-BTC_USD-5min"
  )

  val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)
  val exponentialMovingAveragePrice4 = new ExponentialMovingAveragePrice(4)
  val exponentialMovingAveragePrice2 = new ExponentialMovingAveragePrice(2)

  /**
   * calculated by hand
   */
  "Calculation" should "be correct" in {
    assert(exponentialMovingAveragePrice4.apply(7, marketDataSet)==201.2)
    assert(exponentialMovingAveragePrice2.apply(2, marketDataSet)==83.33333333333333)
  }

}
