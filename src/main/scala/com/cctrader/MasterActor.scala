package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.movingaverage.MovingAverageCoordinatorActor

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  def startTradingSystems(dataReady: DataReady): Unit = {
    //all TSCoordinators to run should be listed here
    val tsMovingAverageActor = context.actorOf(
      MovingAverageCoordinatorActor.props(dataActor, dataReady),
      "MovingAverage"
    )
  }

  override def receive: Receive = {
    case dataReady: DataReady =>
      startTradingSystems(dataReady)
  }
}
