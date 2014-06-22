package com.cctrader

import java.util.{Date, UUID}

import akka.actor._
import com.cctrader.StartTraining
import com.cctrader.data._

import scala.concurrent.Await

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/**
 *
 */
trait TSCoordinatorActor extends Actor with ActorLogging {

  val id = UUID.randomUUID()
  val name: String
  val dataAvailable: DataReady
  val dataActor: ActorRef
  var tradingSystemActor: ActorRef
  var nextTradingSystem: ActorRef
  var tradingSystemTime: Date
  val marketDataSettings: MarketDataSettings
  var mode = Mode.TESTING
  var marketDataSet: MarketDataSet = null
  var liveDataActor: ActorRef = _
  val numberOfLivePointsAtTheTimeForBackTest: Int
  val signalWriter: SignalWriterTrait
  var transferToNextSystemDate: Date
  var nextSystemReady: Boolean
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem: Int // this * numberOfLivePointsAtTheTimeForBackTest is the number of dataPoints processed before a new system is started
  var numberOfPointsProcessedByCurrentSystem = 0
  var hasRunningTS = false
  var messageDPCount = 0

  dataActor ! marketDataSettings

  def tsProps: Props

  implicit val timeout = Timeout(10 minutes)

  def startTradingSystemActor: ActorRef = context.actorOf(tsProps, "trading-system")
  
  def startAndTrainNewSystem() {
    // starting a new system
    nextTradingSystem = startTradingSystemActor
    if (mode == Mode.TESTING) {
      //wait for training to complete
      val askFuture = nextTradingSystem ? StartTraining(MarketDataSet(marketDataSet.iterator.toList, marketDataSet.settings.copy()))
      val trainingDone = Await.result(askFuture, timeout.duration).asInstanceOf[TrainingDone]

      transferToNextSystemDate = new Date(tradingSystemTime.getTime + trainingDone.trainingTimeInMilliSec) //cant use the data collected during training. (Before the TS was ready)
      nextSystemReady = true
    }
    else {
      // send training data to the new system
      nextTradingSystem ! marketDataSet
      nextTradingSystem ! Mode.LIVE
    }

  }

  override def receive: Receive = {
    //received data for training. First time
    case init: Initialize =>
      marketDataSet = init.marketDataSet
      liveDataActor = init.liveDataActorRef
      startAndTrainNewSystem()
      log.info("Received marketDataSet: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
        + ", toDate" + marketDataSet.toDate)

    case trainingDone: TrainingDone =>
      // some system is finished with training and ready to start trading
      nextTradingSystem = sender()
      nextSystemReady = true
     
      if (mode == Mode.TESTING) {
        nextTradingSystem ! AkkOn(numberOfLivePointsAtTheTimeForBackTest, messageDPCount)
      }
      
      if (!hasRunningTS) {
        if (mode == Mode.TESTING) {
          liveDataActor ! RequestLiveBTData(transferToNextSystemDate, numberOfLivePointsAtTheTimeForBackTest)
        }
        else {
          liveDataActor ! RequestLiveData(transferToNextSystemDate)
        }
      }

    case newDataPoint: DataPoint =>
      log.debug("Received: newDataPoint")
      messageDPCount = messageDPCount + 1
      tradingSystemTime = newDataPoint.date
      marketDataSet.addDataPoint(newDataPoint)
      numberOfPointsProcessedByCurrentSystem = numberOfPointsProcessedByCurrentSystem + 1
      if (nextSystemReady && newDataPoint.date.after(transferToNextSystemDate)) {
        tradingSystemActor ! PoisonPill
        tradingSystemActor = nextTradingSystem
        tradingSystemActor ! marketDataSet
        numberOfPointsProcessedByCurrentSystem = 0
        nextSystemReady = false
        hasRunningTS = true
      }
      if (numberOfPointsProcessedByCurrentSystem >= tsNumberOfPointsToProcessBeforeStartTrainingNewSystem) {
        startAndTrainNewSystem()
      }
      if (hasRunningTS) {
        tradingSystemActor ! newDataPoint
      }
      
    case Mode.LIVE =>
      mode = Mode.LIVE
      tradingSystemActor ! Mode.LIVE

    case akk: AkkOn =>
      log.debug("Received: AkkOn")
      messageDPCount = 0
      liveDataActor ! RequestLiveBTData(tradingSystemTime, numberOfLivePointsAtTheTimeForBackTest)
  }

}
