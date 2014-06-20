package com.cctrader

import akka.actor.{Props, ActorSystem}
import com.cctrader.systems.movingaverage.MovingAverageCoordinatorActor
import com.typesafe.config.ConfigFactory

/**
 * Used to start the actor system
 */

object Boot extends App {
  implicit val system = ActorSystem("actor-system")
  system.actorOf(Props[MasterActor], "master")
}