package com.cctrader.systems.movingaverage

import java.util.Date

import akka.actor.{ActorRef, Props}
import com.cctrader.data.{CurrencyPair, Exchange, Granularity}
import com.cctrader.{DataReady, MarketDataSettings, TSCoordinatorActor}

/**
 *
 */
class MovingAverageCoordinatorActor(dataActorIn: ActorRef, dataAvailableIn: DataReady) extends {
  val dataAvailable = dataAvailableIn
  val dataActor = dataActorIn
  val backtestStartDate = new Date(1339539816 * 1000)
  val marketDataSettings = MarketDataSettings(
    startDate = backtestStartDate,
    numberOfHistoricalPoints = 100,
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

  val tradingSystemActor = context.actorOf(Props[MovingAverageTSActor], "trading-system")
  val sigmoidNormalizerScale = 100
}

object MovingAverageCoordinatorActor {
  def props(dataActor: ActorRef, dataReady: DataReady): Props =
    Props(new MovingAverageCoordinatorActor(dataActor, dataReady))
}