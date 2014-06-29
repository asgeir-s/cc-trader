package com.cctrader.systems.ann.oneperiodahead

import java.util.Date

import akka.actor.{Props, ActorRef}
import com.cctrader.data._
import com.cctrader.systems.dummy.{DummyTSActor, SignalWriter}
import com.cctrader.{MarketDataSettings, DataReady, TSCoordinatorActor}

/**
 *
 */
class ANNOnePeriodAheadCoordinator(dataActorIn: ActorRef, dataAvailableIn: DataReady) extends {
  val name = "ANNOnePeriodAhead"
  val dataAvailable = dataAvailableIn
  val dataActor = dataActorIn
  var tradingSystemTime = new Date(1403155416L * 1000L)
  val numberOfLivePointsAtTheTimeForBackTest = 100
  var transferToNextSystemDate: Date = new Date(0)
  val sigmoidNormalizerScale = 20
  var nextSystemReady: Boolean = false
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem = 24 // test depends on this
  val signalWriter = new SignalWriter(name + "trades")

  val marketDataSettings = MarketDataSettings(
    startDate = tradingSystemTime,
    numberOfHistoricalPoints = 60,
    granularity = Granularity.min30,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp,
    PriceChangeScale = 100,
    VolumeChangeScale = 1000,
    MinPrice = 0,
    MaxPrice = 1500,
    MinVolume = 0,
    MaxVolume = 1000000
  )
} with TSCoordinatorActor {

  def tsProps = ANNOnePeriodAheadTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter)

}

object ANNOnePeriodAheadCoordinator {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new ANNOnePeriodAheadCoordinator(dataActor, dataReady))
}

