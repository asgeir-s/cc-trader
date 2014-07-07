package com.cctrader.dbtables

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}


class TSTable(tag: Tag) extends Table[TSInfo](tag, "tsinfo") {
  // This is the primary key column
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def starTimestamp = column[Int]("starttimestamp")

  def basedOnHistoryPoints = column[Int]("basedonhistorypoints")

  def granularity = column[String]("granularity")

  def currencyPair = column[String]("currencypair")

  def exchange = column[String]("exchange")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, starTimestamp, basedOnHistoryPoints, granularity, currencyPair, exchange) <>(TSInfo.tupled, TSInfo.unapply)
}

case class TSInfo(id: Option[Long], name: String, starTimestamp: Int, basedOnHistoryPoints: Int, granularity: String, currencyPair: String, exchange: String)