package com.cctrader.indicators

import com.cctrader.UnitTest
import com.cctrader.indicators.technical.AccumulationDistributionOscillator

/**
 *
 */
class InputIndicatorSpec extends UnitTest {

  "normalizing 50 from the range 0 to 100 to the range -1 to 1" should "give 0" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(50) == 0D)
  }

  "normalizing 0 from the range 0 to 100 to the range -1 to 1" should "give -1" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(0) == -1D)
  }

  "normalizing 200 from the range 0 to 100 to the range -1 to 1" should "give 2" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(200) == 3D)
  }

  "normalizing -100 from the range 0 to 100 to the range -1 to 1" should "give -2" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(-100) == -3D)
  }

  "normalizing 100 from the range 0 to 100 to the range -1 to 1" should "give 1" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(100) == 1D)
  }

  "normalizing 100 from the range 0 to 100 to the range 0 to 1" should "give 1" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.reScaled(100) == 1D)
  }

  "normalizing 50 from the range 0 to 100 to the range 0 to 1" should "give 0.5" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.reScaled(50) == 0.5D)
  }

  "normalizing 0 from the range 0 to 100 to the range 0 to 1" should "give 0" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(0, 100)
    testIndicator.normOutRange(0, 1)
    assert(testIndicator.reScaled(0) == 0D)
  }

  "scaling and reScaling" should "work" in {
    val testIndicator = new AccumulationDistributionOscillator
    testIndicator.normInRang(-40, 40)
    testIndicator.normOutRange(-1, 1)
    assert(testIndicator.reScaled(0)==0)
    assert(testIndicator.reScaled(20)==0.5)
    assert(testIndicator.reScaled(-20)==(-0.5))
    assert(testIndicator.deScaled(0) == 0)
    assert(testIndicator.deScaled(0.5) == 20)
    assert(testIndicator.deScaled(-0.5) == -20)




  }


}
