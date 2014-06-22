package com.cctrader.data

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.{MarketDataSettings, RequestLiveBTData}

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}


/**
 *
 */
class LiveDataActor(sessionIn: Session, marketDataSettings: MarketDataSettings) extends Actor with ActorLogging {

  implicit val session: Session = sessionIn

  var live = false

  var lastPointID: Int = 0

  val table = {
    marketDataSettings.granularity match {
      case Granularity.min1 =>
        TableQuery[Min1Table]

      case Granularity.min2 =>
        TableQuery[Min2Table]

      case Granularity.min5 =>
        TableQuery[Min5Table]

      case Granularity.min10 =>
        TableQuery[Min10Table]

      case Granularity.min15 =>
        TableQuery[Min15Table]

      case Granularity.min30 =>
        TableQuery[Min30Table]

      case Granularity.hour1 =>
        TableQuery[Hour1Table]

      case Granularity.hour2 =>
        TableQuery[Hour2Table]

      case Granularity.hour5 =>
        TableQuery[Hour5Table]

      case Granularity.hour12 =>
        TableQuery[Hour12Table]

      case Granularity.day =>
        TableQuery[DayTable]
    }
  }

  override def receive: Receive = {
    case RequestLiveBTData(tradingSystemTime, numOfPoints) =>
      log.debug("Received: RequestLiveBTData. End time:" + tradingSystemTime)
      val idOfLastDataPoint = table.list.last.id.get
      val dataPointsToReturn = table.filter(_.timestamp >= (tradingSystemTime.getTime / 1000L).toInt).take(numOfPoints).list
      dataPointsToReturn.foreach(x => {
        sender() ! x
        println("sending:" + x)
        if (x.id.get == idOfLastDataPoint) {
          sender() ! Mode.LIVE
          live = true
          // LIVE
          log.info("WE GO LIVE!")
          while (true) {
            //check for new dataPoints and send them to sender
          }
        }
      })
      log.debug("Finished processing: RequestLiveBTData, return size:" + dataPointsToReturn.size)

  }
}

object LiveDataActor {
  def props(sessionIn: Session, marketDataSettings: MarketDataSettings): Props =
    Props(new LiveDataActor(sessionIn, marketDataSettings))
}
