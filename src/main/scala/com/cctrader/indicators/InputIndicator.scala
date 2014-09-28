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

  def name: String = {
    val fullName = this.getClass.getName
    fullName.substring(fullName.lastIndexOf('.')+1, fullName.length)
  }

  def setNormalizationBounds(data: MarketDataSet, pointsNeededToCompute: Int) {
    var min = Double.MaxValue
    var max = Double.MinValue
    for (i <- pointsNeededToCompute until data.size) {
      val value: Double = apply(i, data)
      if(value < min) {
        min = value
      }
      else if(value > max){
        max = value
      }
    }
    println(name + ": Should be Oscillator: minValue:" + min + ", maxValue:" + max + "")
    if(Math.abs(min) > Math.abs(max)) {
      normInRang(-Math.abs(min), Math.abs(min))
    }
    else {
      normInRang(-Math.abs(max), Math.abs(max))
    }
  }

  def normInRang(min: Double, max: Double): Unit = {
    if (!normInSet) {
      normMinIn = min
      normMaxIn = max
      normInSet = true
      println(name + ": Normalization range set to:[" + normMinIn + ", " + normMaxIn + "]")
    }
    else {
      println(name + ": ERROR: normInRang already set to: minValue:" + normMinIn + ", maxValue:" + normMaxIn)
    }

  }

  def normOutRange(min: Double, max: Double): Unit = {
    if (!normOutSet) {
      normMinOut = min
      normMaxOut = max
      normOutSet = true
    }
    else {
      println(name + ": ERROR: normOutRang already set")
    }
  }

  def getReScaled(index: Int, dataSet: MarketDataSet): Double = {
    reScaled(apply(index, dataSet))
  }

  def reScaled(value: Double) = {
    if (normInSet && normOutSet) {
      (value - normMinIn) * (normMaxOut - normMinOut) / (normMaxIn - normMinIn) + normMinOut
    }
    else {
      println(name + ": ERROR: normOutRang or normInRang not set")
      Double.NaN
    }
  }

  def deScaled(value: Double) = {
    if (normInSet && normOutSet) {
      (value - normMinOut) * (normMaxIn - normMinIn) / (normMaxOut - normMinOut) + normMinIn
    }
    else {
      println(name + ": ERROR: normOutRang or normInRang not set")
      Double.NaN
    }
  }

}
