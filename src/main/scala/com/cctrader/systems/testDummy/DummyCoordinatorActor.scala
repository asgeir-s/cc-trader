package com.cctrader.systems.testDummy

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 * Shows how to implement a TSCoordinatorActor.
 *
 * And used for testing of the TSCoordinator trait.
 */
class DummyCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  def tsProps = DummyTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object DummyCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new DummyCoordinatorActor(dataActor, tsSettingPath))
}