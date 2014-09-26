package com.cctrader.systems.classical.williamR

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.cctrader.indicators.technical.{WilliamsR, Momentum, RateOfChange}

/**
 *
 */
class WilliamRTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val williamR = new WilliamsR(config.getInt("formula.periods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    val will = williamR(marketDataSet.size - 1, marketDataSet)
    println("will:" + will)
    if (will < thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (will > thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (will > thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (will < thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }

  }
}

object WilliamRTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new WilliamRTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
