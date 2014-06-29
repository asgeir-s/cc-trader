package com.cctrader.systems.dummy

import java.util.Date

import akka.actor.{ActorRef, Props}
import com.cctrader.data.{MarketDataSet, CurrencyPair, Exchange, Granularity}
import com.cctrader.{DataReady, MarketDataSettings, TSCoordinatorActor}

/**
 * Shows how to implement a TSCoordinatorActor.
 *
 * And used for testing of the TSCoordinator trait.
 */
class DummyCoordinatorActor(dataActorIn: ActorRef, dataAvailableIn: DataReady) extends {
  val name = "Dummy"
  val dataAvailable = dataAvailableIn
  val dataActor = dataActorIn
  var tradingSystemTime = new Date(1339539816L * 1000L)
  val numberOfLivePointsAtTheTimeForBackTest = 100
  var transferToNextSystemDate: Date = new Date(0)
  val sigmoidNormalizerScale = 20
  var nextSystemReady: Boolean = false
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem = 5 // test depends on this
  val signalWriter = new SignalWriter(name + "trades")

  val marketDataSettings = MarketDataSettings(
    startDate = tradingSystemTime,
    numberOfHistoricalPoints = 100,
    granularity = Granularity.day,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp,
    PriceChangeScale = 50,
    VolumeChangeScale = 1000,
    MinPrice = 0,
    MaxPrice = 1500,
    MinVolume = 0,
    MaxVolume = 1000000
  )
} with TSCoordinatorActor {

  def tsProps = DummyTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter)

}

object DummyCoordinatorActor {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new DummyCoordinatorActor(dataActor, dataReady))
}