package com.cctrader.systems.ann.priceDirection

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class PriceDirectionCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {
  def tsProps = PriceDirectionTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object PriceDirectionCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new PriceDirectionCoordinatorActor(dataActor, tsSettingPath))
}