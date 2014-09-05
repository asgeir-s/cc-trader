package com.cctrader.systems.ann.oneperiodahead

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler, TSSettings}
import com.cctrader.indicators.machin.ANNOnePeriodAhead

/**
 *
 */
class ANNOnePeriodAheadTS(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: TSSettings) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
  val stopPercentage = tsSetting.stopPercentage.toDouble // TODO: config
} with TradingSystemActor {

  val aNNOnePeriodAhead = new ANNOnePeriodAhead(tsSetting)

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
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 - (stopPercentage/100)))
    }

    else if(signalWriter.status == Signal.SHORT && (marketDataSet.last.high > signalWriter.lastTrade.price * (1 + (stopPercentage/100)))) {
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 + (stopPercentage/100)))
    }

    if (signalWriter.status == Signal.SHORT && prediction > tsSetting.thresholdCloseShort) { // TODO: config
      goClose
    }
    else if (signalWriter.status == Signal.LOONG && prediction < tsSetting.thresholdLong) { // TODO: config
      goClose
    }

    if (prediction > tsSetting.thresholdLong) { //0.4 // TODO: config
      if(signalWriter.status == Signal.CLOSE) {
        goLoong
      }
    }
    else if (prediction < tsSetting.thresholdShort){ // 0.4 // TODO: config
      if(signalWriter.status == Signal.CLOSE) {
        goShorte
      }
    }

  }
}

object ANNOnePeriodAheadTS {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: TSSettings): Props =
    Props(new ANNOnePeriodAheadTS(trainingMarketDataSet, signalWriterIn, tsSetting))
}