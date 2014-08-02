package com.cctrader.systems.dummy

import akka.actor.Props
import com.cctrader.TradingSystemActor
import com.cctrader.data.Signal.Signal
import com.cctrader.data._

import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Shows how to implement a TradingSystemActor.
 *
 * And used for testing of the TradingSystemActor trait.
 */
class DummyTSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
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
      goLoong
    }
    else {
      sell = true
      goClose
    }
  }
}

object DummyTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter): Props =
    Props(new DummyTSActor(trainingMarketDataSet, signalWriterIn))
}