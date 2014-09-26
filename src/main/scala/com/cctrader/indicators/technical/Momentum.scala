package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 102.900
 * min: -108.780
 * mean: -0.458
 * std: 21.317
 */
class Momentum(n: Int) extends InputIndicator {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    (data(t).close - data(t-n).close)
  }

}
