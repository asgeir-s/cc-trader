package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 * Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 100
 * min: 0
 * mean: 45.598
 * std: 33.531
 * @param stochasticK the stochasticK to use for the average
 * @param n the number of previous time periods to use to calculate the average ("speed" of the average)
 */
class StochasticD(stochasticK: StochasticK, n: Int) extends InputIndicator {
  normInRang(0, 100)
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return Stochastic %D
   */
  def apply(t: Int, data: MarketDataSet): Double = {
    var sum: Double = 0
    for (i <- 0 to (n-1)) {
      sum+=stochasticK.apply(t-i, data)
    }
    sum/n
  }
}
