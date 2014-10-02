package com.cctrader

import akka.actor.{ActorSystem, Props}

/**
 * Used to start the actor system
 */

object Boot extends App {
  implicit val system = ActorSystem("actor-system-cctrader")
  system.actorOf(Props[MasterActor], "master")
}