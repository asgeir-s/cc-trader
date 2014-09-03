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
  var tradingSystemDate = new Date(1388448000L * 1000L) // summer 2013: 1375228800L 1. januart: 1388448000L
  val numberOfLivePointsAtTheTimeForBackTest = 100
  var transferToNextSystemDate: Date = new Date(0)
  var nextSystemReady: Boolean = false
  val tsNumberOfPointsToProcessBeforeStartTrainingNewSystem = 1 // 1 test depends on this

  val marketDataSettings = MarketDataSettings(
    startDate = tradingSystemDate,
    numberOfHistoricalPoints = 40, //40
    granularity = Granularity.day, // day
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp
  )
} with TSCoordinatorActor {

  val signalWriter = new SignalWriter(name, tsId)

  def tsProps = ANNOnePeriodAheadTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter)

}

object ANNOnePeriodAheadCoordinator {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new ANNOnePeriodAheadCoordinator(dataActor, dataReady))
}
