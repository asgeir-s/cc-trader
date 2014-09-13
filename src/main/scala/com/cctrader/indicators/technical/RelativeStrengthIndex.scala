package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *
 */
class RelativeStrengthIndex(n: Int) extends InputIndicator {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    var sumUp: Double = 0
    var sumDown: Double = 0
    for (i <- (t-n+1) to t) {
      val change = data(i).close - data(i-1).close

      if(change > 0) {
        sumUp+=change
      }
      else if(change < 0) {
        sumDown+=change.abs
      }
    }
    val rs = (sumUp/n)/(sumDown/n)
    (100-(100/(1+rs))) /100 //scaled by me
  }
}
