package com.cctrader.systems.classical.oppositeMACD

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 * Shows how to implement a TSCoordinatorActor.
 *
 * And used for testing of the TSCoordinator trait.
 */
class OppositeMACDCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = OppositeMACDTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeMACDCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeMACDCoordinatorActor(dataActor, tsSettingPath))
}