package com.cctrader.systems.ann.recurrentForwardIndicator

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class RecurrentCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {
  def tsProps = RecurrentForwardIndicatorTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object RecurrentCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new RecurrentCoordinatorActor(dataActor, tsSettingPath))
}