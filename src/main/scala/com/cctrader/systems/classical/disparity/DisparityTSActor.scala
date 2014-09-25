package com.cctrader.systems.classical.disparity

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.cctrader.indicators.technical.{DisparityIndex, RelativeStrengthIndex}

/**
 *
 */
class DisparityTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val indicator = new DisparityIndex(config.getInt("formula.periods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    val disparity = indicator(marketDataSet.size - 1, marketDataSet)
    println("disparity:" + disparity)
    if (disparity > thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (disparity < thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (disparity < thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (disparity > thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object DisparityTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new DisparityTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
