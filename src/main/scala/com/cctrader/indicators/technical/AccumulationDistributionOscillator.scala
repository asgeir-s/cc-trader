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
class AccumulationDistributionOscillator extends InputIndicator {
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    val indicator = (data(t).high - data(t - 1).close) / (data(t).high - data(t).low)

    // ensures that a number is returned
    if (indicator > Double.MinValue && indicator < Double.MaxValue) {indicator}
    else {0}
  }
}
