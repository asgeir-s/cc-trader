package com.cctrader.indicators.machin;

import com.cctrader.data.MarketDataSet;
import com.cctrader.data.TSSettings;
import com.cctrader.indicators.fundamental.*;
import com.cctrader.indicators.technical.*;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;


/**
 * A Encog ANN trading-system not using any of Encogs-Market classes.
 * Predict the change in percent
 */
public class ANNOnePeriodAhead implements MachineIndicator {

    private TSSettings tsSettings;
    private BasicNetwork network;
    private final int POINTS_LOOK_AHEAD = 10;
    private final int pointsNeededToCompute = 30;

    //Input:
    AccumulationDistributionOscillator accumulationDistribution = new AccumulationDistributionOscillator();
    AroonOscillator aroonOscillator = new AroonOscillator(25);
    DisparityIndex disparityIndex = new DisparityIndex(10);
    Momentum momentum = new Momentum(5);
    MovingAverageExponentialConvergence maec = new MovingAverageExponentialConvergence(9, 26);
    PriceOscillator priceOscillator = new PriceOscillator(9, 26);
    RateOfChange rateOfChange = new RateOfChange(10);
    RelativeStrengthIndex relativeStrengthIndex = new RelativeStrengthIndex(15);
    StochasticK stochasticK = new StochasticK(10);
    StochasticD stochasticD = new StochasticD(stochasticK, 3);
    StochasticSlowD stochasticSlowD = new StochasticSlowD(stochasticD, 6);
    VolumeOscillator volumeOscillator = new VolumeOscillator(9, 26);
    WilliamsR williamsR = new WilliamsR(25);
    MovingAverageTransactionsPerBlockOscillator movingAverageTransactionsPerBlockOscillator = new MovingAverageTransactionsPerBlockOscillator(9, 26);
    NumberOfBTCTransactionsOscillator numberOfBTCTransactionsOscillator = new NumberOfBTCTransactionsOscillator(9, 26);

    //Output:
    MovingAveragePrice movingAveragePriceOut = new MovingAveragePrice(8);


    public ANNOnePeriodAhead(TSSettings tsSettingsIn) {
        tsSettings = tsSettingsIn;
    }

    public String getFromMachineLearningSettings(String key) {
        return tsSettings.machineLearningSettings().get(key).get();
    }

    /**
     * @param data marketDataSet to use for training
     * @return final error rate after training on input data
     */
    public double train(MarketDataSet data) {
        // creating input and output data
        System.out.println("Creating input and output data:");
        double[][] input = new double[data.size() - pointsNeededToCompute - POINTS_LOOK_AHEAD][]; // -1 because cant se one after last point
        double[][] ideal = new double[data.size() - pointsNeededToCompute - POINTS_LOOK_AHEAD][];
        System.out.println("Length of arrays:" + input.length);
        int count = 0;

        System.out.println("For min:" + pointsNeededToCompute + ", for max:" + data.size() + ", range:" + (data.size() - pointsNeededToCompute));

        for (int i = pointsNeededToCompute; i < data.size() - POINTS_LOOK_AHEAD; i++) {
            input[count] = getIndicatorArrayForIndex(data, i); // for Bitcoin: getIndicatorArrayForIndex for Stock(Yahoo): getIndicatorArrayForIndexCloseOnly
            ideal[count] = correctTrainingOutput(data, i);
            if (count % 1000 == 0) {
                System.out.println("Creating input and output data #" + count + ".");
            }
            count++;
        }
        System.out.println("Loop exit");

        System.out.println("input size:" + input.length);
        System.out.println("ideal size:" + ideal.length);

        System.out.println("input[1] size:" + input[1].length);
        System.out.println("ideal[1] size:" + ideal[1].length);

        MLDataSet mlDataSet = new BasicMLDataSet(input, ideal);

        // creating network
        System.out.println("Creating network:");
        network = new BasicNetwork();
        network.addLayer(new BasicLayer(new ActivationTANH(), true, mlDataSet.getInputSize()));

        int neuronsInHiddenLayer1 = Integer.parseInt(getFromMachineLearningSettings("neuronsInLayer1"));
        int neuronsInHiddenLayer2 = Integer.parseInt(getFromMachineLearningSettings("neuronsInLayer2"));
        int neuronsInHiddenLayer3 = Integer.parseInt(getFromMachineLearningSettings("neuronsInLayer3"));

        if (neuronsInHiddenLayer1 > 0) {
            network.addLayer(new BasicLayer(new ActivationTANH(), true, neuronsInHiddenLayer1));
            System.out.println("Add hidden layer #1, with size: " + neuronsInHiddenLayer1);
        }
        if (neuronsInHiddenLayer2 > 0) {
            network.addLayer(new BasicLayer(new ActivationTANH(), true, neuronsInHiddenLayer2));
            System.out.println("Add hidden layer #2, with size: " + neuronsInHiddenLayer2);
        }
        if (neuronsInHiddenLayer3 > 0) {
            network.addLayer(new BasicLayer(new ActivationTANH(), true, neuronsInHiddenLayer3));
            System.out.println("Add hidden layer #3, with size: " + neuronsInHiddenLayer3);
        }
        network.addLayer(new BasicLayer(new ActivationTANH(), true, mlDataSet.getIdealSize()));
        network.getStructure().finalizeStructure();
        network.reset();

        // training
        System.out.println("Start training:");
        final Train train = new Backpropagation(network, mlDataSet, 0.01, 0.4);
        System.out.println("Training type: Backpropagation");

        double lastError = Double.MAX_VALUE;
        int epoch = 1;
        int lastAnneal = 0;
        do {
            train.iteration();
            double error = train.getError();
            System.out.println("Iteration(Backprop) #" + epoch + " Error:" + error);

            if (error > 0.05) {
                if ((lastAnneal > 100) && (error > lastError || Math.abs(error - lastError) < 0.0001)) {
                    trainNetworkAnneal(mlDataSet);
                    lastAnneal = 0;
                }
            }
            lastError = train.getError();
            epoch++;
            if(epoch > 2000) {
                break;
            }
            lastAnneal++;
        } while (train.getError() > Double.parseDouble(getFromMachineLearningSettings("maxError")));
        System.out.println("Training done! Final error: " + train.getError());

        return train.getError();
    }

    public void continueTrain(MarketDataSet data) {
        // training
        // creating input and output data
        System.out.println("Creating input and output data:");
        double[][] input = new double[data.size() - pointsNeededToCompute - POINTS_LOOK_AHEAD][]; // -1 because cant se one after last point
        double[][] ideal = new double[data.size() - pointsNeededToCompute - POINTS_LOOK_AHEAD][];
        System.out.println("Length of arrays:" + input.length);
        int count = 0;

        System.out.println("For min:" + pointsNeededToCompute + ", for max:" + data.size() + ", range:" + (data.size() - pointsNeededToCompute));

        for (int i = pointsNeededToCompute; i < data.size() - POINTS_LOOK_AHEAD; i++) {
            input[count] = getIndicatorArrayForIndex(data, i); // for Bitcoin: getIndicatorArrayForIndex for Stock(Yahoo): getIndicatorArrayForIndexCloseOnly
            ideal[count] = correctTrainingOutput(data, i);
            if (count % 1000 == 0) {
                System.out.println("Creating input and output data #" + count + ".");
            }
            count++;
        }
        System.out.println("Loop exit");

        System.out.println("input size:" + input.length);
        System.out.println("ideal size:" + ideal.length);

        System.out.println("input[1] size:" + input[1].length);
        System.out.println("ideal[1] size:" + ideal[1].length);

        MLDataSet mlDataSet = new BasicMLDataSet(input, ideal);

        System.out.println("Start training:");
        final Train train = new Backpropagation(network, mlDataSet, 0.01, 0.4);
        System.out.println("Training type: Backpropagation");

        double lastError = Double.MAX_VALUE;
        int epoch = 1;
        int lastAnneal = 0;
        do {
            train.iteration();
            double error = train.getError();
            System.out.println("Iteration(Backprop) #" + epoch + " Error:" + error);

            if (error > 0.05) {
                if ((lastAnneal > 100) && (error > lastError || Math.abs(error - lastError) < 0.0001)) {
                    trainNetworkAnneal(mlDataSet);
                    lastAnneal = 0;
                }
            }
            lastError = train.getError();
            epoch++;
            if(epoch > 2000) {
                break;
            }
            lastAnneal++;
        } while ((train.getError() > Double.parseDouble(getFromMachineLearningSettings("maxError"))));
        System.out.println("Training done! Final error: " + train.getError());
    }

    private void trainNetworkAnneal(MLDataSet mlDataSet) {
        System.out.println("Training with simulated annealing for 5 iterations");

        final NeuralSimulatedAnnealing train = new NeuralSimulatedAnnealing(network, new TrainingSetScore(mlDataSet), 10, 2, 100);

        int epoch = 1;

        for(int i = 0; i<5; i++){
            train.iteration();
            System.out.println("Iteration(Anneal) #" + epoch + " Error:" + train.getError());
            epoch++;
        }
    }


    /**
     * @param data marketDataSet to evaluate
     * @return can return change in percent/absolute change or expected price (depends on indicator)
     */
    public double compute(MarketDataSet data) {
        MLData predictData = network.compute(new BasicMLData(getIndicatorArrayForIndex(data, data.size() - 1)));
        Double predict = predictData.getData(0);
        System.out.println("predict:" + predict);
        return predict;
    }

    /**
     * @param index for training: the point to create data for. For computing index should always be the last point in the MarketDataSet
     * @return a Double array with the normalized input values.
     */
    private double[] getIndicatorArrayForIndex(MarketDataSet marketDataSet, int index) {
        double[] indicatorArray = new double[15];

        indicatorArray[0] = normal(accumulationDistribution.apply(index, marketDataSet));
        indicatorArray[1] = normal(aroonOscillator.apply(index, marketDataSet) / 100D);
        indicatorArray[2] = normal(disparityIndex.apply(index, marketDataSet) / 100D);
        indicatorArray[3] = normal(momentum.apply(index, marketDataSet) / 100D);
        indicatorArray[4] = normal(maec.apply(index, marketDataSet) / 100D);
        indicatorArray[5] = normal(priceOscillator.apply(index, marketDataSet)); // Stryrer alt
        indicatorArray[6] = normal(rateOfChange.apply(index, marketDataSet) / 100D);
        indicatorArray[7] = normal(relativeStrengthIndex.apply(index, marketDataSet) / 100D);
        indicatorArray[8] = normal(stochasticK.apply(index, marketDataSet) / 100D);
        indicatorArray[9] = normal(stochasticD.apply(index, marketDataSet) / 100D);
        indicatorArray[10] = normal(stochasticSlowD.apply(index, marketDataSet) / 100D);
        indicatorArray[11] = normal(volumeOscillator.apply(index, marketDataSet)); // Stryrer alt
        indicatorArray[12] = normal(williamsR.apply(index, marketDataSet) / 100D);
        indicatorArray[13] = normal(numberOfBTCTransactionsOscillator.apply(index, marketDataSet));
        indicatorArray[14] = normal(movingAverageTransactionsPerBlockOscillator.apply(index, marketDataSet));


        for (int i = 0; i < indicatorArray.length; i++) {
            System.out.println("Indicator " + i + ": " + indicatorArray[i]);
        }

        return indicatorArray;
    }

    public double normal(double num){
        if(num > 1) {
            return 1;
        }
        if(num < -1) {
            return -1;
        }
        else {
            return num;
        }
    }

/*
    private double[] correctTrainingOutput(MarketDataSet marketDataSet, int index) {
        double[] correctOutput = new double[1];
        Momentum momentum = new Momentum(3);
        correctOutput[0] = normal(momentum.calculate(index + POINTS_LOOK_AHEAD, marketDataSet) / 100D);
        System.out.println("Output: " + correctOutput[0]);
        return correctOutput;
    }
*/

    private double[] correctTrainingOutput(MarketDataSet marketDataSet, int index) {
        double maxClose = 0;
        for(int i = 0; i < POINTS_LOOK_AHEAD; i++) {
            if(marketDataSet.apply(index+i).close() > maxClose) {
                maxClose = marketDataSet.apply(index+i).close();
            }
        }
        double[] correctOutput = new double[1];
        correctOutput[0] = normal((maxClose - marketDataSet.apply(index).close()) / marketDataSet.apply(index).close());
        System.out.println("Output: " + correctOutput[0]);
        return correctOutput;
    }

/*
    private double[] correctTrainingOutput(MarketDataSet marketDataSet, int index) {
        double maxMovingAverage = 0;
        for(int i = 0; i < POINTS_LOOK_AHEAD; i++) {
            if(movingAveragePriceOut.apply(index + i, marketDataSet) > maxMovingAverage) {
                maxMovingAverage = movingAveragePriceOut.apply(index + i, marketDataSet);
            }
        }
        double[] correctOutput = new double[1];
        correctOutput[0] = normal((maxMovingAverage - marketDataSet.apply(index).close()) / marketDataSet.apply(index).close());
        System.out.println("Output: " + correctOutput[0]);
        return correctOutput;
    }
    */


}