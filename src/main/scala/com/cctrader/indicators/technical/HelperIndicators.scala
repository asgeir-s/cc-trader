package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

/**
 *
 */
trait HelperIndicators {

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  def calculate(t: Int, data: MarketDataSet): Double


}
