package com.cctrader.systems.classical.oppositeROC

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, MarketDataSet, Signaler}
import com.cctrader.indicators.technical.{RateOfChange, RelativeStrengthIndex}

/**
 *
 */
class OppositeROCTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val rateOfChange = new RateOfChange(config.getInt("formula.ROCPeriods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    val roc = rateOfChange(marketDataSet.size - 1, marketDataSet)
    println("roc:" + roc)
    if (roc < thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (roc > thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (roc > thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (roc < thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object OppositeROCTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new OppositeROCTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
