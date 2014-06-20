package com.cctrader.systems.movingaverage

import akka.actor.Actor.Receive
import com.cctrader.TradingSystemActor
import com.cctrader.data.Signal.Signal
import com.cctrader.data.Signal.Signal
import com.cctrader.data.{Signal, MarketDataSet, DataPoint}

/**
 *
 */
class MovingAverageTSActor extends TradingSystemActor{



  override def receive: Receive = {
    case "Test" =>
      println("Yo")

  }

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @param marketDataSet the data set to train on
   * @return unixTimestamp for training duration. Timestamp at end of training - start timestamp.
   */
  override def train(marketDataSet: MarketDataSet): Int = {0}

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @param dataPoint dataPoint to evaluate
   * @return BUY, SELL or HOLD signal
   */
  override def newDataPoint(dataPoint: DataPoint): Signal = {Signal.BUY}
}
