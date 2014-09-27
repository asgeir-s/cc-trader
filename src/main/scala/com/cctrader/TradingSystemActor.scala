package com.cctrader

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data._
import com.typesafe.config.ConfigFactory

/**
 *
 */
trait TradingSystemActor extends Actor with ActorLogging {
  val signalWriter: Signaler
  var dataPointCountInAkk = 0
  var akkOn = 0
  var mode = Mode.TESTING
  var marketDataSet: MarketDataSet
  val settingPath: String
  val config = ConfigFactory.load(settingPath)
  log.debug("Started: TradingSystemActor")
  val stopPercentage = config.getDouble("thresholds.stopPercentage")
  val thresholdLong = config.getDouble("thresholds.long")
  val thresholdShort = config.getDouble("thresholds.short")
  val thresholdCloseShort = config.getDouble("thresholds.closeShort")
  val thresholdCloseLong = config.getDouble("thresholds.closeLong")

  val continueTrainingInterval = config.getInt("laterTrainingInterval")
  val laterTrainingInterval = config.getInt("laterTrainingSetSize")

  var countDPnow = 0

  /**
   * Train the system.
   * If the system does not need training, return 0
   * @return timestamp in milliseconds for training duration. Timestamp at end of training - start timestamp.
   */
  def train(trainingMarketDataSet: MarketDataSet): Long

  /**
   * Called when a new dataPoint is received. As last in marketDataSet.
   *
   * From this function call goLooong, goShort or goClose to write signals to the signal database.
   */
  def newDataPoint(): Unit

  def goLong: Boolean = {
    if(signalWriter.status.equals(Signal.CLOSE)) {
      signalWriter.newSignal(Signal.LONG, marketDataSet.last)
      true
    }
    else {
      false
    }
  }

  def goShort: Boolean = {
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

  def goCloseStopTestMode(price: Double) = {
    if(!signalWriter.status.equals(Signal.CLOSE)) {
      signalWriter.newSignal(Signal.CLOSE, DataPoint(None, None ,marketDataSet.last.timestamp+1, 0, price, 0, 0, 0))
      true
    }
    else {
      false
    }
  }

  def checkStopPercentage: Unit = {
    if(stopPercentage > 0 && stopPercentage < 100) {
      if (signalWriter.status == Signal.LONG && (marketDataSet.last.low < signalWriter.lastTrade.price * (1 - (stopPercentage / 100)))) {
        goCloseStopTestMode(signalWriter.lastTrade.price * (1 - (stopPercentage / 100)))
      }

      else if (signalWriter.status == Signal.SHORT && (marketDataSet.last.high > signalWriter.lastTrade.price * (1 + (stopPercentage / 100)))) {
        goCloseStopTestMode(signalWriter.lastTrade.price * (1 + (stopPercentage / 100)))
      }
    }
    else {
      println("No stop percentage")
    }
  }

  override def receive: Receive = {
    case StartTraining(marketDataSetIn: MarketDataSet) =>
      log.debug("Received: StartTraining")
      marketDataSet = marketDataSetIn
      val trainingTime = train(marketDataSet)
      sender ! TrainingDone(trainingTime)
      log.debug("Training done")

    case marketDataSetIn: MarketDataSet =>
      log.info("Received new marketDataSet. the old was: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
        + ", toDate" + marketDataSet.toDate)
      marketDataSet = marketDataSetIn
      log.info("NEW (will replace old): size:" + marketDataSetIn.size + ", fromDate" + marketDataSetIn.fromDate
        + ", toDate" + marketDataSetIn.toDate)

    case dataPoint: DataPoint =>
      log.debug("Received DataPoint: time:" + dataPoint.date + ", info:" + dataPoint)
      marketDataSet.addDataPoint(dataPoint)
      log.info("Received new dataPoint. MarketDataSet is now: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
        + ", toDate" + marketDataSet.toDate)
      //this does not work live. Only on test mode. To get the same effect on live trading
      // set a stop/limit order at [currentPrice*(1+stopPercentage)]
      checkStopPercentage
      //evaluate the new dataPoint
      newDataPoint()
      countDPnow+=1
      if(continueTrainingInterval > 0 && countDPnow == continueTrainingInterval) {
        train(marketDataSet.subset(marketDataSet.size - laterTrainingInterval, marketDataSet.size-1))
        countDPnow = 0
      }

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
      signalWriter.goLive

  }

}
