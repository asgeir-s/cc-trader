package com.cctrader.systems.ann.fuzzy

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class FuzzyClassifyCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {
  def tsProps = FuzzyClassifyTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object FuzzyClassifyCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new FuzzyClassifyCoordinatorActor(dataActor, tsSettingPath))
}