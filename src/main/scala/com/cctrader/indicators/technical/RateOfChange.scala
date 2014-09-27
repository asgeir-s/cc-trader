package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * Does not need normalization
 */
class RateOfChange(n: Int) extends InputIndicator{
  println("n=" + n)

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    (data(t).close - data(t-n).close)/ (data(t-n).close)
  }

}
