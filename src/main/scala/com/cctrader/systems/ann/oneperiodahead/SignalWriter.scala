package com.cctrader.systems.ann.oneperiodahead

import com.cctrader.data.Signal.Signal
import com.cctrader.data._

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
class SignalWriter(name: String) extends SignalWriterTrait {

  val table = TableQuery[MASignalTable]
  if (makeTableMap.contains(name)) {
    table.ddl.drop
  }
  table.ddl.create

  def newSignal(signal: Signal, dataPoint: DataPoint) {
    //if(!signal.equals(Signal.SAME)){
      table += Trade(None, (System.currentTimeMillis()/ 1000).toInt, dataPoint.timestamp, signal.toString, dataPoint.close)
    //}
    println("Received: signal:" + signal + ", dataPoint:" + dataPoint)
  }

  def makeTableMap: Map[String, MTable] = {
    val tableList = MTable.getTables.list(session)
    val tableMap = tableList.map { t => (t.name.name, t)}.toMap
    tableMap
  }

  class MASignalTable(tag: Tag) extends Table[Trade](tag, name) with SignalTable {def * = common_*}

}

