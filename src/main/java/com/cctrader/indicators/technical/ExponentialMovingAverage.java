package com.cctrader.indicators.technical;


import com.cctrader.data.MarketDataSet;

/**
 * Given an ordered list of data points, you can construct the exponentially weighted moving average of all
 * the points up to the current point. In an exponential moving average (EMA or EWMA for short),
 * the weights decrease by a constant factor α as the terms get older. This kind of cumulative moving
 * average is frequently used when charting stock prices. The recursive formula for EMA is
 * <p/>
 * EMAtoday = α⋅xtoday + (1-α)EMAyesterday
 * <p/>
 * where xtoday is today's current price point and α is some constant between 0 and 1. Often,
 * α is a function of a certain number of days N. The most commonly used function is α = 2/(N+1). For
 * instance, the 9-day EMA of a sequence has α = 0.2, while a 30-day EMA has α = 2/31 = 0.06452.
 * <p/>
 * For values of α closer to 1, the EMA sequence can be initialized at EMA₁ = x₁. However,
 * if α is very small, the earliest terms in the sequence may receive undue weight with such an
 * initialization. To correct this issue in an N-day EMA, the first term of the EMA sequence is set to be
 * the simple average of the first ⌈(N-1)/2⌉ terms, thus, the EMA starts on day number ⌈(N-1)/2⌉.
 * <p/>
 * For instance, in a 9-day exponential moving average, EMA₄ = (x₁+x₂+x₃+x₄)/4. Then EMA₅ = 0.2x₅ + 0.8EMA₄
 * and EMA₆ = 0.2x₆ + 0.8EMA₅ etc.
 * <p/>
 * Normalize with price normalizer.
 */
public class ExponentialMovingAverage {
    public static Double get(int index, int period,
                             MarketDataSet marketDataSet) {
        Double alpha = 2D / (period + 1D);  //normal way to do it
        Double oldValueClose;

        if (index < (period - 1) / 2) {
            return marketDataSet.apply(index).close();
        }

        int startIndex;
        if (index - (period - 1) > 0)
            startIndex = index - (period - 1);
        else
            startIndex = 0;

        if (startIndex == index)
            return marketDataSet.apply(index).close();

        else {
            oldValueClose = marketDataSet.apply(startIndex).close();

            for (int i = startIndex + 1; i < index + 1; i++) {
                double newValueClose = oldValueClose + alpha * (marketDataSet.apply(i).close() - oldValueClose);
                oldValueClose = newValueClose;
            }
        }
        return oldValueClose;
    }
}