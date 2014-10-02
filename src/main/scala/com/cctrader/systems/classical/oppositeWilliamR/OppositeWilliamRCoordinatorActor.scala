package com.cctrader.systems.classical.oppositeWilliamR

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class OppositeWilliamRCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = OppositeWilliamRTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeWilliamRCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeWilliamRCoordinatorActor(dataActor, tsSettingPath))
}
