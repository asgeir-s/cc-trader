package com.cctrader.data

import com.cctrader.data.Signal._
import com.typesafe.config.ConfigFactory


import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
trait SignalWriterTrait {

  val config = ConfigFactory.load()

  val databaseFactory = Database.forURL(
    url = "jdbc:postgresql://" + config.getString("postgres.host") + ":" + config.getString("postgres.port") + "/" + config
      .getString("postgres.dbname"),
    driver = config.getString("postgres.driver"),
    user = config.getString("postgres.user"),
    password = config.getString("postgres.password"))

  implicit val session = databaseFactory.createSession()


  def newSignal(signal: Signal, dataPoint: DataPoint)

}
