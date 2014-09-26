package com.cctrader.systems.classical.roc

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class ROCCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = ROCTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object ROCCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new ROCCoordinatorActor(dataActor, tsSettingPath))
}
