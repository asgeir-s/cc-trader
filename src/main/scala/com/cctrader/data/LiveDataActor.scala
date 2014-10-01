package com.cctrader.data


import java.sql.Statement

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cctrader.dbtables._
import com.cctrader.{MarketDataSettings, RequestNext}
import com.impossibl.postgres.api.jdbc.{PGConnection, PGNotificationListener}
import com.impossibl.postgres.jdbc.PGDataSource
import com.typesafe.config.ConfigFactory

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q, ResultSetConcurrency, JdbcBackend}


/**
 *
 */
class LiveDataActor(databaseFactory: JdbcBackend.DatabaseDef, marketDataSettings: MarketDataSettings, idStartPoint: Long) extends Actor with ActorLogging {

  implicit var session: Session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)
  var live = false
  var lastPointID: Int = 0
  var idLastSentDP = idStartPoint
  val table = TableQuery[InstrumentTable]((tag:Tag) => new InstrumentTable(tag, marketDataSettings.instrument))

  def getDataSource: PGDataSource = {
    val config = ConfigFactory.load()

    val basicDataSource = new PGDataSource()
    basicDataSource.setPort(config.getString("postgres.port").toInt)
    basicDataSource.setDatabase(config.getString("postgres.dbname"))
    basicDataSource.setUser(config.getString("postgres.user"))
    basicDataSource.setPassword(config.getString("postgres.password"))

    basicDataSource
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
      override def notification(processId: Int, instrument: String, newId: String) {
        session.close()
        session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)
        println("Live data for " + sendTo + " newId:" + newId)
        val numId = {
          if (newId.contains("Some")) {
            newId.substring(newId.lastIndexOf('(') + 1, newId.lastIndexOf(')')).toLong
          }
          else {
            newId.toLong
          }
        }
        println("New entry in the db. instrument:" + instrument + ", newId:" + numId)
        // newId is database id. USe it to retrieve the new row
        val newDataPoint: DataPoint = table.filter(_.id === numId).list.last
        sendTo ! newDataPoint
      }
    })

    val statement: Statement = pgConnection.createStatement()
    statement.addBatch("LISTEN " + marketDataSettings.instrument)
    statement.executeBatch()
    statement.close()
  }

  override def postStop() {
    // clean up some resources ...
    session.close()
  }

  override def receive: Receive = {
    case RequestNext(numOfPoints) =>
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
}

object LiveDataActor {
  def props(databaseFactory: JdbcBackend.DatabaseDef, marketDataSettings: MarketDataSettings, idStartPoint: Long): Props =
    Props(new LiveDataActor(databaseFactory, marketDataSettings, idStartPoint))
}
