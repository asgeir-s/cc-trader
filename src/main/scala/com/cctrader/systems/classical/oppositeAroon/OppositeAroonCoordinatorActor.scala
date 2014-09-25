package com.cctrader.systems.classical.oppositeAroon

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class OppositeAroonCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = OppositeAroonTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeAroonCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeAroonCoordinatorActor(dataActor, tsSettingPath))
}
