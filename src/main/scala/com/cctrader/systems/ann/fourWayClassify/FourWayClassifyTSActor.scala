package com.cctrader.systems.ann.fourWayClassify

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, MarketDataSet, Signaler}

/**
 *
 */
class FourWayClassifyTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {
/*
  val thresholdLong0 = config.getDouble("thresholds.long0")
  val thresholdLong2 = config.getDouble("thresholds.long2")
  val thresholdLong3 = config.getDouble("thresholds.long3")

  val thresholdShort0 = config.getDouble("thresholds.short0")
  val thresholdShort1 = config.getDouble("thresholds.short1")
  val thresholdShort3 = config.getDouble("thresholds.short3")

  val thresholdCloseLong0 = config.getDouble("thresholds.closeLong0")
  val thresholdCloseLong2 = config.getDouble("thresholds.closeLong2")
  val thresholdCloseLong3 = config.getDouble("thresholds.closeLong3")

  val thresholdCloseShort0 = config.getDouble("thresholds.closeShort0")
  val thresholdCloseShort1 = config.getDouble("thresholds.closeShort0")
  val thresholdCloseShort3 = config.getDouble("thresholds.closeShort0")
*/

  var count = 0

  val ann = new FourWayClassifyANN(settingPath)

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(trainingMarketDataSet: MarketDataSet): Long = {
    val startTrainingTime = System.currentTimeMillis()
    ann.train(trainingMarketDataSet)
    val endTrainingTime = System.currentTimeMillis()
    endTrainingTime - startTrainingTime
  }

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @return BUY, SELL or HOLD signal
   */
  override def newDataPoint() {
    log.info("2Received new dataPoint. MarketDataSet is now: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
      + ", toDate" + marketDataSet.toDate)

    val prediction = ann(marketDataSet)
    println("NEW DATAPOINT:")
    println("prediction:" + prediction.toVector)

      //Take Long position
      if ((prediction(0) + prediction(1)) > (prediction(2) + prediction(3) + thresholdLong)) {
        if (signalWriter.status.equals(Signal.CLOSE)) {
          goLong
        }
        else if (signalWriter.status == Signal.SHORT) {
          goClose
          goLong
        }
      }

      //Take Short position
      else if ((prediction(3) + prediction(2)) > (prediction(0) + prediction(1)) + thresholdShort) {
        if (signalWriter.status.equals(Signal.CLOSE)) {
          goShort
        }
        else if (signalWriter.status == Signal.LONG) {
          goClose
          goShort
        }
      }

      //Close Long position
      else if (signalWriter.status == Signal.LONG && (prediction(2) + prediction(3) + thresholdCloseLong) > (prediction(0) + prediction(1))) {
        goClose
      }

      //Close Short position
      else if (signalWriter.status == Signal.SHORT && (prediction(2) + prediction(3) + thresholdCloseShort) < (prediction(0) + prediction(1))) {
        goClose
      }
    }
}

object FourWayClassifyTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new FourWayClassifyTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}