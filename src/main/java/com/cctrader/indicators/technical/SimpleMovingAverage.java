package com.cctrader.indicators.technical;

import com.cctrader.data.MarketDataSet;

/**
 * Normalize with price normalizer.
 */
public class SimpleMovingAverage {

    /**
     * the if numberOfPoints goes farther back then the table the table will return the slowest average
     * available.
     *
     * @param index         the index to end at (current point)
     * @param period        number of points to go back. To include in the average calculation. Fastness.
     * @param marketDataSet the data set
     * @return Double array with average close on 0 and average volume on 1
     */
    public static Double[] get(int index, int period, MarketDataSet marketDataSet) {
        int startIndex;

        if (index - (period - 1) > 0)
            startIndex = index - (period - 1);
        else
            startIndex = 0;

        if (startIndex == index)
            return new Double[]{marketDataSet.apply(index).close(), marketDataSet.apply(index).volume()};
        else {
            int count = 0;
            double sumClose = 0;
            double sumVolume = 0;
            for (int i = startIndex; i < index + 1; i++) {
                sumClose = sumClose + marketDataSet.apply(i).close();
                sumVolume = sumVolume + marketDataSet.apply(i).volume();
                count++;
            }
            return new Double[]{sumClose / count, sumVolume / count};
        }
    }

}
