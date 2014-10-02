package com.cctrader.indicators.fundamental

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator

/**
 *
 */
class NumberOfBTCTransactionsOscillator(slowN: Int, fastN: Int) extends InputIndicator{

  val fastMovingAverage = new MovingAverageNumberOfBTCTransactions(fastN)
  val slowMovingAverage = new MovingAverageNumberOfBTCTransactions(slowN)

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    (fastMovingAverage.apply(t, data)- slowMovingAverage.apply(t, data)) / fastMovingAverage.apply(t, data)
  }
}

