package com.cctrader.data


import akka.actor._
import com.cctrader.dbtables._
import com.cctrader.{MarketDataSettings, RequestNext}

import scala.concurrent.duration._
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{JdbcBackend, ResultSetConcurrency, StaticQuery => Q}


/**
 *
 */
class LiveDataActor(databaseFactory: JdbcBackend.DatabaseDef, marketDataSettings: MarketDataSettings, idStartPoint: Long, coordinator: ActorRef) extends Actor with ActorLogging {

  implicit val system = ActorSystem("actor-system-cctrader")
  import system.dispatcher

  implicit val session: Session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)
  var live = false
  var idLastSentDP = idStartPoint
  val table = TableQuery[InstrumentTable]((tag: Tag) => new InstrumentTable(tag, marketDataSettings.instrument))

  def liveData(sendTo: ActorRef) {
    self ! "LOCK FOR NEW DP"
  }

  override def postStop() {
    // clean up some resources ...
    session.close()
  }

  override def receive: Receive = {
    case RequestNext(numOfPoints) => {
      log.debug("Received: RequestNext " + numOfPoints + " dataPoints.")
      val idOfLastDataPoint = table.sortBy(_.id.asc.reverse).take(1).list.last.id.get
      val dataPointsToReturn = table.sortBy(_.id).filter(x => x.id > idLastSentDP && x.id <= (idLastSentDP + numOfPoints)).list
      println("size of DP to return:" + dataPointsToReturn.size)
      dataPointsToReturn.foreach(x => {
        sender ! x
        idLastSentDP = x.id.get
        println("sending:" + x)
        if (x.id.get == idOfLastDataPoint) {
          live = true
        }
      })
      if (live) {
        println("Goes live: instrument: " + marketDataSettings.instrument)
        liveData(sender)
      }
      log.debug("Finished processing: RequestBTData, return size:" + dataPointsToReturn.size)
    }
    case "LOCK FOR NEW DP" => {
      val newDPs = table.filter(_.id > idLastSentDP).sortBy(_.id).list
      newDPs.foreach(x => {
        idLastSentDP = x.id.get
        coordinator ! x
      })
      context.system.scheduler.scheduleOnce(10 seconds, self, "LOCK FOR NEW DP")
    }
  }
}

  object LiveDataActor {
    def props(databaseFactory: JdbcBackend.DatabaseDef, marketDataSettings: MarketDataSettings, idStartPoint: Long, coordinator: ActorRef): Props =
      Props(new LiveDataActor(databaseFactory, marketDataSettings, idStartPoint, coordinator))
  }
