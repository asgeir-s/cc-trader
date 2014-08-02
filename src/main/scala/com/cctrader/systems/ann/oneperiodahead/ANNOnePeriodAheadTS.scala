package com.cctrader.systems.ann.oneperiodahead

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.Signal._
import com.cctrader.data.{MarketDataSet, Signal, SignalWriter}
import com.cctrader.indicators.machin.ANNOnePeriodAhead

/**
 *
 */
class ANNOnePeriodAheadTS(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
} with TradingSystemActor {

  val aNNOnePeriodAhead = new ANNOnePeriodAhead()

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(): Long = {
    val startTrainingTime = System.currentTimeMillis()
    aNNOnePeriodAhead.train(marketDataSet)
    val endTrainingTime = System.currentTimeMillis()
    endTrainingTime - startTrainingTime
  }

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @return BUY, SELL or HOLD signal
   */
  override def newDataPoint() {
    val prediction = aNNOnePeriodAhead.compute(marketDataSet)

    if (prediction > 0.01) {
      goLoong
    }
    else if (prediction < -0.01) {
      goClose
    }
  }
}

object ANNOnePeriodAheadTS {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter): Props =
    Props(new ANNOnePeriodAheadTS(trainingMarketDataSet, signalWriterIn))
}
