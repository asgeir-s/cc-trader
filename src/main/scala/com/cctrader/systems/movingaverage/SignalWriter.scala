package com.cctrader.systems.movingaverage

import com.cctrader.data.Signal.Signal
import com.cctrader.data._

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery => Q}

/**
 *
 */
class SignalWriter(name: String) extends {val dbName = "movingaverage-trades"} with SignalWriterTrait

