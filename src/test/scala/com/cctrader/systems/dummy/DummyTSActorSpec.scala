package com.cctrader.systems.dummy

import java.util.Date

import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import com.cctrader._
import com.cctrader.data._

import scala.concurrent.duration._

/**
 *
 */
class DummyTSActorSpec extends UnitTest {

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

  val dataPointList2 = List(
    DataPoint(None, None, (new Date(1L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D)
  )

  val marketDataSettings = MarketDataSettings(
    startDate = new Date(8L),
    numberOfHistoricalPoints = 8,
    instrument = "bitstamp_btc_usd_5min"
  )

  val marketDataSettings2 = MarketDataSettings(
    startDate = new Date(1L),
    numberOfHistoricalPoints = 1,
    instrument = "bitstamp_btc_usd_5min"
  )

  val marketDataSet = MarketDataSet(dataPointList, marketDataSettings)
  val marketDataSet2 = MarketDataSet(dataPointList2, marketDataSettings2)

  val tsCoordinatorProbe = TestProbe()

  val dummyTSActorRef = TestActorRef(new DummyTSActor(marketDataSet, new Signaler("DummyTest", 99), "tsSettings/test/DummyTest1.conf"))
  val dummyTSActor = dummyTSActorRef.underlyingActor

  "When the actor receive StartTraining message it" should "train the actor" in {
    tsCoordinatorProbe.send(dummyTSActorRef, StartTraining(marketDataSet))
    val trainingTime = tsCoordinatorProbe.expectMsgType[TrainingDone]
    trainingTime.trainingTimeInMilliSec should equal(100L * 1000L)
  }

  "When receiving marketDataSett training" should "not start" in {
    tsCoordinatorProbe.send(dummyTSActorRef, marketDataSet)
    tsCoordinatorProbe.expectNoMsg(1 second)
  }

  it should "switch the old marketDataSet with the new" in {
    dummyTSActor.marketDataSet should equal(marketDataSet)
    tsCoordinatorProbe.send(dummyTSActorRef, marketDataSet2)
    dummyTSActor.marketDataSet should equal(marketDataSet2)
  }

  "When receiving dataPoint after training is done it" should "increase the dataPoinCount" in {
    dummyTSActorRef ! AkkOn(10, 0)
    dummyTSActor.dataPointCountInAkk should equal(0)
    tsCoordinatorProbe.send(dummyTSActorRef, DataPoint(None, None, (new Date(9L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    dummyTSActor.dataPointCountInAkk should equal(1)
  }

  "When dataPointCountInAkk is equal to akkOn it" should "send AkkOn message" in {
    dummyTSActorRef ! AkkOn(10, 9)
    dummyTSActor.dataPointCountInAkk should equal(9)
    tsCoordinatorProbe.send(dummyTSActorRef, DataPoint(None, None, (new Date(10L).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    val akk = tsCoordinatorProbe.expectMsgType[AkkOn]
    akk.numberOfMessagesBeforeAkk should equal(10)
    dummyTSActor.dataPointCountInAkk should equal(0)
  }

  it should "send Akk after each 10th new dataPoint" in {
    for (time <- 11 to 60) {
      tsCoordinatorProbe.send(dummyTSActorRef, DataPoint(None, None, (new Date(time).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    }
    tsCoordinatorProbe.receiveN(5, 3 seconds)
  }

  it should "not send Akks if mode is LIVE" in {
    dummyTSActorRef ! Mode.LIVE
    for (time <- 11 to 60) {
      tsCoordinatorProbe.send(dummyTSActorRef, DataPoint(None, None, (new Date(time).getTime / 1000).toInt, 500D, 5D, 5D, 5D, 50D))
    }
    tsCoordinatorProbe.expectNoMsg(1 second)
  }

}
