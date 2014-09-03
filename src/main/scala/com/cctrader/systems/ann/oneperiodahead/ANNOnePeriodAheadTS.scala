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
  val stopPercentage = 5.0
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
    println("prediction: " + prediction)

    if (signalWriter.status == Signal.LOONG && (marketDataSet.last.low < signalWriter.lastTrade.price * (1 - (stopPercentage/100)))) {
      goCloseStop(signalWriter.lastTrade.price * (1 - (stopPercentage/100)))
    }

    else if(signalWriter.status == Signal.SHORT && (marketDataSet.last.high > signalWriter.lastTrade.price * (1 + (stopPercentage/100)))) {
      goCloseStop(signalWriter.lastTrade.price * (1 + (stopPercentage/100)))
    }


    if (signalWriter.status == Signal.SHORT && prediction > -0.05) {
      goClose
    }
    else if (signalWriter.status == Signal.LOONG && prediction < 0.05) {
      goClose
    }

    if (prediction > 0.4) {
      if(signalWriter.status == Signal.CLOSE) {
        goLoong
      }
    }
    else if (prediction < -0.4){
      if(signalWriter.status == Signal.CLOSE) {
        goShorte
      }
    }

  }
}

object ANNOnePeriodAheadTS {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter): Props =
    Props(new ANNOnePeriodAheadTS(trainingMarketDataSet, signalWriterIn))
}
