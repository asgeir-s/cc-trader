package com.cctrader.systems.classical.oppositeStochastic

import akka.actor.{ActorRef, Props}
import com.cctrader.TSCoordinatorActor

/**
 *
 */
class OppositeStochasticCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  /**
   * Should define the TradingSystemActors props.
   * Ex: DummyTSActor.props(MarketDataSet(marketDataSet.iterator.toList, marketDataSet.settings), signalWriter)
   * @return the props to use when creating an instance of the tradingSystemActor
   */
  override def tsProps: Props = OppositeStochasticTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object OppositeStochasticCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new OppositeStochasticCoordinatorActor(dataActor, tsSettingPath))
}
