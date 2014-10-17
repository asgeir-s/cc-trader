package com.cctrader.systems.classical.williamR

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class WilliamRCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = WilliamRTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object WilliamRCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new WilliamRCoordinatorActor(dataActor, tsSettingPath))
}
