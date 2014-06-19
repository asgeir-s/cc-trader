package com.cctrader

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.data._
import com.typesafe.config.{Config, ConfigFactory}

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.{StaticQuery => Q, ResultSetConcurrency}

/**
 *
 */
class DataActor() extends Actor with ActorLogging {

  val config = ConfigFactory.load()


  val tsCoordinators = context.actorSelection("../*")

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

  log.debug("Data is ready, startTime:" + startTime + ", endTime:" + endTime)
  tsCoordinators ! DataReady(startTime, endTime)

  def getDataFromDB(requestedData: RequestData): MarketDataSet = {
    var minClose = Double.MaxValue
    var maxClose = 0D
    var minVolume = Double.MaxValue
    var maxVolume = 0D

    if (requestedData.granularity == Granularity.min1) {
      val min1List = min1Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min1List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }
    else if (requestedData.granularity == Granularity.min2) {
      val min2List = min2Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min2List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }
    else if (requestedData.granularity == Granularity.min5) {
      val min5List = min5Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min5List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.min10) {
      val min10List = min10Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min10List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.min15) {
      val min15List = min15Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min15List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.min30) {
      val min30List = min30Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(min30List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.hour1) {
      val hour1List = hour1Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(hour1List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }
    else if (requestedData.granularity == Granularity.hour2) {
      val hour2List = hour2Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(hour2List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.hour5) {
      val hour5List = hour5Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(hour5List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }
    else if (requestedData.granularity == Granularity.hour12) {
      val hour12List = hour12Table.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(hour12List, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else if (requestedData.granularity == Granularity.day) {
      val dayList = dayTable.filter(_.timestamp <= (requestedData.dateNow.getTime /
        1000).toInt).list.reverse.take(requestedData.numberOfHistoricalPoints).map(x => {
        if (x.low < minClose) {
          minClose = x.low
        }
        if (x.high > maxClose) {
          maxClose = x.high
        }
        if (x.volume < minVolume) {
          minVolume = x.volume
        }
        if (x.volume > maxVolume) {
          maxVolume = x.volume
        }
        x
      }).toList
      MarketDataSet(dayList, requestedData.granularity, requestedData.currencyPair,
        requestedData.exchange, minClose, maxClose, minVolume, maxVolume)
    }

    else {
      log.error("Granularity do not exist.")
      null
    }
  }

  override def receive: Receive = {

    case request: RequestData =>
      sender ! getDataFromDB(request)

    case "RequestLiveData" =>
      log.error("Retrieved RequestLiveData. But its not implemented.")

    case DataReady(newStartTime: Date, newEndTime: Date) =>
      if(!sender().equals(self)) {
        startTime = newStartTime
        endTime = newEndTime
        log.debug("New data is ready, startTime:" + startTime + ", endTime:" + endTime)
        "tsManagerActor ! DataReady(startTime, endTime)"
      }
  }
}

