package com.cctrader.systems.dummy

import java.util.Date

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestActorRef, TestProbe}
import akka.util.Timeout
import com.cctrader._
import com.cctrader.data._

import scala.concurrent.duration._

/**
 *
 */
class DummyCoordinatorActorSpec extends UnitTest {

  val dataActorProbe = TestProbe()
  val tradingSystemProbe = TestProbe()
  val liveDataProbe = TestProbe()

  tradingSystemProbe.setAutoPilot(new TestActor.AutoPilot {
    def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
      sender.tell(TrainingDone(100), sender)
      TestActor.KeepRunning
    }
  })

  val dummyTSCoordinatorActorRef = TestActorRef(new DummyCoordinatorActor(dataActorProbe.ref, DataReady(new Date(1L), new Date(8L))){override def startTradingSystemActor() = {
    tradingSystemProbe.ref
  }})
  val dummyTSCoordinatorActor = dummyTSCoordinatorActorRef.underlyingActor

  implicit val timeout = Timeout(1 second)

  val dataPointList = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(2L).getTime / 1000).toInt, 550D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(3L).getTime / 1000).toInt, 610D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(4L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(5L).getTime / 1000).toInt, 400D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(6L).getTime / 1000).toInt, 410D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(7L).getTime / 1000).toInt, 405D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D)
  )

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(8L),
    numberOfHistoricalPoints = 8,
    granularity = Granularity.min5,
    currencyPair = CurrencyPair.BTC_USD,
    exchange = Exchange.bitstamp,
    PriceChangeScale = 50,
    VolumeChangeScale = 1000,
    MinPrice = 0,
    MaxPrice = 1500,
    MinVolume = 0,
    MaxVolume = 10000
  )

  val marketDataSet = MarketDataSet(dataPointList, marketDataSettings)

  "At start dataActor" should "receive marketDataSettings" in {
    dataActorProbe.expectMsgType[MarketDataSettings]
  }

  "When receiving Initialize message it" should "start a TradingSystemActor" in {
    dataActorProbe.send(dummyTSCoordinatorActorRef, Initialize(marketDataSet, liveDataProbe.ref))
    val result = tradingSystemProbe.expectMsgType[StartTraining]
    result.marketDataSet should equal (marketDataSet)
  }

  "The marketDataSet used for training" should "be a different one then the one in the coordinator" in {
    dataActorProbe.send(dummyTSCoordinatorActorRef, Initialize(marketDataSet, liveDataProbe.ref))
    val result = tradingSystemProbe.expectMsgType[StartTraining]
    result.marketDataSet(0) should equal (marketDataSet(0))
    marketDataSet.addDataPoint(DataPoint(None, None, (new Date(9L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    marketDataSet(0) should not equal (result.marketDataSet(0))
  }

  "When receiving trainingDone for the first time it" should "send RequestLiveBTData to the LiveDataActor" in {
    tradingSystemProbe.send(dummyTSCoordinatorActorRef, TrainingDone(100))
    //dummyTSCoordinatorActor.nextTradingSystem should equal (tradingSystemProbe.ref)
    dummyTSCoordinatorActor.nextSystemReady should be (true)
    val result = tradingSystemProbe.expectMsgType[AkkOn]
    result.numberOfMessagesBeforeAkk should equal (5)
    dummyTSCoordinatorActor.hasRunningTS should be (false)
  }

  "When receiving AkkOn from tradingSystemActor it" should "send new RequestLiveBTData to LiveDataActor" in {
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(9L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    dummyTSCoordinatorActor.messageDPCount should not equal (0)
    tradingSystemProbe.send(dummyTSCoordinatorActorRef, AkkOn(10,0))
    liveDataProbe.expectMsgType[RequestLiveBTData]
    dummyTSCoordinatorActor.messageDPCount should equal (0)
  }

  "When receiving dataPoint it" should "update the time to the time of the new dataPoint" in {
    val dataPointDate = (new Date(8L).getTime / 1000).toInt
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    dummyTSCoordinatorActor.tradingSystemTime.getTime should equal (new Date(dataPointDate).getTime)
  }

  "When receiving Initialize the transferToNextSystemDate time" should "be sett correctly" in {
    dataActorProbe.send(dummyTSCoordinatorActorRef, Initialize(marketDataSet, liveDataProbe.ref))
    val result = tradingSystemProbe.expectMsgType[StartTraining]

  }

  "When receiving dataPoint it" should "send the dataPoint to the runningTradingSystem, if a tradingSystem is running" in {
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(8L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[DataPoint]
  }



  }
