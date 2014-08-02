package com.cctrader

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data.Signal.Signal
import com.cctrader.data._

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
   * Called when a new dataPoint is received. As last in marketDataSet.
   *
   * From this function call goLooong, goShort or goClose to write signals to the signal database.
   */
  def newDataPoint(): Unit

  def goLoong: Boolean = {
    if(signalWriter.status.equals(Signal.CLOSE)) {
      signalWriter.newSignal(Signal.LOONG, marketDataSet.last)
      true
    }
    else {
      false
    }
  }

  def goShorte: Boolean = {
    if(signalWriter.status.equals(Signal.CLOSE)) {
      signalWriter.newSignal(Signal.SHORT, marketDataSet.last)
      true
    }
    else {
      false
    }
  }

  def goClose: Boolean = {
    if(!signalWriter.status.equals(Signal.CLOSE)) {
      signalWriter.newSignal(Signal.CLOSE, marketDataSet.last)
      true
    }
    else {
      false
    }
  }


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
      newDataPoint()

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
