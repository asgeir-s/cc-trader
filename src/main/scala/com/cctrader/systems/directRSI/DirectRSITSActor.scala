package com.cctrader.systems.directRSI

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.{Signal, MarketDataSet, Signaler}
import com.cctrader.indicators.technical.RelativeStrengthIndex

/**
 *
 */
class DirectRSITSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
  val stopPercentage: Double = 10
} with TradingSystemActor {

  var hasTrade = false
  val relativeStrengthIndex: RelativeStrengthIndex = new RelativeStrengthIndex(10);

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(): Long = {
    0
  }


  override def newDataPoint() {
    val rsi = relativeStrengthIndex(marketDataSet.size - 1, marketDataSet)
    println("rsi:" + rsi)
    if (rsi > 70) {
      println("GO LONG")
      goLong
      hasTrade = true
    }
    else if (rsi < 30) {
      println("GO SHORT")
      goShort
      hasTrade = true
    }
    if (hasTrade) {
      if (rsi < 50 && signalWriter.status == Signal.LONG) {
        goClose
      }
      else if (rsi > 50 && signalWriter.status == Signal.SHORT) {
        goClose
      }
    }
  }
}

object DirectRSITSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new DirectRSITSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}
