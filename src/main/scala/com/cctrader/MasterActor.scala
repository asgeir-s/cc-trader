package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.ann.oneperiodahead.ANNOnePeriodAheadCoordinator
import com.cctrader.systems.ann.recurrent.ANNRecurrentCoordinator
import com.cctrader.systems.directRSI.DirectRSICoordinatorActor
import com.cctrader.systems.dummy.DummyCoordinatorActor
import com.cctrader.systems.movingAverageConvergens.MAECCoordinatorActor

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  //all TSCoordinators to run should be listed here
  //val dummyCoordinatorActor = context.actorOf( DummyCoordinatorActor.props(dataActor, dataReady), "Dummy")
  //val annOnePeriodAhead = context.actorOf(ANNOnePeriodAheadCoordinator.props(dataActor, "stock_tsla_daily"), "ANNOnePeriodAhead1") // bitstamp_btc_usd_day

  //val annRecurrentOnePeriodAhead = context.actorOf(ANNRecurrentCoordinator.props(dataActor, "tsSettings/RecurrentBTCHour.conf"))
  //val annOnePeriodAheadHour = context.actorOf(ANNOnePeriodAheadCoordinator.props(dataActor, "tsSettings/SuperBitcoinTraderHour.conf"))
  val annOnePeriodAheadDay = context.actorOf(ANNOnePeriodAheadCoordinator.props(dataActor, "tsSettings/SuperBitcoinTraderDay.conf"))
  //val directRSIDay = context.actorOf(DirectRSICoordinatorActor.props(dataActor, "tsSettings/DirectRSIDay.conf"))



  //val annOnePeriodAhead1 = context.actorOf(ANNOnePeriodAheadCoordinator.props(dataActor, "tsSettings/SuperBitcoinTraderHour.conf"))
  //val annOnePeriodAhead3 = context.actorOf(ANNOnePeriodAheadCoordinator.props(dataActor, "stock_appl_daily"), "ANNOnePeriodAhead3")
  //val mAECCoordinatorActor = context.actorOf( MAECCoordinatorActor.props(dataActor, dataReady), "MAEC")

  override def receive: Receive = {
    case _ =>
      println("ERROR: the MasterActor should not receive messages!")
  }
}