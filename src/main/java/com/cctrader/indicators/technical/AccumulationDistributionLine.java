package com.cctrader.indicators.technical;

import com.cctrader.data.DataPoint;
import com.cctrader.data.MarketDataSet;

/**
 * This is a non-bounded indicator that simply keeps a running sum over the period of the security. Traders
 * look for trends in this indicator to gain insight on the amount of purchasing compared to selling of a
 * security. If a security has an accumulation/distribution line that is trending upward,
 * it is a sign that there is more buying than selling.
 * <p/>
 * Acc/Dist = ((Close - Low) - (High - Close)) / (High - Low) * Period\'s Volume
 * <p/>
 * Normalize with price normalizer
 */
public class AccumulationDistributionLine {

    public static double get(int index, int period,
                             MarketDataSet marketDataSet) {

        int startIndex;
        if (index - (period - 1) > 0)
            startIndex = index - (period - 1);
        else
            startIndex = 0;

        if (startIndex == index) {
            DataPoint dp = marketDataSet.apply(index);
            return ((dp.close() - dp.low()) - (dp.high() - dp.close()) / (dp.high() - dp.low()) * dp.volume
                    ());
        }

        double low = marketDataSet.apply(startIndex).low();
        double high = marketDataSet.apply(startIndex).high();
        double volume = marketDataSet.apply(startIndex).volume();
        double close = marketDataSet.apply(startIndex).close();

        for (int i = startIndex + 1; i < index + 1; i++) {
            if (marketDataSet.apply(i).low() < low)
                low = marketDataSet.apply(i).low();
            if (marketDataSet.apply(i).high() > high)
                high = marketDataSet.apply(i).high();
            volume += marketDataSet.apply(i).volume();
            close = marketDataSet.apply(i).close();
        }
        return ((close - low) - (high - close) / (high - low) * volume);
    }

}
