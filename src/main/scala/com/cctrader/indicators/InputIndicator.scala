package com.cctrader.indicators

import com.cctrader.data.MarketDataSet

/**
 * OBS: normOutRang needs to be set to use normalization
 */
trait InputIndicator {

  private var normMinIn: Double = Double.MaxValue
  private var normMaxIn: Double = Double.MinValue
  private var normInSet = false

  private var normMinOut: Double = 0.0
  private var normMaxOut: Double = 0.0
  private var normOutSet = false

  /**
   * Calculating the indicator.
   *
   * @param t index of time now in the marketDataSet
   * @param data the marketDataSet
   * @return
   */
  def apply(t: Int, data: MarketDataSet): Double

  def setNormalizationBounds(data: MarketDataSet, pointsNeededToCompute: Int) {
    for (i <- pointsNeededToCompute until data.size) {
      val value: Double = apply(i, data)
      if(value < normMinIn) {
        normMinIn = value
      }
      else if(value > normMaxIn){
        normMaxIn = value
      }
    }
    normInSet = true
    println("Normalization range set to:[" + normMinIn + ", " + normMaxIn + "]")
  }

  def normInRang(min: Double, max: Double): Unit = {
    if (!normInSet) {
      normMinIn = min
      normMaxIn = max
      normInSet = true
    }
    else {
      println("ERROR: normInRang already set")
    }
  }

  def normOutRange(min: Double, max: Double): Unit = {

    if (!normOutSet) {
      normMinOut = min
      normMaxOut = max
      normOutSet = true
    }
    else {
      println("ERROR: normOutRang already set")
    }
  }

  def getNormalized(index: Int, dataSet: MarketDataSet): Double = {
    normalize(apply(index, dataSet))
  }

  def normalize(value: Double) = {
    if (normInSet && normOutSet) {
      normMinOut + (value - normMinIn) * (normMaxOut - normMinOut) / (normMaxIn - normMinIn)
    }
    else {
      println("ERROR: normOutRang or normInRang not set")
      Double.NaN
    }
  }

  def deNormalize(value: Double) = {
    if (normInSet && normOutSet) {
      normMinOut - (value + normMinIn) / (normMaxOut + normMinOut) * (normMaxIn - normMinIn)
    }
    else {
      println("ERROR: normOutRang or normInRang not set")
      Double.NaN
    }
  }

}
