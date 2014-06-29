package com.cctrader.indicators.technical;


import com.cctrader.data.MarketDataSet;

/**
 * Depends on granularity.
 * The sum of volume with positive volume on upward data points and negative on downwards.
 * <p/>
 * Normalize with volume normalizer. Or max min in last period
 */
public class OnBalanceVolume {

    public static double get(int index, int period,
                             MarketDataSet marketDataSet) {
        int startIndex;

        if (index - (period - 1) > 0)
            startIndex = index - (period - 1);
        else
            startIndex = 0;

        if (startIndex == index)
            if (marketDataSet.apply(index).close() > marketDataSet.apply(index).open())
                return marketDataSet.apply(index).volume();
            else
                return -(marketDataSet.apply(index).volume());
        else {
            double sumVolume = 0;
            for (int i = startIndex; i < index + 1; i++) {
                if (marketDataSet.apply(i).close() > marketDataSet.apply(i).open())
                    sumVolume += marketDataSet.apply(i).volume();
                else
                    sumVolume -= marketDataSet.apply(i).volume();
            }
            return sumVolume;
        }
    }

}
