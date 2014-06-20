package com.cctrader

import java.util.Date

import akka.actor.{Actor, ActorLogging}
import com.cctrader.data._
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
  val min1Table = TableQuery[Min1Table]
  val min2Table = TableQuery[Min2Table]
  val min5Table = TableQuery[Min5Table]
  val min10Table = TableQuery[Min10Table]
  val min15Table = TableQuery[Min15Table]
  val min30Table = TableQuery[Min30Table]
  val hour1Table = TableQuery[Hour1Table]
  val hour2Table = TableQuery[Hour2Table]
  val hour5Table = TableQuery[Hour5Table]
  val hour12Table = TableQuery[Hour12Table]
  val dayTable = TableQuery[DayTable]

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
    var minClose = Double.MaxValue
    var maxClose = 0D
    var minVolume = Double.MaxValue
    var maxVolume = 0D

    if (marketDataSettings.granularity == Granularity.min1) {
      val min1List = min1Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min1List, marketDataSettings)
    }
    else if (marketDataSettings.granularity == Granularity.min2) {
      val min2List = min2Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min2List, marketDataSettings)
    }
    else if (marketDataSettings.granularity == Granularity.min5) {
      val min5List = min5Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min5List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.min10) {
      val min10List = min10Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min10List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.min15) {
      val min15List = min15Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min15List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.min30) {
      val min30List = min30Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(min30List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.hour1) {
      val hour1List = hour1Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(hour1List, marketDataSettings)
    }
    else if (marketDataSettings.granularity == Granularity.hour2) {
      val hour2List = hour2Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(hour2List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.hour5) {
      val hour5List = hour5Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(hour5List, marketDataSettings)
    }
    else if (marketDataSettings.granularity == Granularity.hour12) {
      val hour12List = hour12Table.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(hour12List, marketDataSettings)
    }

    else if (marketDataSettings.granularity == Granularity.day) {
      val dayList = dayTable.filter(_.timestamp <= (marketDataSettings.startDate.getTime /
        1000).toInt).list.reverse.take(marketDataSettings.numberOfHistoricalPoints).toList
      MarketDataSet(dayList, marketDataSettings)
    }

    else {
      log.error("Granularity do not exist.")
      null
    }
  }

  override def receive: Receive = {

    case marketDataSettings: MarketDataSettings =>
      log.info("Received: MarketDataSettings: getting data from database and sending back. MarketDataSettings:" + marketDataSettings)
      sender ! getDataFromDB(marketDataSettings)

    case "RequestLiveData" =>
      log.error("Retrieved RequestLiveData. But its not implemented.")

  }
}

