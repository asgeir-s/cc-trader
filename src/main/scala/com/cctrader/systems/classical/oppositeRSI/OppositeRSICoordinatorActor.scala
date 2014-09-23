package com.cctrader.systems.classical.oppositeRSI

import akka.actor.{Props, ActorRef}
import com.cctrader.TSCoordinatorActor
import com.cctrader.systems.testDummy.DummyTSActor

/**
 *
 */
class OppositeRSICoordinatorActor (dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
  val numberOfLivePointsAtTheTimeForBackTest = 100
} with TSCoordinatorActor {

  def tsProps = OppositeRSITSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeRSICoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeRSICoordinatorActor(dataActor, tsSettingPath))
}
