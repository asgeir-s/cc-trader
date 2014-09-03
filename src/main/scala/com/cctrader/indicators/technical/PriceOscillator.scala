package com.cctrader.indicators.technical

import com.cctrader.data.MarketDataSet

/**
 *
 */
class PriceOscillator(fastN: Int, slowN: Int) extends InputIndicator{
  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def calculate(t: Int, data: MarketDataSet): Double = {
    val fastMovingAverage = new MovingAveragePrice(fastN)
    val slowMovingAverage = new MovingAveragePrice(slowN)

    (fastMovingAverage.calculate(t, data)- slowMovingAverage.calculate(t, data)) / fastMovingAverage.calculate(t, data)
  }
}
