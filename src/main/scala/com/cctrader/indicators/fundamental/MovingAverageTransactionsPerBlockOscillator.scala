package com.cctrader.indicators.fundamental

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *
 */
class MovingAverageTransactionsPerBlockOscillator(slowN: Int, fastN: Int) extends InputIndicator{

  val fastMovingAverage = new MovingAverageTransactionsPerBlock(fastN)
  val slowMovingAverage = new MovingAverageTransactionsPerBlock(slowN)

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {

    (fastMovingAverage.apply(t-1, data)- slowMovingAverage.apply(t-1, data)) / fastMovingAverage.apply(t-1, data) // -1 here because it should lock at the data for the day before
  }
}
