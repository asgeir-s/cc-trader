package com.cctrader.data

import java.util.Date

import com.cctrader.MarketDataSettings
import org.apache.commons.math3.analysis.function.Sigmoid

import scala.collection.mutable.ListBuffer

/**
 * A collection of DataPoints.
 * The collection will have a fixed size of MaxSize. The oldest points will be removed when new are added.
 * The min and max are updated when new points come inn.
 *
 * DataPoints can be normalized.
 *
 * !Change is GOOD, Absolute is bad!
 *
 * According to : http://www.mirlabs.org/ijcisim/regular_papers_2014/IJCISIM_24.pdf
 * the sigmoid function is one of the best normalizers for ANN usage.
 */
case class MarketDataSet(private val data: List[DataPoint], settings: MarketDataSettings) {

  println("New MarketDataSet created data size is: " + data.size)

  if (settings.numberOfHistoricalPoints < data.length) {
    throw new Exception("dataPoint list size:" + data.size + " is bigger then maxSize:" + settings.numberOfHistoricalPoints)
  }

  val sigmoid = new Sigmoid(-1.0, 1.0)

  val list: ListBuffer[DataPoint] = ListBuffer()
  list.++=(data)

  /**
   * Add new dataPoint to this set
   * @param dataPoint to be added
   */
  def addDataPoint(dataPoint: DataPoint) {
    println("Add ne dp to marketDataSet:" + dataPoint + ". Size is:" + size)

    if(list.length == settings.numberOfHistoricalPoints) {
      println("Remove from list: " + list(0))
      list.-=(list(0))
    }
    list.+=(dataPoint)
  }

  /**
   *
   * @param fromIndex start index (from current marketDataSet)
   * @param toIndex end index (from current marketDataSet)
   * @return the new marketData set, with the same settings as this
   */
  def subset(fromIndex: Int, toIndex: Int) = {
    MarketDataSet(list.slice(fromIndex, toIndex).toList, settings)
  }

  def size = list.length

  def apply(index: Int): DataPoint = list(index)

  /**
   * @return the newest dataPoint
   */
  def last = list.last

  /**
   * @return the oldest dataPoint
   */
  def first = list(0)

  def getList = list

  def iterator = list.iterator

  def fromDate: Date = list(0).date

  def toDate: Date = list.last.date

  override def toString = {
    "MarketDataSet from " + fromDate + " to " + toDate + "with: granularity:" +
      Granularity + ", size:" + list.size
  }

}