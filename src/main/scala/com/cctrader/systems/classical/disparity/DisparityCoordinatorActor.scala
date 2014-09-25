package com.cctrader.systems.classical.disparity

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class DisparityCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = DisparityTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object DisparityCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new DisparityCoordinatorActor(dataActor, tsSettingPath))
}
