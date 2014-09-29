package com.cctrader

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.systems.ann.forwardIndicator.ForwardIndicatorsCoordinatorActor
import com.cctrader.systems.ann.recurrentForwardIndicator.RecurrentCoordinatorActor
import com.cctrader.systems.classical.oppositeAroon.OppositeAroonCoordinatorActor
import com.cctrader.systems.classical.oppositeDisparity.OppositeDisparityCoordinatorActor
import com.cctrader.systems.classical.oppositeROC.OppositeROCCoordinatorActor
import com.cctrader.systems.classical.oppositeStochastic.OppositeStochasticCoordinatorActor
import com.cctrader.systems.classical.oppositeWilliamR.OppositeWilliamRCoordinatorActor
import com.cctrader.systems.classical.roc.ROCCoordinatorActor
import com.cctrader.systems.classical.williamR.WilliamRCoordinatorActor
import com.cctrader.systems.classical.rsi.RSICoordinatorActor
import com.cctrader.systems.classical.aroon.AroonCoordinatorActor
import com.cctrader.systems.classical.disparity.DisparityCoordinatorActor
import com.cctrader.systems.classical.macd.MACDCoordinatorActor
import com.cctrader.systems.classical.oppositeMACD.OppositeMACDCoordinatorActor
import com.cctrader.systems.classical.oppositeRSI.OppositeRSICoordinatorActor
import com.cctrader.systems.classical.stochastic.StochasticCoordinatorActor
import com.cctrader.systems.testDummy.DummyCoordinatorActor
import com.typesafe.config.ConfigFactory

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val config = ConfigFactory.load()
  val tableName = config.getString("instrumentTable")
  val granularity = tableName.substring(tableName.lastIndexOf('_'))

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  //ANN
  //context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann/forwardIndicator/aroon/Aroon" + granularity +".conf"))
  //context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann/forwardIndicator/disparity/Disparity" + granularity +".conf"))
  //context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann/forwardIndicator/ROC/ROC" + granularity +".conf"))
  //context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann/forwardIndicator/williamsR/WilliamsR" + granularity +".conf"))


  //Classical
  //context.actorOf(StochasticCoordinatorActor.props(dataActor, "tsSettings/classical/stochastic/Stochastic" + granularity +".conf"))
  //context.actorOf(RSICoordinatorActor.props(dataActor, "tsSettings/classical/RSI/RSI" + granularity +".conf"))
  //context.actorOf(ROCCoordinatorActor.props(dataActor, "tsSettings/classical/ROC/ROC" + granularity +".conf"))
  //context.actorOf(WilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/WilliamR/WilliamR" + granularity +".conf"))
  //context.actorOf(DisparityCoordinatorActor.props(dataActor, "tsSettings/classical/Disparity/Disparity" + granularity +".conf"))
  //context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical/Aroon/Aroon" + granularity +".conf"))
  //context.actorOf(MACDCoordinatorActor.props(dataActor, "tsSettings/classical/MACD/MACD" + granularity +".conf"))


  //Classical Opposite
  //context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical/oppositeRSI/OppositeRSI" + granularity +".conf"))
  //context.actorOf(OppositeMACDCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeMACD/OppositeMACD" + granularity +".conf"))
  //context.actorOf(OppositeROCCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeROC/OppositeROC" + granularity +".conf"))
  //context.actorOf(OppositeWilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeWilliamR/OppositeWilliamR" + granularity +".conf"))
  //context.actorOf(OppositeDisparityCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeDisparity/OppositeDisparity" + granularity +".conf"))
  //context.actorOf(OppositeAroonCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeAroon/OppositeAroon" + granularity +".conf"))
  //context.actorOf(OppositeStochasticCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeStochastic/OppositeStochastic" + granularity +".conf"))


  //2hour - TOP SYSTEMS
  context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical/oppositeRSI/OppositeRSI_2hour.conf"))
  context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical/Aroon/Aroon_2hour.conf"))


  //6hour - TOP SYSTEMS
  context.actorOf(ROCCoordinatorActor.props(dataActor, "tsSettings/classical/ROC/ROC_6hour.conf"))
  context.actorOf(WilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/WilliamR/WilliamR_6hour.conf"))
  context.actorOf(DisparityCoordinatorActor.props(dataActor, "tsSettings/classical/Disparity/Disparity_6hour.conf"))
  context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical/Aroon/Aroon_6hour.conf"))
  context.actorOf(OppositeMACDCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeMACD/OppositeMACD_6hour.conf"))


  //12hour - TOP SYSTEMS
  context.actorOf(ROCCoordinatorActor.props(dataActor, "tsSettings/classical/ROC/ROC_12hour.conf"))
  context.actorOf(WilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/WilliamR/WilliamR_12hour.conf"))
  context.actorOf(DisparityCoordinatorActor.props(dataActor, "tsSettings/classical/Disparity/Disparity_12hour.conf"))
  context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical/Aroon/Aroon_12hour.conf"))
  context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical/oppositeRSI/OppositeRSI_12hour.conf"))
  context.actorOf(OppositeMACDCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeMACD/OppositeMACD_12hour.conf"))

  override def receive: Receive = {
    case _ =>
      println("ERROR: the MasterActor should not receive messages!")
  }
}