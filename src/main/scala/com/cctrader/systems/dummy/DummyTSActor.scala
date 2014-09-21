package com.cctrader.systems.dummy

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data._

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Shows how to implement a TradingSystemActor.
 *
 * And used for testing of the TradingSystemActor trait.
 */
class DummyTSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
  val stopPercentage: Double = 0
} with TradingSystemActor {

  var sell = false

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(): Long = {
    100L * 1000L
  }


  override def newDataPoint() {
    if (sell) {
      sell = false
      goLong
    }
    else {
      sell = true
      goClose
    }
  }
}

object DummyTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: Signaler, tsSettingPath: String): Props =
    Props(new DummyTSActor(trainingMarketDataSet, signalWriterIn, tsSettingPath))
}