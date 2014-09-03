package com.cctrader.indicators.technical

import java.util.Date

import com.cctrader.indicators.technical.AroonOscillator
import com.cctrader.{MarketDataSettings, UnitTest}
import com.cctrader.data._

/**
 *
 */
class AroonOscillatorSpec extends UnitTest {

  val dataPointList = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 105, 150, 100D, 160D, 150),
    DataPoint(None, None, (new Date(2L).getTime / 1000).toInt, 35, 50, 30D, 60D, 50),
    DataPoint(None, None, (new Date(3L).getTime / 1000).toInt, 95, 100, 90D, 120D, 100),
    DataPoint(None, None, (new Date(4L).getTime / 1000).toInt, 75, 100, 70D, 110D, 100),
    DataPoint(None, None, (new Date(5L).getTime / 1000).toInt, 135, 150, 130D, 500D, 150),
    DataPoint(None, None, (new Date(6L).getTime / 1000).toInt, 165, 200, 160D, 220D, 200),
    DataPoint(None, None, (new Date(7L).getTime / 1000).toInt, 220, 250, 100D, 300D, 250),
    DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 180, 200, 170D, 220D, 500)
  )

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(new Date(0L).getTime / 1000),
    numberOfHistoricalPoints = 8,
    granularity = Granularity.min5,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp
  )

  val marketDataSet = new MarketDataSet(dataPointList, marketDataSettings)
  val aroonOscillator4 = new AroonOscillator(4)
  val aroonOscillator6 = new AroonOscillator(7)

  /**
   * calculated by hand
   */
  "Calculation" should "be correct" in {
    println(aroonOscillator4.calculate(7, marketDataSet))
    println(aroonOscillator6.calculate(7, marketDataSet))
  }

}