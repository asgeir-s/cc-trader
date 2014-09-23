package com.cctrader.systems.ann.forwardIndicator

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler, TSSettings}
import com.cctrader.indicators.machin.ANNOnePeriodAhead
import com.cctrader.indicators.technical.RelativeStrengthIndex
import com.typesafe.config.ConfigFactory

/**
 *
 */
class ForwardIndicatorsTSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, settingPath: String) extends {
  val config = ConfigFactory.load(settingPath)
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
  val stopPercentage = config.getDouble("thresholds.stopPercentage")
} with TradingSystemActor {

  val relativeStrengthIndex: RelativeStrengthIndex = new RelativeStrengthIndex(10)


  val thresholdLong = config.getDouble("thresholds.long")
  val thresholdShort = config.getDouble("thresholds.short")
  val thresholdCloseShort = config.getDouble("thresholds.closeShort")
  val thresholdCloseLong = config.getDouble("thresholds.closeLong")

  val continueTrainingInterval = config.getInt("ml.continueTrainingInterval")
  val continueTrainingSetSize = config.getInt("ml.continueTrainingSetSize")

  var count = 0
  //val aNNOnePeriodAhead = new ANNOnePeriodAhead(tsSetting)
  val ann = new ForwardIndicator(settingPath)
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
    val rsiToDay = relativeStrengthIndex(marketDataSet.size-1, marketDataSet)
    val prediction = ann(marketDataSet)
    println("Prediction: " + prediction)
    println("RSI today: " + rsiToDay)

    if (signalWriter.status == Signal.LONG && (marketDataSet.last.low < signalWriter.lastTrade.price * (1 - (stopPercentage/100)))) {
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 - (stopPercentage/100)))
    }

    else if(signalWriter.status == Signal.SHORT && (marketDataSet.last.high > signalWriter.lastTrade.price * (1 + (stopPercentage/100)))) {
      goCloseStopTestMode(signalWriter.lastTrade.price * (1 + (stopPercentage/100)))
    }

    if (signalWriter.status == Signal.SHORT && rsiToDay > 50) {
      goClose
    }
    else if (signalWriter.status == Signal.LONG && rsiToDay < 50) {
      goClose
    }

    if (prediction > thresholdLong || rsiToDay > 70) { //0.4
        goLong
    }
    else if (prediction < thresholdShort || rsiToDay <  30){ // 0.4
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

object ForwardIndicatorsTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new ForwardIndicatorsTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}