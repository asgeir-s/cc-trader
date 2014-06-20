package com.cctrader

import java.util.Date

import akka.actor.ActorRef
import com.cctrader.data.CurrencyPair._
import com.cctrader.data.DataPoint
import com.cctrader.data.Exchange._
import com.cctrader.data.Granularity.Granularity

/**
 *
 * @param startDate date for the requested MarketDataSet to end
 * @param numberOfHistoricalPoints number of points to retrieve and keep in MarketDataSet during run (the maximum number of points needed for training)
 * @param granularity granularity of the MarketDataSet
 * @param currencyPair currency pair of the MarketDataSet
 * @param exchange exchange of the MarketDataSet
 * @param PriceChangeScale the number to davide the price change by before putting it in the sigmoid function (the maximum change between to points ever). Can be 50 - 100, size depends on granularity.
 * @param VolumeChangeScale the number to davide the volume change by before putting it in the sigmoid function (the maximum change between to points ever). Size depends on granularity.
 * @param MinPrice min price ever in the MarketDataSet
 * @param MaxPrice max price ever in the MarketDataSet
 * @param MinVolume min volume ever in the MarketDataSet
 * @param MaxVolume max volume ever in the MarketDataSet
 */
case class MarketDataSettings(
                               startDate: Date,
                               numberOfHistoricalPoints: Int,
                               granularity: Granularity,
                               currencyPair: CurrencyPair,
                               exchange: Exchange,
                               PriceChangeScale: Int, // should be ca. 50 - 100 (the maximum of change between two points)
                               VolumeChangeScale: Int,
                               MinPrice: Double,
                               MaxPrice: Double,
                               MinVolume: Double,
                               MaxVolume: Double
                               )

case class DataReady(fromDate: Date, toDate: Date)

case class TrainingDone(trainingTime: Int)
