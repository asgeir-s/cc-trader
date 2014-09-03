package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

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
  override def calculate(t: Int, data: MarketDataSet): Double = {
    (data(t).close / data(t-n).close) * 100
  }
}
