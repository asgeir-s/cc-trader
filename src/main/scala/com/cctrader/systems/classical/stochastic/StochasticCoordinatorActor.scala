package com.cctrader.systems.classical.stochastic

import akka.actor.{Props, ActorRef}
import com.cctrader.TSCoordinatorActor
import com.cctrader.systems.classical.oppositeRSI.OppositeRSITSActor

/**
 *
 */
class StochasticCoordinatorActor(dataActorIn: ActorRef, tsSettingPathIn: String) extends {
  val tsSettingPath: String = tsSettingPathIn
  val dataActor = dataActorIn
} with TSCoordinatorActor {

  /**
   * Should define the TradingSystemActors props.
   * Ex: DummyTSActor.props(MarketDataSet(marketDataSet.iterator.toList, marketDataSet.settings), signalWriter)
   * @return the props to use when creating an instance of the tradingSystemActor
   */
  override def tsProps: Props = StochasticTSActor.props(newCopyOfMarketDataSet(marketDataSet), signalWriter, tsSettingPath)

}

object StochasticCoordinatorActor {
  def props(dataActor: ActorRef, tsSettingPath: String): Props =
    Props(new StochasticCoordinatorActor(dataActor, tsSettingPath))
}
