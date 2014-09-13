package com.cctrader.indicators.fundamental

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

import scala.collection.immutable.TreeMap

/**
 *
 */
class GoogleTrend extends InputIndicator {

  //load cvs to List
  val list = List()

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    // return last list entry before time of data(t)
  0
  }
}
