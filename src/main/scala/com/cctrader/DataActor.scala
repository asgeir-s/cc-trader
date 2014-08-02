package com.cctrader

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data._
import com.cctrader.dbtables._
import com.typesafe.config.ConfigFactory

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.{ResultSetConcurrency, StaticQuery => Q}

/**
 *
 */
class DataActor extends Actor with ActorLogging {

  val config = ConfigFactory.load()
  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.dbname"),
    driver = config.getString("postgres.driver"),
    user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  implicit val session: Session = databaseFactory.createSession().forParameters(rsConcurrency = ResultSetConcurrency.ReadOnly)

  val tickTable = TableQuery[TickTable]
  val tableMap = Map(
    Granularity.min1 -> TableQuery[Min1Table],
    Granularity.min2 -> TableQuery[Min2Table],
    Granularity.min5 -> TableQuery[Min5Table],
    Granularity.min10 -> TableQuery[Min10Table],
    Granularity.min15 -> TableQuery[Min15Table],
    Granularity.min30 -> TableQuery[Min30Table],
    Granularity.hour1 -> TableQuery[Hour1Table],
    Granularity.hour2 -> TableQuery[Hour2Table],
    Granularity.hour5 -> TableQuery[Hour5Table],
    Granularity.hour12 -> TableQuery[Hour12Table],
    Granularity.day -> TableQuery[DayTable]
  )

  var startTime: Date = {
    val firstRow = tickTable.filter(x => x.id === 1L).take(1)
    val value = firstRow.firstOption map (x => x.date)
    value.get
  }

  var endTime = {
    val lengthString = tickTable.length.run
    val lastRow = tickTable.filter(x => x.id === lengthString.toLong).take(1)
    val value = lastRow.firstOption map (x => x.date)
    value.get
  }

  log.debug("ALL AVAILABLE DATA: startTime:" + startTime + ", endTime:" + endTime)
  context.parent ! DataReady(startTime, endTime)

  def getDataFromDB(marketDataSettings: MarketDataSettings): MarketDataSet = {
    if (marketDataSettings.startDate.before(startTime)) {
      log.error("market data startTime is before startTime in the database. StartTime is: " + marketDataSettings.startDate)
    }
    MarketDataSet(
      tableMap(marketDataSettings.granularity).filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList.reverse,
      marketDataSettings
    )

  }

  override def receive: Receive = {

    case marketDataSettings: MarketDataSettings =>
      log.info("Received: MarketDataSettings: getting data from database and sending back. MarketDataSettings:" + marketDataSettings)
      val thisMarketDataSet = getDataFromDB(marketDataSettings)
      sender ! Initialize(thisMarketDataSet, context.actorOf(LiveDataActor.props(databaseFactory.createSession(), marketDataSettings, thisMarketDataSet.last.id.get)))
      println(thisMarketDataSet)
  }
}

