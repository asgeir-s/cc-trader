package com.cctrader.systems.ann.recurrentForwardIndicator

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.typesafe.config.ConfigFactory

/**
 *
 */
class RecurrentForwardIndicatorTSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, settingPath: String) extends {
  val config = ConfigFactory.load(settingPath)
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
  val stopPercentage = config.getDouble("thresholds.stopPercentage")
} with TradingSystemActor {

  val thresholdLong = config.getDouble("thresholds.long")
  val thresholdShort = config.getDouble("thresholds.short")
  val thresholdCloseShort = config.getDouble("thresholds.closeShort")
  val thresholdCloseLong = config.getDouble("thresholds.closeLong")

  val continueTrainingInterval = config.getInt("ml.continueTrainingInterval")
  val continueTrainingSetSize = config.getInt("ml.continueTrainingSetSize")

  var count = 0
  val ann = new RecurrentForwardIndicator(settingPath)
  var lastPredict:Double = 0


  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(): Long = {
    val startTrainingTime = System.currentTimeMillis()
    ann.train(marketDataSet)
    val endTrainingTime = System.currentTimeMillis()
    endTrainingTime - startTrainingTime
  }

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @return BUY, SELL or HOLD signal
   */
  override def newDataPoint() {
    val prediction = ann(marketDataSet)
    println("prediction: " + prediction)

    if (signalWriter.status == Signal.LONG && (marketDataSet.last.low < signalWriter.lastTrade.price * (1 - (stopPercentage/100)))) {
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 - (stopPercentage/100)))
    }

    else if(signalWriter.status == Signal.SHORT && (marketDataSet.last.high > signalWriter.lastTrade.price * (1 + (stopPercentage/100)))) {
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 + (stopPercentage/100)))
    }

    if (signalWriter.status == Signal.SHORT && prediction > thresholdCloseShort) {
      goClose
    }
    else if (signalWriter.status == Signal.LONG && prediction < thresholdCloseLong) {
      goClose
    }

    if (prediction > thresholdLong) { //0.4
        goLong
    }
    else if (prediction < thresholdShort){ // 0.4
        goShort
    }
    count+=1
    if(count == continueTrainingInterval) {
      ann.train(marketDataSet.subset(marketDataSet.size - continueTrainingSetSize, marketDataSet.size-1))
      count = 0
    }
    lastPredict = prediction
  }
}

object RecurrentForwardIndicatorTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new RecurrentForwardIndicatorTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}