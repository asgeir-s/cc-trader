package com.cctrader

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest._

/**
 * All unit tests should extend this.
 */
abstract class UnitTest extends TestKit(ActorSystem("test")) with FunSuiteLike with Matchers with
OptionValues with Inside with Inspectors with BeforeAndAfterAll {

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}
