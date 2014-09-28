package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 * This indicator fluctuates between -100 and +100 with zero as the middle line.
 * max: 100
 * min: -100
 *
 * normOutRang needs to be set to use normalization
 */
class AroonOscillator(n: Int) extends InputIndicator {
  normInRang(-100, 100)

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {

    var indexOfHigh: Int = t-n+1
    var indexOfLow: Int = t-n+1
    var high: Double = data(t-n+1).high
    var low: Double = data(t-n+1).low

    for (i <- (t-n+2) to t) {
      if(data(i).high > high) {
        indexOfHigh = i
        high = data(i).high
      }
      if(data(i).low < low) {
        indexOfLow = i
        low = data(i).low
      }
    }
    ((((n.toDouble-(t.toDouble-indexOfHigh.toDouble))/n.toDouble)*100.toDouble) - (((n.toDouble-(t.toDouble-indexOfLow.toDouble))/n.toDouble)*100.toDouble))
  }

}
