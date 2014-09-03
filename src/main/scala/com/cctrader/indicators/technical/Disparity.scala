package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

/**
 *
 */
class Disparity(n: Int) extends InputIndicator{
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def calculate(t: Int, data: MarketDataSet): Double = {
    val movingAverage = new MovingAveragePrice(n)
    (data(t).close/movingAverage.calculate(t, data))*100
  }
}
