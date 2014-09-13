package com.cctrader.indicators.fundamental

import com.cctrader.data.MarketDataSet
import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.{HelperIndicators, InputIndicator}

import scala.io.Source

/**
 *
 */
class NumberOfTransaction extends HelperIndicators {

  //load cvs to List
  val src = Source.fromFile("download/coindesk-xbtdailytransactions.csv")
  val iter = src.getLines().map(_.split(","))
  final val list: List[(String, Double)] = iter.map(x => (x(0).asInstanceOf[String], x(1).toDouble)).toList

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  override def apply(t: Int, data: MarketDataSet): Double = {
    // return last list entry before time of data(t)

    val month: String = {
      if((data(t).date.getMonth+1) < 10) {
        "0" + (data(t).date.getMonth+1)
      }
      else {
        (data(t).date.getMonth+1).toString
      }
    }

    val dayOfMOnth: String = {
      if((data(t).date.getDate) < 10) {
        "0" + (data(t).date.getDate)
      }
      else {
        (data(t).date.getDate).toString
      }
    }
    findValueForDate("\"" + (data(t).date.getYear+1900) + "-" + month + "-" + dayOfMOnth + " 00:00:00\"")
  }

  def findValueForDate(srt: String): Double = {
    val value = list.find(x => x._1.equals(srt))
    value.get._2
  }
}
