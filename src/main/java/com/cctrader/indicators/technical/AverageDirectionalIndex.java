package com.cctrader.indicators.technical;

import com.cctrader.data.MarketDataSet;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

/**
 * Average Directional Index. Based on
 * http://technical.traders.com/tradersonline/display.asp?art=278
 * <p/>
 * The ADX is a combination of two price movement measures: the positive directional indicator (+DI) and
 * the negative directional indicator (-DI). The ADX measures the strength of a trend but not the direction
 * . The +DI measures the strength of the upward trend while the -DI measures the strength of the downward
 * trend. These two measures are also plotted along with the ADX line. Measured on a scale between zero and
 * 100, readings below 20 signal a weak trend while readings above 40 signal a strong trend.
 *
 * @author Carlos Aza Villarrubia
 * @version 1.1
 * @date 25/05/2008
 */
public class AverageDirectionalIndex {

    public static double get(int index, int period, MarketDataSet marketDataSet) {

        double[] tr = new double[period];
        double[] dmPlus = new double[period];
        double[] dmMinus = new double[period];
        double[] trN = new double[period];
        double[] dmPlusN = new double[period];
        double[] dmMinusN = new double[period];
        double[] dx = new double[period];
        double[] adx = new double[period];
        int counter = 0;

        int periodEnd = index - 1;
        double high = marketDataSet.apply(periodEnd).high();
        double low = marketDataSet.apply(periodEnd).low();
        double close = marketDataSet.apply(periodEnd).close();
        double high_1 = marketDataSet.apply(periodEnd - 1).high();
        double low_1 = marketDataSet.apply(periodEnd - 1).low();
        double close_1 = marketDataSet.apply(periodEnd - 1).close();

        for (int i = 0; i < period - 1; i++) {
            tr[i] = tr[i + 1];
            dmPlus[i] = dmPlus[i + 1];
            dmMinus[i] = dmMinus[i + 1];
            trN[i] = trN[i + 1];
            dmPlusN[i] = dmPlusN[i + 1];
            dmMinusN[i] = dmMinusN[i + 1];
            dx[i] = dx[i + 1];
            adx[i] = adx[i + 1];
        }

        // the first calculation for ADX is the true range value (TR)
        tr[period - 1] = Math.max(high - low, Math.max(Math.abs(high
                - close_1), Math.abs(low - close_1)));

        // determines the positive directional movement or returns zero if there
        // is no positive directional movement.
        dmPlus[period - 1] = high - high_1 > low_1 - low ? Math.max(high
                - high_1, 0) : 0;

        // calculates the negative directional movement or returns zero if there
        // is no negative directional movement.
        dmMinus[period - 1] = low_1 - low > high - high_1 ? Math.max(
                low_1 - low, 0) : 0;

        // The daily calculations are volatile and so the data needs to be
        // smoothed. First, sum the last N periods for TR, +DM and - DM
        double trSum = 0;
        double dmPlusSum = 0;
        double dmMinusSum = 0;
        for (int i = 0; i < period; i++) {
            trSum += tr[i];
            dmPlusSum += dmPlus[i];
            dmMinusSum += dmMinus[i];
        }

        // The smoothing formula subtracts 1/Nth of yesterday's trN from
        // yesterday's trN and then adds today's TR value
        // The truncating function is used to calculate the indicator as close
        // as possible to the developer of the ADX's original form of
        // calculation (which was done by hand).
        trN[period - 1] = ((int) (1000D * (trN[period - 2]
                - (trN[period - 2] / period) + trSum))) / 1000D;
        dmPlusN[period - 1] = ((int) (1000D * (dmPlusN[period - 2]
                - (dmPlusN[period - 2] / period) + dmPlusSum))) / 1000D;
        dmMinusN[period - 1] = ((int) (1000D * (dmMinusN[period - 2]
                - (dmMinusN[period - 2] / period) + dmMinusSum))) / 1000D;

        // Now we have a 14-day smoothed sum of TR, +DM and -DM.
        // The next step is to calculate the ratios of +DM and -DM to TR.
        // The ratios are called the +directional indicator (+DI) and
        // -directional indicator (-DI).
        // The integer function (int) is used because the original developer
        // dropped the values after the decimal in the original work on the ADX
        // indicator.
        double diPlus = (int) (100D * dmPlusN[period - 1] / trN[period - 1]);
        double diMinus = (int) (100D * dmMinusN[period - 1] / trN[period - 1]);
        ;

        // The next step is to calculate the absolute value of the difference
        // between the +DI and the -DI and the sum of the +DI and -DI.
        double diDiff = Math.abs(diPlus - diMinus);
        double diSum = diPlus + diMinus;

        // The next step is to calculate the DX, which is the ratio of the
        // absolute value of the difference between the +DI and the -DI divided
        // by the sum of the +DI and the -DI.
        dx[period - 1] = (int) (100D * (diDiff / diSum));

        // The final step is smoothing the DX to arrive at the value of the ADX.
        // First, average the last N days of DX values
        double dxMedia = 0;
        for (int i = 0; i < period; i++) {
            dxMedia += dx[i];
        }
        dxMedia /= period;

        // The smoothing process uses yesterday's ADX value multiplied by N-1,
        // and then add today's DX value. Finally, divide this sum by N.
        if (counter == 2 * (period - 1)) {
            adx[period - 2] = dxMedia;
        }
        adx[period - 1] = (adx[period - 2] * (period - 1) + dx[period - 1])
                / period;

        counter++;

        return adx[period - 1];
    }

    public static double normalize(double value) {
        NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize, null, 100, 0, 1.0,
                -1.0);

        return normalizer.normalize(value);
    }


}