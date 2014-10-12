package com.cctrader.systems.ann.vanstoneFinnie

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class VanstoneFinnieCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {
  def tsProps = VanstoneFinnieTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object VanstoneFinnieCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new VanstoneFinnieCoordinatorActor(dataActor, tsSettingPath))
}