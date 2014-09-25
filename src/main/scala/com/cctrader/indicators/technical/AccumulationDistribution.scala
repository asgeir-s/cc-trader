package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *Genetic algorithms approach to feature discretization in artificial neural networks for the prediction of stock price index:
 * max: 3.730
 * min: -0.157
 * mean: 0.447
 * std: 0.334
 *
 * normOutRang needs to be set to use normalization
 */
class AccumulationDistribution extends InputIndicator {

  var lastAD = 0D
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    val indicator = lastAD + (((data(t).close-data(t).low)-(data(t).high-data(t).close))/(data(t).high-data(t).low))*data(t).volume
    lastAD = indicator
    // ensures that a number is returned
    if (indicator > Double.MinValue && indicator < Double.MaxValue) {indicator}
    else {0}
  }
}
