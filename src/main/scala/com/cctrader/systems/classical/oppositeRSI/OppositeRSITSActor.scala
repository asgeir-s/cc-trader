package com.cctrader.systems.classical.oppositeRSI

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, MarketDataSet, Signaler}
import com.cctrader.indicators.technical.RelativeStrengthIndex

/**
 *
 */
class OppositeRSITSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val relativeStrengthIndex: RelativeStrengthIndex = new RelativeStrengthIndex(config.getInt("formula.RSIPeriods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    val rsi = relativeStrengthIndex(marketDataSet.size - 1, marketDataSet)
    println("rsi:" + rsi)
    if (rsi > thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (rsi < thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (rsi < thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (rsi > thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object OppositeRSITSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new OppositeRSITSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
