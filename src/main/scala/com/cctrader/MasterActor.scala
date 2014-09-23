package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.ann.forwardIndicator.ForwardIndicatorsCoordinatorActor
import com.cctrader.systems.ann.recurrentForwardIndicator.RecurrentCoordinatorActor
import com.cctrader.systems.classical.oppositeRSI.OppositeRSICoordinatorActor
import com.cctrader.systems.testDummy.DummyCoordinatorActor
import com.cctrader.systems.classical.macd.MAECCoordinatorActor

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  //all TSCoordinators to run should be listed here
  //val dummyCoordinatorActor = context.actorOf( DummyCoordinatorActor.props(dataActor, dataReady), "Dummy")
  //val annOnePeriodAhead = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "stock_tsla_daily"), "ANNOnePeriodAhead1") // bitstamp_btc_usd_day

  //val annRecurrentOnePeriodAhead = context.actorOf(RecurrentCoordinatorActor.props(dataActor, "tsSettings/Recurrent_Hour1.conf"))
  //val annOnePeriodAheadHour = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/OppositeRSI_Hour1.conf"))
  val oppositeANNDay1 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day2.conf"))
  //val oppositeANNDay2 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day3.conf"))
  //val oppositeANNDay3 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day4.conf"))
  //val oppositeANNDay4 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day5.conf"))


  val directOpositRSIDay = context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical.oppositeRSI/OppositeRSI_Day1.conf"))



  //val annOnePeriodAhead1 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/OppositeRSI_Hour1.conf"))
  //val annOnePeriodAhead3 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "stock_appl_daily"), "ANNOnePeriodAhead3")
  //val mAECCoordinatorActor = context.actorOf( MAECCoordinatorActor.props(dataActor, dataReady), "MAEC")

  override def receive: Receive = {
    case _ =>
      println("ERROR: the MasterActor should not receive messages!")
  }
}