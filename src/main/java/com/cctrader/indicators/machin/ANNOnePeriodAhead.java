package com.cctrader.indicators.machin;

import com.cctrader.data.MarketDataSet;
import com.cctrader.indicators.technical.*;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;


/**
 * A Encog ANN trading-system not using any of Encogs-Market classes.
 * Predict the change in percent
 */
public class ANNOnePeriodAhead implements MachineIndicator {

    private BasicNetwork network;
    private final int POINTS_LOOK_AHEAD = 1;
    private final int HIDDEN_1_NEURONS = 10;
    private final int HIDDEN_2_NEURONS = 5;
    private final Double MAX_ERROR = 0.000003;
    private final int pointsNeededToCompute = 30;

    /**
     * @param data marketDataSet to use for training
     * @return final error rate after training on input data
     */
    public double train(MarketDataSet data) {
        // creating input and output data
        System.out.println("Creating input and output data:");
        double[][] input = new double[data.size()-pointsNeededToCompute-1][]; // -1 because cant se one after last point
        double[][] ideal = new double[data.size()-pointsNeededToCompute-1][];
        System.out.println("Length of arrays:" + input.length);
        int count = 0;

        System.out.println("For min:" + pointsNeededToCompute + ", for max:" + data.size() + ", range:" + (data.size() - pointsNeededToCompute));

        for(int i = pointsNeededToCompute; i < data.size()-1; i++) {
            input[count] = getIndicatorArrayForIndex(data, i);
            ideal[count] = correctTrainingOutput(data, i);
            if (count % 1000 == 0) {
                System.out.println("Creating input and output data #" + count + ".");
            }
            count++;
        }
        System.out.println("Loop exit");

        MLDataSet mlDataSet = new BasicMLDataSet(input, ideal);

        // creating network
        System.out.println("Creating network:");
        network = new BasicNetwork();
        network.addLayer(new BasicLayer(mlDataSet.getInputSize()));

        network.addLayer(new BasicLayer(HIDDEN_1_NEURONS));
        network.addLayer(new BasicLayer(HIDDEN_2_NEURONS));

        network.addLayer(new BasicLayer(mlDataSet.getIdealSize()));
        network.getStructure().finalizeStructure();
        network.reset();

        // training
        System.out.println("Start training:");
        final Train train = new ResilientPropagation(network, mlDataSet);

        int epoch = 1;
        do {
            train.iteration();
            if (epoch % 1000 == 0)
                System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while (train.getError() > MAX_ERROR);
        System.out.println("Training done!");

        return train.getError();
    }


    /**
     * @param data marketDataSet to evaluate
     * @return can return change in percent/absolute change or expected price (depends on indicator)
     */
    public double compute(MarketDataSet data) {
        MLData predictData = network.compute(new BasicMLData(getIndicatorArrayForIndex(data, data.size()-1)));
        Double predictedChange = predictData.getData(0);
        return predictedChange;
    }

    /**
     *
     * @param index for training: the point to create data for. For computing index should always be the last point in the MarketDataSet
     * @return a Double array with the normalized input values.
     */
    private double[] getIndicatorArrayForIndex(MarketDataSet marketDataSet, int index) {
        double[] indicatorArray = new double[9];

        Double aroonOscillator = AroonOscillator.get(index, 21, marketDataSet);
        indicatorArray[0] = AroonOscillator.normalize(aroonOscillator);

        Double averageDirectionalIndex = AverageDirectionalIndex.get(index, 20, marketDataSet);
        if (averageDirectionalIndex > 30) {
            indicatorArray[1] = 1;
        }
        else {
            indicatorArray[1] = 0;
        }

        // moving average exponential convergence
        Double exponentialMovingAverageSlow = ExponentialMovingAverage.get(index, 26, marketDataSet);
        Double exponentialMovingAverageFast = ExponentialMovingAverage.get(index, 9, marketDataSet);
        if (exponentialMovingAverageSlow < exponentialMovingAverageFast) {
            indicatorArray[2] = 1;
        }
        else if (exponentialMovingAverageSlow > exponentialMovingAverageFast) {
            indicatorArray[2] = -1;
        }


        Double accumulationDistributionLine = AccumulationDistributionLine.get(index, 20, marketDataSet);
        if(accumulationDistributionLine < -0.01) {
            indicatorArray[3] = -1;
        }
        else if(accumulationDistributionLine > 0.01) {
            indicatorArray[3] = 1;
        }
        else {
            indicatorArray[3] = 0;
        }

        // simple moving average change between last close and this close
        Double[] simpleMovingAverageThis = SimpleMovingAverage.get(index, 3, marketDataSet);
        Double[] simpleMovingAverageLast = SimpleMovingAverage.get(index-1, 3, marketDataSet);
        indicatorArray[4] = marketDataSet.sigmoidNormalizerPriceChange(simpleMovingAverageThis[0] - simpleMovingAverageLast[0]);
        // simple moving average change between last and this average volume
        indicatorArray[5] = marketDataSet.sigmoidNormalizerVolumeChange(simpleMovingAverageThis[1] - simpleMovingAverageLast[1]);
        // change between last close and this close
        indicatorArray[6] = marketDataSet.sigmoidNormalizerPriceChange(marketDataSet.apply(index).close() - marketDataSet.apply(index-1).close());
        // change between last close and this close (1 back)
        indicatorArray[7] = marketDataSet.sigmoidNormalizerPriceChange(marketDataSet.apply(index-1).close() - marketDataSet.apply(index-2).close());
        // change between last close and this close (2 back)
        indicatorArray[8] = marketDataSet.sigmoidNormalizerPriceChange(marketDataSet.apply(index-2).close() - marketDataSet.apply(index-3).close());

        //printer
      //  System.out.println("DataPoint:" + marketDataSet.apply(index));
      //  for (int i = 0; i < indicatorArray.length; i++) {
      //      System.out.println(i + " = " + indicatorArray[i]);
      //  }

        return indicatorArray;
    }

    private double[] correctTrainingOutput(MarketDataSet marketDataSet, int index) {
        double[] correctOutput = new double[1];
        correctOutput[0] = marketDataSet.sigmoidNormalizerPriceChange(marketDataSet.apply(index+1).close() - marketDataSet.apply(index).close());
        return correctOutput;
    }


}