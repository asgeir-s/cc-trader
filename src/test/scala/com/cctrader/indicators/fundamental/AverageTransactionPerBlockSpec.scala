package com.cctrader.indicators.fundamental

import java.util.Date

import com.cctrader.data.{DataPoint, MarketDataSet}
import com.cctrader.{MarketDataSettings, UnitTest}

/**
 *
 */
class AverageTransactionPerBlockSpec extends UnitTest{

  final val dataPointList = List(
    DataPoint(None, None, 1359849600, 105, 150, 100D, 160D, 150),
    DataPoint(None, None, 1369849600, 35, 50, 30D, 60D, 50),
    DataPoint(None, None, 1379849600, 95, 100, 90D, 120D, 100),
    DataPoint(None, None, 1389849600, 75, 100, 70D, 110D, 100),
    DataPoint(None, None, 1399849600, 135, 150, 130D, 500D, 150),
    DataPoint(None, None, 1399859600, 165, 200, 160D, 220D, 200),
    DataPoint(None, None, 1399869600, 220, 250, 100D, 300D, 250),
    DataPoint(None, None, 1399879600, 180, 200, 170D, 220D, 500)
  )

  final val marketDataSettings = MarketDataSettings(
    startDate = new Date(new Date(0L).getTime / 1000),
    numberOfHistoricalPoints = 8,
    instrument = "bitstamp-BTC_USD-5min"
  )

  final val mds1 = MarketDataSet(dataPointList, marketDataSettings)
  final val mds2 = MarketDataSet(dataPointList, marketDataSettings)

  "AverageTransactionPerBlock" should
    "load all data in to a list" in {
    val avg = new AverageTransactionPerBlock

    //avg.list.foreach(x => println(x._1))
    assert(avg.findValueForDate("\"2010-02-26 00:00:00\"")==(1.02))
    assert(avg.apply(1, mds1) == avg.apply(1, mds2))
    assert(avg.apply(1, mds1) == 327.74)
    assert(avg.apply(2, mds1) == 250.75)
    assert(avg.apply(2, mds1) == 250.75)
  }

}
