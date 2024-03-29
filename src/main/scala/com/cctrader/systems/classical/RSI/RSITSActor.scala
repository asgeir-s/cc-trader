package com.cctrader.systems.classical.rsi

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{MarketDataSet, Signal, Signaler}
import com.cctrader.indicators.technical.RelativeStrengthIndex

/**
 *
 */
class RSITSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
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
    if (rsi < thresholdLong) {
      if(signalWriter.status.equals(Signal.CLOSE)){
        goLong
      }
      else if (signalWriter.status == Signal.SHORT){
        goClose
        goLong
      }
    }
    else if (rsi > thresholdShort) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goShort
      }
      else if (signalWriter.status == Signal.LONG) {
        goClose
        goShort
      }
    }
    else if (!signalWriter.status.equals(Signal.CLOSE)) {
      if ((rsi > thresholdCloseLong ) && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if ((rsi < thresholdCloseShort) && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object RSITSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new RSITSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
