package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.HelperIndicators

/**
 *
 */
class ExponentialMovingAverageVolume(n: Int) extends HelperIndicators {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {

    var oldValue = data(t-n+1).volume
    var newValue = 0D

    val alpha: Double = 2D / (n + 1D);  //normal way to do it

    for (i <- t-n+2 to t) {
      newValue = oldValue + alpha * (data(i).volume - oldValue)
      oldValue = newValue
    }
    oldValue
  }
}
