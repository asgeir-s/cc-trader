package com.cctrader.indicators

import com.cctrader.UnitTest
import com.cctrader.indicators.technical.AccumulationDistribution

/**
 *
 */
class InputIndicatorSpec extends UnitTest {

  "normalizing 50 from the range 0 to 100 to the range -1 to 1" should "give 0" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.normalize(50) == 0D)
  }

  "normalizing 0 from the range 0 to 100 to the range -1 to 1" should "give -1" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.normalize(0) == -1D)
  }

  "normalizing 200 from the range 0 to 100 to the range -1 to 1" should "give 2" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.normalize(200) == 3D)
  }

  "normalizing -100 from the range 0 to 100 to the range -1 to 1" should "give -2" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.normalize(-100) == -3D)
  }

  "normalizing 100 from the range 0 to 100 to the range -1 to 1" should "give 1" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.normalize(100) == 1D)
  }

  "normalizing 100 from the range 0 to 100 to the range 0 to 1" should "give 1" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.normalize(100) == 1D)
  }

  "normalizing 50 from the range 0 to 100 to the range 0 to 1" should "give 0.5" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.normalize(50) == 0.5D)
  }

  "normalizing 0 from the range 0 to 100 to the range 0 to 1" should "give 0" in {
    val testIndicator = new AccumulationDistribution
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.normalize(0) == 0D)
  }


}
