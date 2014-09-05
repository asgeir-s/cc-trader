package com.cctrader.systems.dummy

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor
import com.cctrader.data._

/**
 * Shows how to implement a TSCoordinatorActor.
 *
 * And used for testing of the TSCoordinator trait.
 */
class DummyCoordinatorActor(dataActorIn: ActorRef, tsSettingIn: TSSettings) extends {
  val tsSetting: TSSettings = tsSettingIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {

  def tsProps = DummyTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSetting)

}

object DummyCoordinatorActor {
  def props(dataActor: ActorRef, tsSetting: TSSettings): Props =
    Props(new DummyCoordinatorActor(dataActor, tsSetting))
}