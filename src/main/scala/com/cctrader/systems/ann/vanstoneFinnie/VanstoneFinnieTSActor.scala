package com.cctrader.systems.ann.vanstoneFinnie

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.cctrader.indicators.technical._

/**
 *
 */
class VanstoneFinnieTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var count = 0

  val ann = new VanstoneFinnie(settingPath)

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
    println("prediction:" + prediction)
    //Take Long position
    if (prediction > thresholdLong) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goLong
      }
      else if (signalWriter.status == Signal.SHORT) {
        goClose
        goLong
      }
    }

    //Take Short position
    else if (prediction < thresholdShort) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goShort
      }
      else if (signalWriter.status == Signal.LONG) {
        goClose
        goShort
      }
    }

    //Close Long position
    else if (signalWriter.status == Signal.LONG && prediction < thresholdCloseLong) {
      goClose
    }

    //Close Short position
    else if (signalWriter.status == Signal.SHORT && prediction > thresholdCloseShort) {
      goClose
    }
  }


}

object VanstoneFinnieTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new VanstoneFinnieTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}