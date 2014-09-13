package com.cctrader.indicators.fundamental

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.HelperIndicators

/**
 *
 */
class MovingAverageNumberOfBTCTransactions(n: Int) extends HelperIndicators {

  val numberOfTransaction = new NumberOfTransaction

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    var sum: Double = 0
    for (i <- (t-n+1) to t) {
      sum+=numberOfTransaction.apply(i, data)
    }
    sum/n
  }
}
