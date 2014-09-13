package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *
 * @param stochasticD the stochasticD to use for the average
 * @param n the number of previous time periods to use to calculate the average ("speed" of the average)
 */
class StochasticSlowD(stochasticD: StochasticD, n: Int) extends InputIndicator{
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return Stochastic Slow %D
   */
  def apply(t: Int, data: MarketDataSet): Double = {
    var sum: Double = 0
    for (i <- 0 to (n-1)) {
      sum+=stochasticD.apply(t-i, data)
    }
    sum/n
  }
}
