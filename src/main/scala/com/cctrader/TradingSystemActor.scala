package com.cctrader

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data.Signal.Signal
import com.cctrader.data.{DataPoint, MarketDataSet}

/**
 *
 */
trait TradingSystemActor extends Actor with ActorLogging {

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @param marketDataSet the data set to train on
   * @return unixTimestamp for training duration. Timestamp at end of training - start timestamp.
   */
  def train(marketDataSet: MarketDataSet): Int

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @param dataPoint dataPoint to evaluate
   * @return BUY, SELL or HOLD signal
   */
  def newDataPoint(dataPoint: DataPoint): Signal


  override def receive: Receive = {
    case marketDataSet: MarketDataSet =>
      log.info("Received marketDataSet (for training): size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
        + ", toDate" + marketDataSet.toDate)
      val trainingTime = train(marketDataSet)
      context.parent ! TrainingDone(trainingTime)

    case dataPoint: DataPoint =>
      log.info("Received new dataPoint ")

  }

}
