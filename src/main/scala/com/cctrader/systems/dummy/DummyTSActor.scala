package com.cctrader.systems.dummy

import akka.actor.{ActorRef, Props}
import com.cctrader.TradingSystemActor
import com.cctrader.data.Signal.Signal
import com.cctrader.data._

import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
class DummyTSActor(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter) extends {
  val signalWriter = signalWriterIn
  var marketDataSet = trainingMarketDataSet
} with TradingSystemActor {

  var sell = false

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return unixTimestamp for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(): Int = {
    100
  }

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @return BUY, SELL or HOLD signal
   */
  override def newDataPoint(): Signal = {
    if (sell) {
      sell = false
      Signal.SELL
    }
    else {
      sell = true
      Signal.BUY
    }
  }
}

object DummyTSActor {
  def props(trainingMarketDataSet: MarketDataSet, signalWriterIn: SignalWriter): Props =
    Props(new DummyTSActor(trainingMarketDataSet, signalWriterIn))
}