package com.cctrader.indicators.machin;

import com.cctrader.data.MarketDataSet;

/**
 *
 */
public interface MachineIndicator {
    /**
     * @param data marketDataSet to use for training
     * @return final error rate after training on input data
     */
    public double train(MarketDataSet data);

    /**
     * @param data marketDataSet to evaluate
     * @return can return change in percent/absolute change or expected price (depends on indicator)
     */
    public double compute(MarketDataSet data);
}
