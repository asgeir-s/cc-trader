package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 115.682
 * min: 87.959
 * mean: 99.949
 * std: 2.682
 */
class Disparity(n: Int) extends InputIndicator{
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    val movingAverage = new MovingAveragePrice(n)
    (data(t).close/movingAverage(t, data)) * 100
  }
}
