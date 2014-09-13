package com.cctrader.systems.ann.oneperiodahead

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor
import com.cctrader.data._

/**
 *
 */
class ANNOnePeriodAheadCoordinator(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath = tsSettingPathIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {
  def tsProps = ANNOnePeriodAheadTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)
}

object ANNOnePeriodAheadCoordinator {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new ANNOnePeriodAheadCoordinator(dataActor, tsSettingPath))
}