package com.cctrader.systems.classical.stochastic

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, Signaler, MarketDataSet}
import com.cctrader.indicators.technical.{StochasticD, StochasticK}

/**
 *
 */
class StochasticTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {



  val stochasticK = new StochasticK(config.getInt("formula.periodsK"))
  val stochasticD = new StochasticD(stochasticK, config.getInt("formula.periodsD"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(trainingMarketDataSet: MarketDataSet): Long = {0}

  /**
   * Called when a new dataPoint is received. As last in marketDataSet.
   *
   * From this function call goLooong, goShort or goClose to write signals to the signal database.
   */
  override def newDataPoint(): Unit = {
    val stochasticKValue = stochasticK(marketDataSet.size-1, marketDataSet)
    val stochasticDValue = stochasticD(marketDataSet.size-1, marketDataSet)
    val diff = stochasticKValue - stochasticDValue

    if (signalWriter.status == Signal.SHORT && diff > thresholdCloseShort) {
      goClose
    }
    else if (signalWriter.status == Signal.LONG && diff < thresholdCloseLong) {
      goClose
    }
    if (diff > thresholdLong) { //0.4
      goLong
    }
    else if (diff < thresholdShort){ // 0.4
      goShort
    }
  }
}

object StochasticTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new StochasticTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}