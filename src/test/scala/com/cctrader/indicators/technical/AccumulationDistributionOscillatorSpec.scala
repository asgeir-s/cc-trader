package com.cctrader.indicators.technical

import java.util.Date

import com.cctrader.data._
import com.cctrader.{MarketDataSettings, UnitTest}

/**
 *
 */
class AccumulationDistributionOscillatorSpec extends UnitTest {

  val dataPointList = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 105, 150, 100D, 160D, 150),
    DataPoint(None, None, (new Date(2L).getTime / 1000).toInt, 35, 50, 30D, 60D, 50),
    DataPoint(None, None, (new Date(3L).getTime / 1000).toInt, 95, 100, 90D, 120D, 100),
    DataPoint(None, None, (new Date(4L).getTime / 1000).toInt, 75, 100, 70D, 110D, 100),
    DataPoint(None, None, (new Date(5L).getTime / 1000).toInt, 135, 150, 130D, 200D, 150),
    DataPoint(None, None, (new Date(6L).getTime / 1000).toInt, 165, 200, 160D, 220D, 200),
    DataPoint(None, None, (new Date(7L).getTime / 1000).toInt, 220, 250, 200D, 300D, 250),
    DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 180, 200, 170D, 220D, 500)
  )

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(new Date(0L).getTime / 1000),
    numberOfHistoricalPoints = 8,
    instrument = "bitstamp-BTC_USD-5min"
  )

  val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)
  val accumulationDistributionOscillator4 = new AccumulationDistribution()
  val accumulationDistributionOscillator2 = new AccumulationDistribution()

  /**
   * calculated by hand
   */
  "Calculation" should "be correct" in {
    println(accumulationDistributionOscillator4.apply(7, marketDataSet))
    println(accumulationDistributionOscillator2.apply(2, marketDataSet))
  }

}