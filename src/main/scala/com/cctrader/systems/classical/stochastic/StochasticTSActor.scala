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
    val stochasticKValue = stochasticK(marketDataSet.size - 1, marketDataSet)
    val stochasticDValue = stochasticD(marketDataSet.size - 1, marketDataSet)
    val diff = stochasticKValue - stochasticDValue
    println("Stochastic diff:" + diff)

    if (diff > thresholdLong) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goLong
      }
      else if (signalWriter.status == Signal.SHORT) {
        goClose
        goLong
      }
    }
    else if (diff < thresholdShort) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goShort
      }
      else if (signalWriter.status == Signal.LONG) {
        goClose
        goShort
      }
    }
    else if (!signalWriter.status.equals(Signal.CLOSE)) {
      if ((diff < thresholdCloseLong) && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if ((diff > thresholdCloseShort) && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object StochasticTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new StochasticTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}