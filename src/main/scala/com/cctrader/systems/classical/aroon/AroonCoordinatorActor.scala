package com.cctrader.systems.classical.aroon

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class AroonCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = AroonTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object AroonCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new AroonCoordinatorActor(dataActor, tsSettingPath))
}
