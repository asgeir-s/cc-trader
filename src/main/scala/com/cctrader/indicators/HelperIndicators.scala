package com.cctrader.indicators

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
  def apply(t: Int, data: MarketDataSet): Double


}
