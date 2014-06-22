package com.cctrader

import akka.actor.{ActorSystem, Props}

/**
 * Used to start the actor system
 */

object Boot extends App {
  implicit val system = ActorSystem("actor-system")
  system.actorOf(Props[MasterActor], "master")
}