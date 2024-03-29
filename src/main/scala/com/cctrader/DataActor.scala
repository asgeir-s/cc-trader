package com.cctrader

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data._
import com.cctrader.dbtables._
import com.typesafe.config.ConfigFactory

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.{JdbcBackend, ResultSetConcurrency, StaticQuery => Q}

/**
 *
 */
class DataActor extends Actor with ActorLogging {

  val config = ConfigFactory.load()
  val databaseFactory: JdbcBackend.DatabaseDef = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.dbname"),
    driver = config.getString("postgres.driver"),
    user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  implicit val session: Session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)

  def getDataFromDB(marketDataSettings: MarketDataSettings): MarketDataSet = {
    val table = TableQuery[InstrumentTable]((tag:Tag) => new InstrumentTable(tag, marketDataSettings.instrument))

    val startTime: Date = {
      val idOfMin = table.map(_.id).min
      val firstOption = table.filter(_.id === idOfMin).firstOption
      firstOption.get.date
    }

    val endTime = {
      val idOfMax = table.map(_.id).max
      val firstOption = table.filter(_.id === idOfMax).firstOption
      firstOption.get.date
    }

    log.info("For instrument " + marketDataSettings.instrument + ": startTime:" + startTime + ", endTime:" + endTime)

    if (marketDataSettings.startDate.before(startTime)) {
      log.error("Market data startTime is before startTime in the database. StartTime is: " + marketDataSettings.startDate + " and start time in DB is: " + startTime)
    }
    MarketDataSet(
      table.filter(_.timestamp <= (marketDataSettings.startDate.getTime/1000).toInt).sortBy(_.id).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList.reverse,
      marketDataSettings
    )

  }

  override def postStop() {
    // clean up some resources ...
    session.close()
  }

  override def receive: Receive = {
    case marketDataSettings: MarketDataSettings =>
      log.info("Received: MarketDataSettings: getting data from database and sending back for MarketDataSettings:" + marketDataSettings)
      val thisMarketDataSet = getDataFromDB(marketDataSettings)
      sender ! Initialize(thisMarketDataSet, context.actorOf(LiveDataActor.props(databaseFactory,  marketDataSettings, thisMarketDataSet.last.id.get, sender)))
      println(thisMarketDataSet)
  }
}

