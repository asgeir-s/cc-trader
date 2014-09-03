package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

/**
 *
 */
class MovingAverageVolume(n: Int) extends HelperIndicators {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def calculate(t: Int, data: MarketDataSet): Double = {
    var sum: Double = 0
    for (i <- (t-n+1) to t) {
      sum+=data(i).volume
    }
    sum/n
  }
}
