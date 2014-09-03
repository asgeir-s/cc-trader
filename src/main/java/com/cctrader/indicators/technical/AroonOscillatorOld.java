package com.cctrader.indicators.technical;

import com.cctrader.data.MarketDataSet;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

/**
 * An expansion of the Aroon is the Aroon oscillator, which simply plots the difference between the Aroon
 * up and down lines by subtracting the two lines. This line is then plotted between a range of -100 and
 * 100. The centerline at zero in the oscillator is considered to be a major signal line determining the
 * trend. The higher the value of the oscillator from the centerline point, the more upward strength there
 * is in the security; the lower the oscillator's value is from the centerline,
 * the more downward pressure. A trend reversal is signaled when the oscillator crosses through the
 * centerline. For example, when the oscillator goes from positive to negative,
 * a downward trend is confirmed. Divergence is also used in the oscillator to predict trend reversals. A
 * reversal warning is formed when the oscillator and the price trend are moving in an opposite direction.
 */
public class AroonOscillatorOld {
    public static Double get(int index, int period,
                             MarketDataSet marketDataSet) {

        double aroonUP = 0D;
        double aroonDown = 0D;

        int startIndex;
        if (index - (period - 1) > 0)
            startIndex = index - (period - 1);
        else {
            startIndex = 0;
            period = index;
        }

        double high = marketDataSet.apply(startIndex).high();
        int indexOfHigh = 0;

        double low = marketDataSet.apply(startIndex).low();
        int indexOfLow = 0;

        int count = 1;
        for (int i = startIndex + 1; i < index + 1; i++) {
            if (marketDataSet.apply(i).high() > high) {
                high = marketDataSet.apply(i).high();
                indexOfHigh = i;
            }

            if (marketDataSet.apply(i).low() < low) {
                low = marketDataSet.apply(i).low();
                indexOfLow = i;
            }
            count++;
        }

        aroonUP = ((new Double(count) - (new Double(count) - new Double(indexOfHigh))) / new Double(count)
        ) * new Double(100);

        aroonDown = ((new Double(count) - (new Double(count) - new Double(indexOfLow))) / new Double(count)
        ) * new Double(100);
        return aroonUP - aroonDown;
    }

    public static double normalize(double value) {
        NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize, null, 100, -100, 1.0,
                -1.0);

        return normalizer.normalize(value);
    }
}