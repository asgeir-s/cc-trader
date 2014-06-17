package com.cctrader

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/**
 * Used to start the actor system
 */

object Boot extends App {

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")


  // ActorSystem to host our application in
  implicit val system = ActorSystem("actor-system")

  val dataActor = system.actorOf(DataActor.props, "dataActor")

}