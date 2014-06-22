package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.dummy.DummyCoordinatorActor

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  def startTradingSystems(dataReady: DataReady): Unit = {
    //all TSCoordinators to run should be listed here
    val tsMovingAverageActor = context.actorOf(
      DummyCoordinatorActor.props(dataActor, dataReady),
      "Dummy"
    )
  }

  override def receive: Receive = {
    case dataReady: DataReady =>
      startTradingSystems(dataReady)
  }
}