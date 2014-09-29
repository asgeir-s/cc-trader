package com.cctrader.systems.classical.macd

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data._
import com.cctrader.indicators.technical.MovingAverageExponentialConvergence

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Shows how to implement a TradingSystemActor.
 *
 * And used for testing of the TradingSystemActor trait.
 */
class MACDTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
  var marketDataSet = marketDataSetIn
  val signalWriter = signalWriterIn
  val settingPath = settingPathIn
} with TradingSystemActor {

  var hasTrade = false
  val maec = new MovingAverageExponentialConvergence(config.getInt("formula.slowPeriods"), config.getInt("formula.fastPeriods"))

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Long = {
    0
  }


  override def newDataPoint() {
    println("marketDataSet size:" + marketDataSet.size)
   val macd = maec(marketDataSet.size-1, marketDataSet)
    println("macd:" + macd)
    if (macd > thresholdLong) {
      if(signalWriter.status.equals(Signal.CLOSE)){
        goLong
      }
      else if (signalWriter.status == Signal.SHORT){
        goClose
        goLong
      }
    }
    else if ( macd < thresholdShort) {
      if (signalWriter.status.equals(Signal.CLOSE)) {
        goShort
      }
      else if (signalWriter.status == Signal.LONG) {
        goClose
        goShort
      }
    }
    else if (!signalWriter.status.equals(Signal.CLOSE)) {
      if ((macd < thresholdCloseLong) && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if ((macd > thresholdCloseShort) && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }

}

object MACDTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, settingPath: String): Props =
    Props(new MACDTSActor(trainingMarketDataSet, signalWriterIn, settingPath))
}