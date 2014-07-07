package com.cctrader

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data.Signal.Signal
import com.cctrader.data.{DataPoint, MarketDataSet, Mode, SignalWriter}

/**
 *
 */
trait TradingSystemActor extends Actor with ActorLogging {
  val signalWriter: SignalWriter
  var dataPointCountInAkk = 0
  var akkOn = 0
  var mode = Mode.TESTING
  var marketDataSet: MarketDataSet

  log.debug("Started: TradingSystemActor")


  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  def train(): Long

  /**
   * Evaluate new dataPoint.
   * Should be of the same granularity as the training set.
   * @return BUY, SELL or HOLD signal
   */
  def newDataPoint(): Signal


  override def receive: Receive = {
    case StartTraining(marketDataSetIn: MarketDataSet) =>
      log.debug("Received: StartTraining")
      marketDataSet = marketDataSetIn
      val trainingTime = train()
      sender ! TrainingDone(trainingTime)
      log.debug("Training done")

    case marketDataSetIn: MarketDataSet =>
      marketDataSet = marketDataSetIn
      log.info("Received new marketDataSet (will replace ord): size:" + marketDataSetIn.size + ", fromDate" + marketDataSetIn.fromDate
        + ", toDate" + marketDataSetIn.toDate)

    case dataPoint: DataPoint =>
      log.debug("Received DataPoint: time:" + dataPoint.date + ", info:" + dataPoint)
      marketDataSet.addDataPoint(dataPoint)
      signalWriter.newSignal(newDataPoint(), dataPoint) //compute dataPoint and write to database

      if (mode == Mode.TESTING) {
        dataPointCountInAkk = dataPointCountInAkk + 1
        if (dataPointCountInAkk >= akkOn) {
          sender() ! AkkOn(dataPointCountInAkk, 0)
          dataPointCountInAkk = 0
        }

      }
    case akk: AkkOn =>
      akkOn = akk.numberOfMessagesBeforeAkk
      dataPointCountInAkk = akk.initialCount

    case Mode.LIVE =>
      mode = Mode.LIVE

  }

}
