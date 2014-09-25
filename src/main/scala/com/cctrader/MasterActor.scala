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

/**
 *
 */
class MasterActor extends Actor with ActorLogging {

  val dataActor = context.actorOf(Props[DataActor], "dataActor")

  //all TSCoordinators to run should be listed here
  //val dummyCoordinatorActor = context.actorOf( DummyCoordinatorActor.props(dataActor, dataReady), "Dummy")
  //val annOnePeriodAhead = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "stock_tsla_daily"), "ANNOnePeriodAhead1") // bitstamp_btc_usd_day

  //val annRecurrentOnePeriodAhead = context.actorOf(RecurrentCoordinatorActor.props(dataActor, "tsSettings/Recurrent_Hour1.conf"))
  //val annOnePeriodAheadHour = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Hour1.conf"))
  //val oppositeANNDay1 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day2.conf"))
  //val oppositeANNDay2 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day3.conf"))
  //val oppositeANNDay3 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day4.conf"))
  //val oppositeANNDay4 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/ann.forwardIndicator/OppositeRSI_Day5.conf"))

  //Classical Day
  //context.actorOf(StochasticCoordinatorActor.props(dataActor, "tsSettings/classical/stochastic/Stochastic_Day.conf"))
  //context.actorOf(RSICoordinatorActor.props(dataActor, "tsSettings/classical/RSI/RSI_Day.conf"))
  //context.actorOf(ROCCoordinatorActor.props(dataActor, "tsSettings/classical/ROC/ROC_Day.conf"))
  //context.actorOf(WilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/WilliamR/WilliamR_Day.conf"))
  //context.actorOf(DisparityCoordinatorActor.props(dataActor, "tsSettings/classical/Disparity/Disparity_Day.conf"))
  //context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical/Aroon/Aroon_Day.conf"))
  //context.actorOf(MACDCoordinatorActor.props(dataActor, "tsSettings/classical/MACD/MACD_Day.conf"))

  //Classical Opposite Day
  context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical/oppositeRSI/OppositeRSI_Day.conf"))
  context.actorOf(OppositeMACDCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeMACD/OppositeMACD_Day.conf"))
  //context.actorOf(OppositeROCCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeROC/OppositeROC_Day.conf"))
  //context.actorOf(OppositeWilliamRCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeWilliamR/OppositeWilliamR_Day.conf"))
  //context.actorOf(OppositeDisparityCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeDisparity/OppositeDisparity_Day.conf"))
  //context.actorOf(OppositeAroonCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeAroon/OppositeAroon_Day.conf"))
  //context.actorOf(OppositeStochasticCoordinatorActor.props(dataActor, "tsSettings/classical/oppositeStochastic/OppositeStochastic_Day.conf"))


  //Classical Hour
  //val OppositeRSIHour = context.actorOf(OppositeRSICoordinatorActor.props(dataActor, "tsSettings/classical.oppositeRSI/OppositeRSI_Hour.conf"))
  //val stochasticHour = context.actorOf(StochasticOscillatorCoordinatorActor.props(dataActor, "tsSettings/classical.stochasticOscillator/Stochastic_Hour.conf"))
  //val RSIHour = context.actorOf(RSICoordinatorActor.props(dataActor, "tsSettings/classical.RSI/RSI_Hour.conf")) //WTF!!
  //val ROC1Hour = context.actorOf(ROCCoordinatorActor.props(dataActor, "tsSettings/classical.ROC/ROC_Hour.conf"))
  //val williamRHour = context.actorOf(WilliamRCoordinatorActor.props(dataActor, "tsSettings/classical.WilliamR/WilliamR_Hour.conf"))
  //val disparityHour = context.actorOf(DisparityCoordinatorActor.props(dataActor, "tsSettings/classical.Disparity/Disparity_Hour.conf"))
  //val aroonHour = context.actorOf(AroonCoordinatorActor.props(dataActor, "tsSettings/classical.Aroon/Aroon_Hour.conf"))
  //val macdHour = context.actorOf(MACDCoordinatorActor.props(dataActor, "tsSettings/classical.MACD/MACD_Hour.conf"))
  //val oppositeMACDHour = context.actorOf(OppositeMACDCoordinatorActor.props(dataActor, "tsSettings/classical.oppositeMACD/OppositeMACD_Hour.conf"))





  //val annOnePeriodAhead1 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "tsSettings/OppositeRSI_Hour1.conf"))
  //val annOnePeriodAhead3 = context.actorOf(ForwardIndicatorsCoordinatorActor.props(dataActor, "stock_appl_daily"), "ANNOnePeriodAhead3")
  //val mAECCoordinatorActor = context.actorOf( MAECCoordinatorActor.props(dataActor, dataReady), "MAEC")

  override def receive: Receive = {
    case _ =>
      println("ERROR: the MasterActor should not receive messages!")
  }
}