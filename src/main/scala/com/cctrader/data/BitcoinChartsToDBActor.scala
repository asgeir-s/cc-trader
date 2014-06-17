package com.cctrader.data

import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.Date

import akka.actor.{Actor, ActorLogging, Props}
import com.cctrader.DataReady
import com.cctrader.data.CurrencyPair.CurrencyPair
import com.typesafe.config.ConfigFactory

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Downloads full history from Bitcoin Charts and writes it to the database.
 *
 * UNIX: requires /usr/bin/gzip command.
 */
class BitcoinChartsToDBActor(download: Boolean, decompress: Boolean,
                             writeToDB: Boolean, createGranularityTabs: Boolean,
                             csvFilPath: String, implicit var session: Session) extends Actor with
ActorLogging {
  var currencyPair: CurrencyPair = CurrencyPair.BTC_USD
  val config = ConfigFactory.load()
  val bitcoinchartsURL = new URL("http://api.bitcoincharts.com/v1/csv/bitstampUSD.csv.gz")
  val compressedHistoryFile = "download/bitstampUSD" + new java.util.Date().getTime + ".csv.gz"
  val decompressedHistoryFile = {
    if (csvFilPath.length == 0)
      compressedHistoryFile.substring(0, compressedHistoryFile.length - 3)
    else
      csvFilPath
  }

  if (download) {
    log.debug("Download start")
    val connection = bitcoinchartsURL.openConnection()
    Files.copy(connection.getInputStream, Paths.get(compressedHistoryFile))
    log.debug("Download finished")
  }

  if (decompress) {
    log.debug("Decompress start")
    Runtime.getRuntime.exec("/usr/bin/gzip -df " + compressedHistoryFile).waitFor //Unix
    log.debug("Decompress Finished")
  }

  val tickTable = TableQuery[TickTable]

  if (writeToDB) {
    // remove table if it exists
    if (makeTableMap.contains("tick"))
      tickTable.ddl.drop
    tickTable.ddl.create

    log.debug("Writing to database start")
    val filPath = Paths.get(decompressedHistoryFile).toAbsolutePath.toString
    sql"COPY tick(timestamp, price, amount) FROM '#$filPath' DELIMITER ',' CSV;".as[String].list
    log.debug("Writing to database finish")
  }

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

  if (createGranularityTabs) {
    log.debug("Creating data granularity-tables - Start")
    val list: List[TickDataPoint] = tickTable.list
    val iterator = list.iterator
    var tickDataPoint = iterator.next()
    var lastTick = tickDataPoint

    val min1Table = TableQuery[Min1Table]
    var min1Row = NextRow(60, tickDataPoint)
    if (makeTableMap.contains("min1")) {
      min1Table.ddl.drop
    }
    min1Table.ddl.create

    val min2Table = TableQuery[Min2Table]
    var min2Row = NextRow(120, tickDataPoint)
    if (makeTableMap.contains("min2")) {
      min2Table.ddl.drop
    }
    min2Table.ddl.create

    val min5Table = TableQuery[Min5Table]
    var min5Row = NextRow(300, tickDataPoint)
    if (makeTableMap.contains("min5")) {
      min5Table.ddl.drop
    }
    min5Table.ddl.create

    val min10Table = TableQuery[Min10Table]
    var min10Row = NextRow(600, tickDataPoint)
    if (makeTableMap.contains("min10")) {
      min10Table.ddl.drop
    }
    min10Table.ddl.create

    val min15Table = TableQuery[Min15Table]
    var min15Row = NextRow(900, tickDataPoint)
    if (makeTableMap.contains("min15")) {
      min15Table.ddl.drop
    }
    min15Table.ddl.create

    val min30Table = TableQuery[Min30Table]
    var min30Row = NextRow(1800, tickDataPoint)
    if (makeTableMap.contains("min30")) {
      min30Table.ddl.drop
    }
    min30Table.ddl.create

    val hour1Table = TableQuery[Hour1Table]
    var hour1Row = NextRow(3600, tickDataPoint)
    if (makeTableMap.contains("hour1")) {
      hour1Table.ddl.drop
    }
    hour1Table.ddl.create

    val hour2Table = TableQuery[Hour2Table]
    var hour2Row = NextRow(7200, tickDataPoint)
    if (makeTableMap.contains("hour2")) {
      hour2Table.ddl.drop
    }
    hour2Table.ddl.create

    val hour5Table = TableQuery[Hour5Table]
    var hour5Row = NextRow(18000, tickDataPoint)
    if (makeTableMap.contains("hour5")) {
      hour5Table.ddl.drop
    }
    hour5Table.ddl.create

    val hour12Table = TableQuery[Hour12Table]
    var hour12Row = NextRow(43200, tickDataPoint)
    if (makeTableMap.contains("hour12")) {
      hour12Table.ddl.drop
    }
    hour12Table.ddl.create

    val dayTable = TableQuery[DayTable]
    var dayRow = NextRow(86400, tickDataPoint)
    if (makeTableMap.contains("day")) {
      dayTable.ddl.drop
    }
    dayTable.ddl.create

    while (iterator.hasNext) {
      tickDataPoint = iterator.next()

      while (min1Row.endTimestamp < tickDataPoint.timestamp) {
        min1Table += min1Row.thisRow
        min1Row = min1Row.updateNoTickNextRow()
      }
      min1Row.addTick(tickDataPoint)

      while (min2Row.endTimestamp < tickDataPoint.timestamp) {
        min2Table += min2Row.thisRow
        min2Row = min2Row.updateNoTickNextRow()
      }
      min2Row.addTick(tickDataPoint)

      while (min5Row.endTimestamp < tickDataPoint.timestamp) {
        min5Table += min5Row.thisRow
        min5Row = min5Row.updateNoTickNextRow()
      }
      min5Row.addTick(tickDataPoint)

      while (min10Row.endTimestamp < tickDataPoint.timestamp) {
        min10Table += min10Row.thisRow
        min10Row = min10Row.updateNoTickNextRow()
      }
      min10Row.addTick(tickDataPoint)

      while (min15Row.endTimestamp < tickDataPoint.timestamp) {
        min15Table += min15Row.thisRow
        min15Row = min15Row.updateNoTickNextRow()
      }
      min15Row.addTick(tickDataPoint)

      while (min30Row.endTimestamp < tickDataPoint.timestamp) {
        min30Table += min30Row.thisRow
        min30Row = min30Row.updateNoTickNextRow()
      }
      min30Row.addTick(tickDataPoint)

      while (hour1Row.endTimestamp < tickDataPoint.timestamp) {
        hour1Table += hour1Row.thisRow
        hour1Row = hour1Row.updateNoTickNextRow()
      }
      hour1Row.addTick(tickDataPoint)

      while (hour2Row.endTimestamp < tickDataPoint.timestamp) {
        hour2Table += hour2Row.thisRow
        hour2Row = hour2Row.updateNoTickNextRow()
      }
      hour2Row.addTick(tickDataPoint)

      while (hour5Row.endTimestamp < tickDataPoint.timestamp) {
        hour5Table += hour5Row.thisRow
        hour5Row = hour5Row.updateNoTickNextRow()
      }
      hour5Row.addTick(tickDataPoint)

      while (hour12Row.endTimestamp < tickDataPoint.timestamp) {
        hour12Table += hour12Row.thisRow
        hour12Row = hour12Row.updateNoTickNextRow()
      }
      hour12Row.addTick(tickDataPoint)

      while (dayRow.endTimestamp < tickDataPoint.timestamp) {
        dayTable += dayRow.thisRow
        dayRow = dayRow.updateNoTickNextRow()
      }
      dayRow.addTick(tickDataPoint)

      lastTick = tickDataPoint
    }
    log.debug("Creating granularity-tables - Finished")
  }
  log.debug("Data is ready, startTime:" + startTime + ", endTime:" + endTime)
  context.parent ! DataReady(startTime, endTime)

  def makeTableMap: Map[String, MTable] = {
    val tableList = MTable.getTables.list(session)
    val tableMap = tableList.map { t => (t.name.name, t)}.toMap
    tableMap
  }

  override def receive: Receive = {
    case e: Any =>
      log.warning("DatabaseDownloadActor received unknown message.")
  }

  case class NextRow(intervalSec: Int, firstTick: TickDataPoint) {
    var open = firstTick.price
    var high = firstTick.price
    var low = firstTick.price
    var volume = firstTick.amount
    var close = firstTick.price
    var endTimestamp = firstTick.timestamp + intervalSec

    def addTick(tick: TickDataPoint): Unit = {
      if (volume == 0) {
        open = tick.price
        high = tick.price
        low = tick.price
        volume = tick.amount
        close = tick.price
      }
      else {
        if (tick.price > high) {
          high = tick.price
        }
        else if (tick.price < low) {
          low = tick.price
        }
        volume = volume + tick.amount
        close = tick.price
      }
    }

    def updateNoTickNextRow() = {
      NextRow(intervalSec, TickDataPoint(None, None, endTimestamp, close, 0))
    }

    def thisRow: DataPoint = {
      DataPoint(None, None, endTimestamp, open, close, low, high, volume)
    }
  }

}

object BitcoinChartsToDBActor {
  // The reason for this parameters is testing. Hidden from use.
  def props(session: Session): Props = Props(new BitcoinChartsToDBActor(true, true, true, true, "", session))
}