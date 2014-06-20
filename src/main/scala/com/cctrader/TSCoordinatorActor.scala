package com.cctrader

import java.util.Date

import akka.actor.{ActorSelection, Actor, ActorLogging, ActorRef}
import com.cctrader.data.CurrencyPair.CurrencyPair
import com.cctrader.data.Exchange.Exchange
import com.cctrader.data.Granularity.Granularity
import com.cctrader.data.{DataPoint, MarketDataSet}

/**
 *
 */
trait TSCoordinatorActor extends Actor with ActorLogging {

  val dataAvailable: DataReady
  val dataActor: ActorRef
  val tradingSystemActor: ActorRef
  val backtestStartDate: Date
  val marketDataSettings: MarketDataSettings

  dataActor ! marketDataSettings

  override def receive: Receive = {
    //received data for training
    case marketDataSet: MarketDataSet =>
     log.info("Received marketDataSet: size:" + marketDataSet.size + ", fromDate" + marketDataSet.fromDate
       + ", toDate" + marketDataSet.toDate)
      tradingSystemActor ! marketDataSet

    case "TRAINING DONE" =>
      //ready for run data
  }

}
