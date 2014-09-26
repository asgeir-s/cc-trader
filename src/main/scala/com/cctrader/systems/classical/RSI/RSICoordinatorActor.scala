package com.cctrader.systems.classical.rsi

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class RSICoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = RSITSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object RSICoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new RSICoordinatorActor(dataActor, tsSettingPath))
}
