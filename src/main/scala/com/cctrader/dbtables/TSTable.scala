package com.cctrader.dbtables

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * This table is public!
 */
class TSTable(tag: Tag) extends Table[TSInfo](tag, "tsinfo") {
  // This is the primary key column
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def starTimestamp = column[Int]("starttimestamp")

  def instrument = column[String]("instrument")


  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, starTimestamp, instrument) <>(TSInfo.tupled, TSInfo.unapply)
}

case class TSInfo(id: Option[Long], name: String, starTimestamp: Int, instrument: String)