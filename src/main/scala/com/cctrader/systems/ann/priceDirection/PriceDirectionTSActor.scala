package com.cctrader.systems.ann.priceDirection

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.cctrader.indicators.technical._

/**
 *
 */
class PriceDirectionTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  val output = config.getString("output.indicator")
  var count = 0
  var indicator = {
    if (output.equals("rateOfChange")) {
      new RateOfChange(config.getInt("indicators.rateOfChange"))
    }
    else if (output.equals("williamsR")) {
      new WilliamsR(config.getInt("indicators.williamsR"))
    }
    else if (output.equals("disparityIndex")) {
      new DisparityIndex(config.getInt("indicators.disparityIndex"))
    }
    else if (output.equals("aroonOscillator")) {
      new AroonOscillator(config.getInt("indicators.aroonOscillator"))
    }
    else {
      log.error("ForwardIndicator not correctly specified.")
      new RateOfChange(config.getInt("indicators.rateOfChange"))
    }
  }

  val ann = new PriceDirection(settingPath, indicator)

  val reversal = output.equals("williamsR")


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

    val directIndicator = ann.directIndicator(marketDataSet)
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
    else if (signalWriter.status == Signal.LONG && prediction < 0) {
      goClose
    }

    //Close Short position
    else if (signalWriter.status == Signal.SHORT && prediction > 0) {
      goClose
    }

  }
}

object PriceDirectionTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSetting: String): Props =
    Props(new PriceDirectionTSActor(trainingMarketDataSet, signalWriterIn, tsSetting))
}