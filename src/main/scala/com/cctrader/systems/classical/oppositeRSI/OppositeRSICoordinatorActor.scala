package com.cctrader.systems.classical.oppositeRSI

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class OppositeRSICoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = OppositeRSITSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeRSICoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeRSICoordinatorActor(dataActor, tsSettingPath))
}
