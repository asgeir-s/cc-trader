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
    if (dataPoint.low < settings.MinPrice) {
      throw new Exception("dataPoint price is lower then minPrice:" + settings.MinPrice + ".")
    }
    if (dataPoint.high > settings.MaxPrice) {
      throw new Exception("dataPoint price is higher then maxPrice:" + settings.MaxPrice + ".")
    }
    if (dataPoint.volume < settings.MinVolume) {
      throw new Exception("dataPoint volume is lower then minVolume:" + settings.MinVolume + ".")
    }
    if (dataPoint.volume > settings.MaxVolume) {
      throw new Exception("dataPoint volume is higher then maxVolume:" + settings.MaxVolume + ".")
    }
    if (list.size == settings.numberOfHistoricalPoints) {
      list.trimStart(1)
    }
    list.append(dataPoint)
  }

  /**
   * Sigmoid Normalization for change between prices. Scaled with: PriceChangeScale.
   *
   * @param value change in price between to points
   * @return divided by PriceChangeScale and the normalized value with Sigmoid Normalization to range -1 to 1
   */
  def sigmoidNormalizerPriceChange(value: Double): Double = sigmoid.value(value / settings.PriceChangeScale) //1.0 / (1.0 + Math.exp(-value))

  /**
   * Sigmoid Normalization for change between volumes. Scaled with: VolumeChangeScale.
   *
   * @param value change in volume between to points
   * @return divided by VolumeChangeScale and the normalized value with Sigmoid Normalization to range -1 to 1
   */
  def sigmoidNormalizerVolumeChange(value: Double): Double = sigmoid.value(value / settings.VolumeChangeScale) //1.0 / (1.0 + Math.exp(-value))

  /**
   * Sigmoid Normalization for absolute price. Scaled with: (MaxPrice - MinPrice).
   *
   * @param value change in price between to points
   * @return divided by (MaxPrice - MinPrice) and the normalized value with Sigmoid Normalization to range -1 to 1
   */
  def sigmoidNormalizerPriceAbsolute(value: Double): Double = sigmoid.value(value / (settings.MaxPrice - settings.MinPrice)) //1.0 / (1.0 + Math.exp(-value))

  /**
   * Sigmoid Normalization for absolute volume. Scaled with: (MaxVolume - MinVolume).
   *
   * @param value change in volume between to points
   * @return divided by (MaxVolume - MinVolume) and the normalized value with Sigmoid Normalization to range -1 to 1
   */
  def sigmoidNormalizerVolumeAbsolute(value: Double): Double = sigmoid.value(value / (settings.MaxVolume - settings.MinVolume)) //1.0 / (1.0 + Math.exp(-value))


  /**
   * Absolute normalize Sigmoid dataPoint.
   *
   * @param dataPoint dataPoint to normalize
   * @param priceAbsoluteNormalizedFunction function for absolute price normalization
   * @param volumeAbsoluteNormalizedFunction function for absolute volume normalization
   * @return dataPoint with absolute normalized values
   */
  def normalizedDataPointAbsolute(dataPoint: DataPoint, priceAbsoluteNormalizedFunction: Double => Double, volumeAbsoluteNormalizedFunction: Double => Double): DataPoint = {
    DataPoint(
      dataPoint.id,
      dataPoint.sourceId,
      dataPoint.timestamp,
      sigmoidNormalizerVolumeChange(dataPoint.open),
      sigmoidNormalizerVolumeChange(dataPoint.close),
      sigmoidNormalizerVolumeChange(dataPoint.low),
      sigmoidNormalizerVolumeChange(dataPoint.high),
      volumeAbsoluteNormalizedFunction(dataPoint.volume)
    )
  }

  /**
   * Change normalize Sigmoid "dataPoint".
   * This is not a real dataPoint, only a value holder.
   *
   * @param first the first point (according to timestamp)
   * @param last the second point (according to timestamp)
   * @param priceChangeNormalizedFunction function for change price normalization
   * @param volumeChangeNormalizedFunction function for change volume normalization
   * @return normalized dataPoint
   */
  def normalizedDataPointChange(first: DataPoint, last: DataPoint, priceChangeNormalizedFunction: Double => Double, volumeChangeNormalizedFunction: Double => Double): DataPoint = {
    DataPoint(
      None,
      None,
      0,
      sigmoidNormalizerVolumeChange(last.open - first.open),
      sigmoidNormalizerVolumeChange(last.close - first.close),
      sigmoidNormalizerVolumeChange(last.low - first.low),
      sigmoidNormalizerVolumeChange(last.high - first.high),
      sigmoidNormalizerVolumeChange(last.volume - first.volume)
    )
  }

  /**
   * Absolute Sigmoid normalize dataPoint with index.
   *
   * @param index index of dataPoint to normalize.
   * @return absolute normalized dataPoint.
   */
  def dataPointSigmoidNormalizedAbsolute(index: Int): DataPoint = normalizedDataPointAbsolute(list(index), sigmoidNormalizerPriceAbsolute, sigmoidNormalizerVolumeAbsolute)

  /**
   * Change Sigmoid normalized "dataPoint" with index.
   * This is not a real dataPoint, only a value holder.
   *
   * @param first the first point (according to timestamp)
   * @param last the second point (according to timestamp)
   * @return
   */
  def dataPointSigmoidNormalizedChange(first: Int, last: Int): DataPoint = normalizedDataPointChange(list(first), list(last), sigmoidNormalizerPriceChange, sigmoidNormalizerVolumeChange)

  /**
   * Double array with price and volume normalized value for change between the datapoints
   *
   * @param first index of the first point in the time series
   * @param last index of the last point in the time series
   * @return double array with change between the two dataPoints (last - first), sigmoid normalized
   */
  def dataPointSigmoidNormalizedChangeArray(first: Int, last: Int): Array[Double] = {
    val changeNormalDataPoint = normalizedDataPointChange(list(first), list(last), sigmoidNormalizerPriceChange, sigmoidNormalizerVolumeChange)
    Array(changeNormalDataPoint.open, changeNormalDataPoint.close, changeNormalDataPoint.low, changeNormalDataPoint.high, changeNormalDataPoint.volume)

  }

  /**
   * Double array with price and volume normalized value for dataPoint, absolute
   *
   * @param index index of dataPoint to normalize
   * @return double array with absolute sigmoid normalized of dataPoint
   */
  def dataPointSigmoidNormalizedAbsoluteArray(index: Int): Array[Double] = {
    val normalDataPoint = normalizedDataPointAbsolute(list(index), sigmoidNormalizerPriceAbsolute, sigmoidNormalizerVolumeAbsolute)
    Array(normalDataPoint.open, normalDataPoint.close, normalDataPoint.low, normalDataPoint.high, normalDataPoint.volume)
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

  def last = list.last

  def first = list(0)

  def getList = list

  def iterator = list.iterator

  val fromDate: Date = list(0).date

  val toDate: Date = list.last.date

  override def toString = {
    "MarketDataSet from " + fromDate + " to " + toDate + "with: granularity:" +
      Granularity + ", size:" + data.size
  }

}