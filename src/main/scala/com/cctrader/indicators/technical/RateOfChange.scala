package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *
 */
class RateOfChange(n: Int) extends InputIndicator{
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    val indicator = (data(t).close / data(t-n).close) //* 100 //scaled by me
    //scale:
    indicator - 1
  }
}
