package com.cctrader.data

import java.util.Date

import com.cctrader.data.CurrencyPair.CurrencyPair
import com.cctrader.data.Exchange.Exchange
import com.cctrader.data.Granularity.Granularity

/**
 * A collection of DataPoints.
 */
case class MarketDataSet(data: List[DataPoint],
                         granularity: Granularity,
                         currencyPair: CurrencyPair,
                         exchange: Exchange,
                         minPrice: Double,
                         maxPrice: Double,
                         minVolume: Double,
                         maxVolume: Double) {


  def size = data.length

  def apply(index: Int) = data(index)

  def last = data.last

  def iterator = data.iterator

  val fromDate: Date = data(0).date

  val toDate: Date = data.last.date

  /*
  Returns a new MarketDataSet consisting of this and that.
  If it fails this is returned.
   */
  def +(that: MarketDataSet): MarketDataSet = {
    if (this.toDate.compareTo(that.fromDate) <= 0 && this.granularity == that.granularity && this
      .currencyPair == that.currencyPair && this.exchange == that.exchange)
      MarketDataSet(this.data ++ that.data, this.granularity, this.currencyPair,
        this.exchange, this.minPrice.min(that.minPrice), this.maxPrice.max(that.maxPrice),
        this.minVolume.min(that.minVolume), this.maxVolume.max(that.maxPrice))
    else
      this
  }

  def getSubset(fromIndex: Int, toIndex: Int): MarketDataSet = {
    val newData = data.slice(fromIndex, toIndex)
    val minPrice = newData.minBy(_.low)
    val maxPrice = newData.maxBy(_.high)
    val minVolume = newData.minBy(_.volume)
    val maxVolume = newData.maxBy(_.volume)
    new MarketDataSet(newData, granularity,
      currencyPair,
      exchange,
      minPrice.low,
      maxPrice.high,
      minVolume.volume,
      maxVolume.volume)
  }

  override def toString = {
    "MarketDataSet from " + fromDate + " to " + toDate + "with: granularity:" +
      granularity + ", size:" + data.size
  }

}