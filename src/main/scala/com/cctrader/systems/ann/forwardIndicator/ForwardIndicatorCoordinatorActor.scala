package com.cctrader.systems.ann.forwardIndicator

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor
import com.cctrader.data._

/**
 *
 */
class ForwardIndicatorsCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {
  def tsProps = ForwardIndicatorsTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object ForwardIndicatorsCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new ForwardIndicatorsCoordinatorActor(dataActor, tsSettingPath))
}