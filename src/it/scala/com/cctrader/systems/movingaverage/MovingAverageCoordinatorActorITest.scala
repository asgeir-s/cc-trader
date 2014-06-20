package com.cctrader.systems.movingaverage

import java.util.Date

import akka.testkit.{TestActorRef, TestProbe}
import com.cctrader._

/**
 *
 */
class MovingAverageCoordinatorActorITest extends ItTest {
  val testProbe = TestProbe()
  val dataActorProbe = TestProbe()
  val dataActorRef = TestActorRef[MovingAverageCoordinatorActor]
  //val actor = dataActorRef.underlyingActor

  testProbe.expectMsgType[MarketDataSettings]
}
