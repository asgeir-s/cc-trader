package com.cctrader

import akka.actor.ActorSystem

/**
 *
 */
abstract class ItTest extends TestKit(ActorSystem("test")) with FunSuiteLike with Matchers with
OptionValues with Inside with Inspectors with BeforeAndAfterAll {

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }


}
