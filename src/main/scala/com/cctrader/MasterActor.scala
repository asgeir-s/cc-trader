package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  // paths to the tsSettings files to start tse's for
  val tsSettingsPaths = Array("tsSettings/SuperBitcoinTrader.conf")
  val props = tsSettingsPaths.map(Settings2Props.convert2Props(dataActor, _))
  props.foreach(context.actorOf(_))


  override def receive: Receive = {
    case _ =>
      println("ERROR: the MasterActor should not receive messages!")
  }
}