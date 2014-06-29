package com.cctrader.data


import java.sql.Statement

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cctrader.{MarketDataSettings, RequestLiveBTData}
import com.impossibl.postgres.api.jdbc.{PGConnection, PGNotificationListener}
import com.impossibl.postgres.jdbc.PGDataSource
import com.typesafe.config.ConfigFactory

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

  def getDataSource: PGDataSource = {
    val config = ConfigFactory.load()

    val basicDataSource = new PGDataSource();
    basicDataSource.setPort(config.getString("postgres.port").toInt)
    basicDataSource.setDatabase(config.getString("postgres.dbname"))
    basicDataSource.setUser(config.getString("postgres.user"))
    basicDataSource.setPassword(config.getString("postgres.password"))

    basicDataSource;
  }

  def liveData(sendTo: ActorRef) {
    sendTo ! Mode.LIVE
    log.info("WE GO LIVE!")

    //listen for new dataPoints
    // send new dataPoints to sendTo
    val dataSource: PGDataSource = getDataSource
    val pgConnection: PGConnection = dataSource.getConnection.asInstanceOf[PGConnection]

    pgConnection.addNotificationListener(new PGNotificationListener() {
      @Override
      override def notification(processId: Int, granularity: String, newId: String) {
        println("New entry in the db. granularity:" + granularity + ", newId:" + newId)
        // newId is database id. USe it to retrieve the new row
        val newDataPoint: DataPoint = table.filter(_.id === newId.toLong).list.last
        sendTo ! newDataPoint
      }
    })

    val statement: Statement = pgConnection.createStatement()
    statement.addBatch("LISTEN " + marketDataSettings.granularity.toString)
    statement.executeBatch()
    statement.close()
  }

  override def receive: Receive = {
    case RequestLiveBTData(tradingSystemTime, numOfPoints) =>
      log.debug("Received: RequestLiveBTData. End time:" + tradingSystemTime)
      val idOfLastDataPoint = table.list.last.id.get
      val dataPointsToReturn = table.filter(_.timestamp >= (tradingSystemTime.getTime / 1000L).toInt).take(numOfPoints).list
      dataPointsToReturn.foreach(x => {
        sender ! x
        println("sending:" + x)
        if (x.id.get == idOfLastDataPoint) {
          live = true
        }
      })
      if (live) {
        liveData(sender)
      }
      log.debug("Finished processing: RequestLiveBTData, return size:" + dataPointsToReturn.size)

  }
}

object LiveDataActor {
  def props(sessionIn: Session, marketDataSettings: MarketDataSettings): Props =
    Props(new LiveDataActor(sessionIn, marketDataSettings))
}
