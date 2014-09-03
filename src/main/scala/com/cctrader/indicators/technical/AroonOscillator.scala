package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

/**
 *
 */
class AroonOscillator(n: Int) extends InputIndicator {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def calculate(t: Int, data: MarketDataSet): Double = {

    var indexOfHigh: Int = t-n+1
    var indexOfLow: Int = t-n+1
    var high = data(t-n+1).high
    var low = data(t-n+1).low

    println("TOTO")

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
    (((n.toDouble-(t.toDouble-indexOfHigh.toDouble))/n.toDouble)*100.toDouble) - (((n.toDouble-(t.toDouble-indexOfLow.toDouble))/n.toDouble)*100.toDouble)
  }
}
