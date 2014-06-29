package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.ann.oneperiodahead.ANNOnePeriodAheadCoordinator
import com.cctrader.systems.dummy.DummyCoordinatorActor

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  def startTradingSystems(dataReady: DataReady): Unit = {
    //all TSCoordinators to run should be listed here
    //val dummyCoordinatorActor = context.actorOf( DummyCoordinatorActor.props(dataActor, dataReady), "Dummy")
    val annOnePeriodAhead = context.actorOf( ANNOnePeriodAheadCoordinator.props(dataActor, dataReady), "ANNOnePeriodAhead")

  }

  override def receive: Receive = {
    case dataReady: DataReady =>
      startTradingSystems(dataReady)
  }
}
