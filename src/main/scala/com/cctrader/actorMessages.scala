package com.cctrader

import java.util.Date

import akka.actor.ActorRef
import com.cctrader.data.CurrencyPair._
import com.cctrader.data.Exchange._
import com.cctrader.data.Granularity.Granularity
import com.cctrader.data.MarketDataSet

/**
 *  TODO: should include instrument: bitcoin, APPL-stock etc.
 *
 * @param startDate date for the requested MarketDataSet to end
 * @param numberOfHistoricalPoints number of points to retrieve and keep in MarketDataSet during run (the maximum number of points needed for training)
 * @param granularity granularity of the MarketDataSet
 * @param currencyPair currency pair of the MarketDataSet
 * @param exchange exchange of the MarketDataSet
 */
case class MarketDataSettings(
                               startDate: Date,
                               numberOfHistoricalPoints: Int,
                               granularity: Granularity,
                               currencyPair: CurrencyPair,
                               exchange: Exchange
                               )

case class DataReady(fromDate: Date, toDate: Date)

case class TrainingDone(trainingTimeInMilliSec: Long)

case class RequestLiveData(fromDate: Date)

case class RequestNext(numOfPoints: Int)

case class AkkOn(numberOfMessagesBeforeAkk: Int, initialCount: Int)

/**
 *
 * @param marketDataSet
 * @param liveDataActorRef
 */
case class Initialize(marketDataSet: MarketDataSet, liveDataActorRef: ActorRef)

case class StartTraining(marketDataSet: MarketDataSet)