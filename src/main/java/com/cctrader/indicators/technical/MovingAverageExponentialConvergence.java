package com.cctrader.indicators.technical;

import com.cctrader.data.MarketDataSet;

/**
 * returns a positive value if fast is above slow.
 * <p/>
 * Normalize with price normalizer?? or max min in last period
 */
public class MovingAverageExponentialConvergence {
    final int slow;
    final int fast;

    public MovingAverageExponentialConvergence(int slow, int fast) {
        this.slow = slow;
        this.fast = fast;
    }

    public double get(int index, MarketDataSet marketDataSet) {
        double slowEMAVC = ExponentialMovingAverage.get(index, slow, marketDataSet);
        double fastEMAVC = ExponentialMovingAverage.get(index, fast, marketDataSet);

        return fastEMAVC - slowEMAVC;
    }
}
