package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 100
 * min: 0
 * mean: 47.598
 * std: 29.531
 */
class RelativeStrengthIndex(n: Int) extends InputIndicator {
  normInRang(0, 100)
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
    if(sumUp == 0){
      0
    }
    else if(sumDown == 0){
      100
    }
    else {
      val rs = (sumUp/n)/(sumDown/n)
      (100-(100/(1+rs)))
    }
  }

}
