package com.cctrader.systems.ann.fourWayClassify

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class FourWayClassifyCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {
  def tsProps = FourWayClassifyTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object FourWayClassifyCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new FourWayClassifyCoordinatorActor(dataActor, tsSettingPath))
}