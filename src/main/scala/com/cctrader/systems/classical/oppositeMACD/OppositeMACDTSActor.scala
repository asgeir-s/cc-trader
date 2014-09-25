package com.cctrader.systems.classical.oppositeMACD

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
class OppositeMACDTSActor(marketDataSetIn: MarketDataSet, signalWriterIn: Signaler, settingPathIn: String) extends {
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
   val macd = maec(marketDataSet.size-1, marketDataSet)
    println("macd:" + macd)
    if (macd < thresholdLong) {
      goLong
      hasTrade = true
    }
    else if (macd > thresholdShort) {
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (macd > thresholdCloseLong && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (macd < thresholdCloseShort && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }

}

object OppositeMACDTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, settingPath: String): Props =
    Props(new OppositeMACDTSActor(trainingMarketDataSet, signalWriterIn, settingPath))
}