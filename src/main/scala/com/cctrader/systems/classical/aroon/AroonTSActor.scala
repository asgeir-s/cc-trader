package com.cctrader.systems.classical.aroon

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, MarketDataSet, Signaler}
import com.cctrader.indicators.technical.{AroonOscillator, DisparityIndex}

/**
 *
 */
class AroonTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val indicator = new AroonOscillator(config.getInt("formula.periods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    val aroon = indicator(marketDataSet.size - 1, marketDataSet)
    println("aroon:" + aroon)
    if (aroon > thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (aroon < thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (aroon < thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (aroon > thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object AroonTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new AroonTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
