package com.cctrader.systems.ann.oneperiodahead

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor
import com.cctrader.data._

/**
 *
 */
class ANNOnePeriodAheadCoordinator(dataActorIn: ActorRef, tsSettingIn: TSSettings) extends {
  val tsSetting: TSSettings = tsSettingIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {

  def tsProps = ANNOnePeriodAheadTS.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSetting)
}

object ANNOnePeriodAheadCoordinator {
  def props(dataActor: ActorRef, tsSetting: TSSettings): Props =
    Props(new ANNOnePeriodAheadCoordinator(dataActor, tsSetting))
}