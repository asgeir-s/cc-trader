package com.cctrader.systems.dummy

import java.util.Date

import akka.actor.{Props, ActorRef}
import com.cctrader.data.{MarketDataSet, Exchange, CurrencyPair, Granularity}
import com.cctrader.{Initialize, TSCoordinatorActor, DataReady, MarketDataSettings}

/**
 *
 */
class DummyCoordinatorActor(dataActorIn: ActorRef, dataAvailableIn: DataReady) extends {
  val name = "Dummy"
  val dataAvailable = dataAvailableIn
  val dataActor = dataActorIn
  var tradingSystemTime = new Date(1339539816L * 1000L)
  val numberOfLivePointsAtTheTimeForBackTest = 5
  var transferToNextSystemDate: Date = new Date(0)
  val sigmoidNormalizerScale = 100
  var nextSystemReady: Boolean = false
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem = 10

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(8L),
    numberOfHistoricalPoints = 8,
    granularity = Granularity.min5,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp,
    PriceChangeScale = 50,
    VolumeChangeScale = 1000,
    MinPrice = 0,
    MaxPrice = 1500,
    MinVolume = 0,
    MaxVolume = 10000
  )

} with TSCoordinatorActor {

  def tsProps = DummyTSActor.props(marketDataSet, signalWriter)

  var tradingSystemActor: ActorRef = _
  val signalWriter = new SignalWriter(name + "trades")
  var nextTradingSystem: ActorRef = _
}

object DummyCoordinatorActor {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new DummyCoordinatorActor(dataActor, dataReady))
}