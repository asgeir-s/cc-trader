package com.cctrader.systems.ann.oneperiodahead

import java.util.Date

import akka.actor.{ActorRef, Props}
import com.cctrader.data._
import com.cctrader.{DataReady, MarketDataSettings, TSCoordinatorActor}

/**
 *
 */
class ANNOnePeriodAheadCoordinator(dataActorIn: ActorRef, dataAvailableIn: DataReady) extends {
  val name = "ANNOnePeriodAhead"
  val dataAvailable = dataAvailableIn
  val dataActor = dataActorIn
  var tradingSystemDate = new Date(1403155416L * 1000L)
  val numberOfLivePointsAtTheTimeForBackTest = 100
  var transferToNextSystemDate: Date = new Date(0)
  val sigmoidNormalizerScale = 20
  var nextSystemReady: Boolean = false
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem = 24 // test depends on this

  val marketDataSettings = MarketDataSettings(
    startDate = tradingSystemDate,
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

  val signalWriter = new SignalWriter(name, tsId)

  def tsProps = ANNOnePeriodAheadTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter)

}

object ANNOnePeriodAheadCoordinator {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new ANNOnePeriodAheadCoordinator(dataActor, dataReady))
}
