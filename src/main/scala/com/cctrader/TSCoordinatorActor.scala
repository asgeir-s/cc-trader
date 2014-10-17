package com.cctrader

import java.util.{Date, UUID}

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.cctrader.data._
import com.cctrader.dbtables.{TSInfo, TSTable}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
trait TSCoordinatorActor extends Actor with ActorLogging {

  val tsSettingPath: String
  val config = ConfigFactory.load(tsSettingPath)
  val dBConfig = ConfigFactory.load()
  val id = UUID.randomUUID()
  val dataActor: ActorRef
  var tradingSystemActor: ActorRef = _
  var nextTradingSystem: ActorRef = _
  var mode = Mode.TESTING
  var marketDataSet: MarketDataSet = null
  var liveDataActor: ActorRef = _
  val numberOfLivePointsAtTheTimeForBackTest: Int = 100 //HARD CODED!
  var transferToNextSystemDate: Date = new Date(0)
  var numberOfPointsProcessedByCurrentSystem = 0
  var hasRunningTS = false
  var messageDPCount = 0
  var countTradingSystemsUsed = 0
  val appConfig = ConfigFactory.load()
  var tradingSystemDate = new Date(appConfig.getLong("startUnixTime") * 1000L) // summer 2013: 1375228800L 1. januart: 1388448000L * 1000L år 2000: 946684800
  var nextSystemReady: Boolean = false
  val name = config.getString("name")
  val numberOfPredictionsPerTS = config.getInt("numberOfPredictionsBeforeNewTSActor")
  val instrument = appConfig.getString("instrumentTable")
  val granularity = tsSettingPath.substring(tsSettingPath.lastIndexOf('_'), tsSettingPath.lastIndexOf('.'))
  val tableName = instrument + granularity
  var lastDPsID = -1

  val marketDataSettings = MarketDataSettings(
    startDate = tradingSystemDate,
    numberOfHistoricalPoints = config.getInt("initialtrainingSetSize"), //40
    tableName
  )

  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + dBConfig.getString("postgres.host") + ":" + dBConfig.getString("postgres.port") + "/" + dBConfig
      .getString("postgres.trader.dbname"),
    driver = dBConfig.getString("postgres.driver"),
    user = dBConfig.getString("postgres.user"),
    password = dBConfig.getString("postgres.password"))

  implicit val session = databaseFactory.createSession()

  val tsTable = TableQuery[TSTable]
  if (!makeTableMap.contains("tsinfo")) {
    tsTable.ddl.create
  }

  //tsTable.filter(p => p.name === name).delete
  //tsTable += TSInfo(None, name, (marketDataSettings.startDate.getTime / 1000L).toInt, marketDataSettings.numberOfHistoricalPoints, marketDataSettings.granularity.toString, marketDataSettings.currencyPair.toString, marketDataSettings.exchange.toString)
  val dbTsId = (tsTable returning tsTable.map(_.id)) += TSInfo(None, name, (marketDataSettings.startDate.getTime / 1000L).toInt, marketDataSettings.instrument)

  val tsId: Long = dbTsId.get
  val signalWriter = new Signaler(config.getString("name"), tsId)

  dataActor ! marketDataSettings

  /**
   * Should define the TradingSystemActors props.
   * Ex: DummyTSActor.props(MarketDataSet(marketDataSet.iterator.toList, marketDataSet.settings), signalWriter)
   * @return the props to use when creating an instance of the tradingSystemActor
   */
  def tsProps: Props

  implicit val timeout = Timeout(10 minutes)

  def makeTableMap: Map[String, MTable] = {
    val tableList = MTable.getTables.list(session)
    val tableMap = tableList.map { t => (t.name.name, t)}.toMap
    tableMap
  }

  def startTradingSystemActor: ActorRef = context.actorOf(tsProps, name + "-ts-" + countTradingSystemsUsed)

  def newCopyOfMarketDataSet(setToCopy: MarketDataSet): MarketDataSet = {
    MarketDataSet(setToCopy.getList.map(x => new DataPoint(x.id, x.sourceId, x.timestamp, x.open, x.close, x.low, x.high, x.volume)).toList, marketDataSettings)

  }

  def startAndTrainNewSystem(marketDataSetForTraining: MarketDataSet) {
    countTradingSystemsUsed = countTradingSystemsUsed + 1
    // starting a new system
    log.debug("Start and train, nextTradingSystem actor")
    nextTradingSystem = startTradingSystemActor
    if (mode == Mode.TESTING) {
      //wait for training to complete
      val askFuture = nextTradingSystem ? StartTraining(marketDataSetForTraining)
      val trainingDone = Await.result(askFuture, timeout.duration).asInstanceOf[TrainingDone]

      transferToNextSystemDate = new Date(tradingSystemDate.getTime + trainingDone.trainingTimeInMilliSec) //cant use the data collected during training. (Before the TS was ready)
      nextSystemReady = true
    }
    else {
      // LIVE
      nextTradingSystem ! StartTraining(marketDataSetForTraining)
      nextTradingSystem ! Mode.LIVE
    }
    // if this is the first system
    if (!hasRunningTS) {
      if (mode == Mode.TESTING) {
        //tradingSystemActor = nextTradingSystem
        liveDataActor ! RequestNext(numberOfLivePointsAtTheTimeForBackTest)
      }
      else {
        liveDataActor ! RequestLiveData(tradingSystemDate)
      }
    }
  }

  override def postStop() {
    // clean up some resources ...
    session.close()
  }

  override def receive: Receive = {
    //received data for training. First time
    case init: Initialize =>
      marketDataSet = newCopyOfMarketDataSet(init.marketDataSet)
      liveDataActor = init.liveDataActorRef
      log.info("Received Initialize message with marketDataSet: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
        + ", toDate" + marketDataSet.toDate)
      startAndTrainNewSystem(newCopyOfMarketDataSet(init.marketDataSet))
      tradingSystemDate = marketDataSet.toDate

    case trainingDone: TrainingDone =>
      // some system is finished with training and ready to start trading
      // this is only used when mode is LIVE, else the startAndTrainNewSystem is awaiting this message
      nextSystemReady = true

    case newDataPoint: DataPoint =>
      log.debug("Received: newDataPoint id:" + newDataPoint.id.get.toInt)
      if (tradingSystemDate.after(newDataPoint.date) || newDataPoint.id.get.toInt == lastDPsID) {
        throw new Exception("The *new* dataPoint is older then the last. Last:" + tradingSystemDate + ", this:" + newDataPoint.date)
      }
      lastDPsID = newDataPoint.id.get.toInt
      // start using nextSystem and kill old
      if (nextSystemReady && (mode == Mode.LIVE || newDataPoint.date.after(transferToNextSystemDate))) {
        if (hasRunningTS) {
          log.debug("Sending PoisonPill to (current) tradingSystem")
          import akka.pattern.gracefulStop
          val stopped: Future[Boolean] = gracefulStop(tradingSystemActor, 60 seconds)
          Await.result(stopped, 80 seconds)
          //tradingSystemActor ! PoisonPill
        }
        log.debug("Transferring to nextTradingSystemActor")
        tradingSystemActor = nextTradingSystem
        tradingSystemActor ! AkkOn(numberOfLivePointsAtTheTimeForBackTest, messageDPCount)
        tradingSystemActor ! newCopyOfMarketDataSet(marketDataSet)
        numberOfPointsProcessedByCurrentSystem = 0
        nextSystemReady = false
        hasRunningTS = true
      }

      messageDPCount = messageDPCount + 1
      marketDataSet.addDataPoint(newDataPoint)
      tradingSystemDate = newDataPoint.date
      println(marketDataSet)

      if (hasRunningTS) {
        numberOfPointsProcessedByCurrentSystem = numberOfPointsProcessedByCurrentSystem + 1
        tradingSystemActor ! newDataPoint
      }
      if (numberOfPredictionsPerTS > 0 && numberOfPointsProcessedByCurrentSystem == numberOfPredictionsPerTS) {
        log.debug("Starts a new tradingSystem")
        startAndTrainNewSystem(newCopyOfMarketDataSet(marketDataSet))
      }

    case Mode.LIVE =>
      mode = Mode.LIVE
      tradingSystemActor ! Mode.LIVE

    case akk: AkkOn =>
      log.debug("Received: AkkOn")
      messageDPCount = 0
      liveDataActor ! RequestNext(numberOfLivePointsAtTheTimeForBackTest) // denne tiden er ikke nødvendigvis riktig. LiveData burde heller bare sende ut de neste n dp

  }

}