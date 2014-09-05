package com.cctrader

import java.util.Date

import akka.actor.ActorRef
import com.cctrader.data.MarketDataSet

/**
 *  TODO: should include instrument: bitcoin, APPL-stock etc.
 *
 * @param startDate date for the requested MarketDataSet to end
 * @param numberOfHistoricalPoints number of points to retrieve and keep in MarketDataSet during run (the maximum number of points needed for training)
 * @param instrument also name of table in the database (including granularity)
 */
case class MarketDataSettings(
                               startDate: Date,
                               numberOfHistoricalPoints: Int,
                               instrument: String
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