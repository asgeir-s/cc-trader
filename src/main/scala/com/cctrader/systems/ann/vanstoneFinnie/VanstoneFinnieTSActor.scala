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

  val annUp = new VanstoneFinnie(settingPath, true)
  val annDown = new VanstoneFinnie(settingPath, false)

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(trainingMarketDataSet: MarketDataSet): Long = {
    val startTrainingTime = System.currentTimeMillis()
    annUp.train(trainingMarketDataSet)
    annDown.train(trainingMarketDataSet)
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

    val predictionMaxUp = annUp(marketDataSet)
    val predictionMinDown = annDown(marketDataSet)

    println("NEW DATAPOINT:")
    println("predictionMaxUp:" + predictionMaxUp)
    println("predictionMinDown:" + predictionMinDown)

    //Take Long position
    if (predictionMaxUp.abs > (predictionMinDown.abs + thresholdLong)) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goLong
      }
      else if (signalWriter.status == Signal.SHORT) {
        goClose
        goLong
      }
    }

    //Take Short position
    else if ((predictionMaxUp.abs + thresholdShort) < predictionMinDown.abs) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goShort
      }
      else if (signalWriter.status == Signal.LONG) {
        goClose
        goShort
      }
    }

    //Close Long position
    else if (signalWriter.status == Signal.LONG && (predictionMinDown.abs > predictionMaxUp.abs)) {
      goClose
    }

    //Close Short position
    else if (signalWriter.status == Signal.SHORT && (predictionMinDown.abs < predictionMaxUp.abs)) {
      goClose
    }
  }


}

object VanstoneFinnieTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new VanstoneFinnieTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}