package com.cctrader.systems.dummy

import java.util.Date

import akka.actor.{PoisonPill, ActorRef}
import akka.testkit.{TestActor, TestActorRef, TestProbe}
import akka.util.Timeout
import com.cctrader._
import com.cctrader.data.Mode.Mode
import com.cctrader.data._

import scala.concurrent.duration._

/**
 *
 */
class DummyCoordinatorActorSpec extends UnitTest {

  val dataActorProbe = TestProbe()
  val tradingSystemProbe = TestProbe()
  val nextTradingSystemProbe = TestProbe()
  val thirdTradingSystemProbe = TestProbe()
  val liveDataProbe = TestProbe()

  tradingSystemProbe.setAutoPilot(new TestActor.AutoPilot {
    def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = msg match {
      case startTraining: StartTraining =>
        sender.tell(TrainingDone(100L * 1000L), sender)
        TestActor.KeepRunning

      case _ =>
        TestActor.KeepRunning
    }
  })

  nextTradingSystemProbe.setAutoPilot(new TestActor.AutoPilot {
    def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = msg match {
      case startTraining: StartTraining =>
        sender.tell(TrainingDone(100L * 1000L), sender)
        TestActor.KeepRunning

      case _ =>
        TestActor.KeepRunning
    }
  })

  thirdTradingSystemProbe.setAutoPilot(new TestActor.AutoPilot {
    def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = msg match {
      case startTraining: StartTraining =>
        sender.tell(TrainingDone(100L * 1000L), sender)
        TestActor.KeepRunning

      case _ =>
        TestActor.KeepRunning
    }
  })

  var first = true
  var second = false
  val dummyTSCoordinatorActorRef = TestActorRef(new DummyCoordinatorActor(dataActorProbe.ref, DataReady(new Date(1L), new Date(8L))) {
    override def startTradingSystemActor =
    if (first) {
      first = false
      second = true
      tradingSystemProbe.ref
    }
    else if (second) {
      second = false
      nextTradingSystemProbe.ref
    }
    else {
      thirdTradingSystemProbe.ref
    }
  })
  val dummyTSCoordinatorActor = dummyTSCoordinatorActorRef.underlyingActor

  implicit val timeout = Timeout(1 second)

  val dataPointList = List(
    DataPoint(None, None, 1339539810, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539812, 550D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539813, 610D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539814, 500D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539815, 400D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539816, 410D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539817, 405D, 5D, 5D, 5D, 50D),
    DataPoint(None, None, 1339539818, 500D, 5D, 5D, 5D, 50D)
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

  "When receiving Initialize message it" should "start a TradingSystemActor and sett transferToNextSystemDate" in {
    dummyTSCoordinatorActor.hasRunningTS should be(false)
    dataActorProbe.send(dummyTSCoordinatorActorRef, Initialize(marketDataSet, liveDataProbe.ref))
    val result = tradingSystemProbe.expectMsgType[StartTraining]
    result.marketDataSet should equal(marketDataSet)
    dummyTSCoordinatorActor.transferToNextSystemDate.getTime should equal(new Date(1339539816L * 1000L).getTime + (100L * 1000L))
    dummyTSCoordinatorActor.nextTradingSystem should equal(tradingSystemProbe.ref)
    dummyTSCoordinatorActor.nextSystemReady should be(true)
  }

  "The liveDataActor" should "receive RequestLiveBTData" in {
    liveDataProbe.expectMsgType[RequestLiveBTData]
  }

  "The marketDataSet used for training" should "be a different one then the one in the coordinator" in {
    dummyTSCoordinatorActor.marketDataSet(0) should equal(marketDataSet(0))
    marketDataSet.addDataPoint(DataPoint(None, None, 1339539819, 50D, 5D, 50D, 5D, 500D))
    marketDataSet(0) should not equal (dummyTSCoordinatorActor.marketDataSet(0))
  }

  "When receiving AkkOn from tradingSystemActor it" should "send new RequestLiveBTData to LiveDataActor" in {
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, 1339539820, 500D, 5D, 5D, 5D, 50D))
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, 1339539821, 500D, 5D, 5D, 5D, 50D))
    dummyTSCoordinatorActor.messageDPCount should not equal (0)
    tradingSystemProbe.send(dummyTSCoordinatorActorRef, AkkOn(10, 0))
    liveDataProbe.expectMsgType[RequestLiveBTData]
    dummyTSCoordinatorActor.messageDPCount should equal(0)
  }

  "When receiving dataPoint it" should "update the time to the time of the new dataPoint" in {
    val dataPointDate = (new Date(8L).getTime / 1000).toInt
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, 1339539822, 500D, 5D, 5D, 5D, 50D))
    dummyTSCoordinatorActor.tradingSystemTime.getTime should equal(new Date(1339539822 * 1000L).getTime)
  }

  "When receiving first dataPoints in testing-mode it" should
    "not send the dataPoints to the new system, before the datapoint-time is after transfer time" in {
    val timeNow = dummyTSCoordinatorActor.tradingSystemTime.getTime
    val timeForTransfer = dummyTSCoordinatorActor.tradingSystemTime.getTime + (100L * 1000L)
    //dummyTSCoordinatorActor.hasRunningTS should be (false)
    var dp = DataPoint(None, None, (timeNow / 1000).toInt, 500D, 5D, 5D, 5D, 50D)
    assert(dp.date.before(new Date(timeForTransfer)))
    dummyTSCoordinatorActor.mode should be(Mode.TESTING)
    liveDataProbe.send(dummyTSCoordinatorActorRef, dp)
    tradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339539824L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339539825L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339539900L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339539999L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[AkkOn]
    tradingSystemProbe.expectMsgType[MarketDataSet]
    tradingSystemProbe.expectMsgType[DataPoint]
  }



  "When in testing-mode: dataPoints" should
    "be sent to the old tradingSystemActor until time is after transferTime. Then the dataPoints should be sent to the new system and the old system should get PoisonPill" in {
    // already got one dataPoint in last test
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339539999L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[DataPoint]
    nextTradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540000L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[DataPoint]
    nextTradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540100L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[DataPoint]
    nextTradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540200L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    tradingSystemProbe.expectMsgType[DataPoint]
    nextTradingSystemProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540300L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    nextTradingSystemProbe.expectMsgType[StartTraining]
    liveDataProbe.expectNoMsg(1 second)
    nextTradingSystemProbe.expectNoMsg(1 second)
    tradingSystemProbe.expectMsgType[DataPoint]
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540350L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    nextTradingSystemProbe.expectNoMsg(1 second)
    tradingSystemProbe.expectMsgType[DataPoint]
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540370L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    nextTradingSystemProbe.expectNoMsg(1 second)
    tradingSystemProbe.expectMsgType[DataPoint]
    // this data point is after change to nextTradingSystem
    dummyTSCoordinatorActor.hasRunningTS should be (true)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540410L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    nextTradingSystemProbe.expectMsgType[AkkOn]
    nextTradingSystemProbe.expectMsgType[MarketDataSet]
    nextTradingSystemProbe.expectMsgType[DataPoint]
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540450L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    nextTradingSystemProbe.expectMsgType[DataPoint]

  }

  "When in live mode: dataPoints" should
    "be sent directly to the new system at once. And the old system should get PoisonPill" in {
    liveDataProbe.send(dummyTSCoordinatorActorRef, Mode.LIVE)
    nextTradingSystemProbe.expectMsgType[Mode]
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540451L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540452L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540453L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540454L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))

    // time to start training nextTradingSystem
    thirdTradingSystemProbe.expectMsgType[StartTraining]
    thirdTradingSystemProbe.expectMsgType[Mode]
    liveDataProbe.expectNoMsg(1 second)
    liveDataProbe.send(dummyTSCoordinatorActorRef, DataPoint(None, None, (new Date(1339540455L * 1000L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    thirdTradingSystemProbe.expectMsgType[AkkOn]
    thirdTradingSystemProbe.expectMsgType[MarketDataSet]
    thirdTradingSystemProbe.expectMsgType[DataPoint]
  }


}
