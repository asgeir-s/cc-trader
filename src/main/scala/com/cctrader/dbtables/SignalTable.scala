package com.cctrader.dbtables

import com.cctrader.data.Trade

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Trade as written to the trade table
 */

trait SignalTable {
  this: Table[_] =>
  // This is the primary key column
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def writetimestamp = column[Int]("writetimestamp")

  def dptimestamp = column[Int]("dptimestamp")

  def signal = column[String]("signal")

  def price = column[Double]("price")

  // Every table needs a * projection with the same type as the table's type parameter
  protected val common_* = (id, writetimestamp, dptimestamp, signal, price) <>(Trade.tupled, Trade.unapply)
}