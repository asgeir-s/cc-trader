package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 * Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 100
 * min: -0.107
 * mean: 54.593
 * std: 33.637
 */
class WilliamsR(n: Int) extends InputIndicator {
  normInRang(0, 100)
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    // finding the lowest low and highest high
    var highestHighPrice = data(t-n).high
    var lowestLowPrice = data(t-n).low
    for (i <- (t-n+1) to t) {
      if(data(i).high > highestHighPrice){
        highestHighPrice = data(i).high
      }
      if (data(i).low < lowestLowPrice) {
        lowestLowPrice = data(i).low
      }
    }

    (((highestHighPrice - data(t).close) / (highestHighPrice - lowestLowPrice)) ) * 100
  }
}
