package com.cctrader.systems.movingaverage

import java.util.Date

import akka.testkit.{TestActorRef, TestProbe}
import com.cctrader.{DataReady, MarketDataSettings, UnitTest}

/**
 *
 */
class MovingAverageCoordinatorActorSpec extends UnitTest {
  val dataActorProbe = TestProbe()
  val movingAverageCoordinatorActor = TestActorRef(
    new MovingAverageCoordinatorActor(dataActorProbe.ref, DataReady(new Date(0), new Date()))
  )
  //val actor = dataActorRef.underlyingActor

  dataActorProbe.expectMsgType[MarketDataSettings]
}
