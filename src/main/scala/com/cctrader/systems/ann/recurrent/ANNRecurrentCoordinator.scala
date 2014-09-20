package com.cctrader.systems.ann.recurrent

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class ANNRecurrentCoordinator(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {
  def tsProps = ANNRecurrentTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object ANNRecurrentCoordinator {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new ANNRecurrentCoordinator(dataActor, tsSettingPath))
}