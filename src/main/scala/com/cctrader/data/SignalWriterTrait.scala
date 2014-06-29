package com.cctrader.data

import com.cctrader.data.Signal._
import com.cctrader.dbtables.SignalTable
import com.typesafe.config.ConfigFactory

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
trait SignalWriterTrait {

  val dbName: String
  val config = ConfigFactory.load()

  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.trader.dbname"),
    driver = config.getString("postgres.driver"),
    user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  implicit val session = databaseFactory.createSession()

  val table = TableQuery[MASignalTable]
  if (makeTableMap.contains(dbName)) {
    table.ddl.drop
  }
  table.ddl.create

  def newSignal(signal: Signal, dataPoint: DataPoint) {
    //if(!signal.equals(Signal.SAME)){
    table += Trade(None, (System.currentTimeMillis() / 1000).toInt, dataPoint.timestamp, signal.toString, dataPoint.close)
    //}
    // notify new trades (should this only happen live??)
    Q.updateNA("NOTIFY " + dbName  + " , '" + table.list.last.id + "'").execute

    println("Received: signal:" + signal + ", dataPoint:" + dataPoint)
  }

  def makeTableMap: Map[String, MTable] = {
    val tableList = MTable.getTables.list(session)
    val tableMap = tableList.map { t => (t.name.name, t)}.toMap
    tableMap
  }

  class MASignalTable(tag: Tag) extends Table[Trade](tag, dbName) with SignalTable {def * = common_*}


}
