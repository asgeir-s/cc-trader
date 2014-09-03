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
import org.encog.neural.networks.training.lma.LevenbergMarquardtTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.simple.EncogUtility;


/**
 * A Encog ANN trading-system not using any of Encogs-Market classes.
 * Predict the change in percent
 */
public class ANNOnePeriodAhead implements MachineIndicator {

    private BasicNetwork network;
    private final int POINTS_LOOK_AHEAD = 1;
    private final int HIDDEN_1_NEURONS = 5;
    private final int HIDDEN_2_NEURONS = 3;
    private final Double MAX_ERROR = 0.001;
    private final int pointsNeededToCompute = 30;

    /**
     * @param data marketDataSet to use for training
     * @return final error rate after training on input data
     */
    public double train(MarketDataSet data) {
        // creating input and output data
        System.out.println("Creating input and output data:");
        double[][] input = new double[data.size() - pointsNeededToCompute - 1][]; // -1 because cant se one after last point
        double[][] ideal = new double[data.size() - pointsNeededToCompute - 1][];
        System.out.println("Length of arrays:" + input.length);
        int count = 0;

        System.out.println("For min:" + pointsNeededToCompute + ", for max:" + data.size() + ", range:" + (data.size() - pointsNeededToCompute));

        for (int i = pointsNeededToCompute; i < data.size() - 1; i++) {
            input[count] = getIndicatorArrayForIndex(data, i);
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
        network.addLayer(new BasicLayer(mlDataSet.getInputSize()));

        network.addLayer(new BasicLayer(HIDDEN_1_NEURONS));
        network.addLayer(new BasicLayer(HIDDEN_2_NEURONS));
        //network.addLayer(new BasicLayer(2));

        network.addLayer(new BasicLayer(mlDataSet.getIdealSize()));
        network.getStructure().finalizeStructure();
        network.reset();

        // training
        System.out.println("Start training:");
        final LevenbergMarquardtTraining train = new LevenbergMarquardtTraining(network, mlDataSet);

        int resets = 0;
        int epoch = 1;
        do {
            train.iteration();
            if (epoch % 1000 == 0 || epoch < 50)
                System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
            if(epoch > 10000) {
                if(train.getError() > MAX_ERROR && resets < 0) {
                    network.reset();
                    epoch = 1;
                    resets++;
                    System.out.println("RESET TRAINING");
                }
                else {
                    break;
                }

            }
        } while (train.getError() > MAX_ERROR);
        System.out.println("Training done!");

        return train.getError();
    }


    /**
     * @param data marketDataSet to evaluate
     * @return can return change in percent/absolute change or expected price (depends on indicator)
     */
    public double compute(MarketDataSet data) {
        MLData predictData = network.compute(new BasicMLData(getIndicatorArrayForIndex(data, data.size() - 1)));
        Double predictMomentum = predictData.getData(0);
        return predictMomentum;
    }

    /**
     * @param index for training: the point to create data for. For computing index should always be the last point in the MarketDataSet
     * @return a Double array with the normalized input values.
     */
    private double[] getIndicatorArrayForIndex(MarketDataSet marketDataSet, int index) {
        double[] indicatorArray = new double[13];

        AccumulationDistributionOscillator accumulationDistributionOscillator = new AccumulationDistributionOscillator();
        AroonOscillator aroonOscillator = new AroonOscillator(25);
        Disparity disparity = new Disparity(10);
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

        indicatorArray[0] = accumulationDistributionOscillator.calculate(index, marketDataSet);
        indicatorArray[1] = aroonOscillator.calculate(index, marketDataSet)/100D;
        indicatorArray[2] = disparity.calculate(index, marketDataSet)/100D;
        indicatorArray[3] = momentum.calculate(index, marketDataSet)/100D;
        indicatorArray[4] = maec.calculate(index, marketDataSet)/100D;
        indicatorArray[5] = priceOscillator.calculate(index, marketDataSet); // Stryrer alt
        indicatorArray[6] = rateOfChange.calculate(index, marketDataSet)/100D;
        indicatorArray[7] = relativeStrengthIndex.calculate(index, marketDataSet)/100D;
        indicatorArray[8] = stochasticK.calculate(index, marketDataSet)/100D;
        indicatorArray[9] = stochasticD.calculate(index, marketDataSet)/100D;
        indicatorArray[10] = stochasticSlowD.calculate(index, marketDataSet)/100D;
        indicatorArray[11] = volumeOscillator.calculate(index, marketDataSet); // Stryrer alt
        indicatorArray[12] = williamsR.calculate(index, marketDataSet)/100D;

        for (int i = 0; i < indicatorArray.length; i++) {
            System.out.println("Indicator " + i + ": " + indicatorArray[i]);
        }

        return indicatorArray;
    }

    private double[] correctTrainingOutput(MarketDataSet marketDataSet, int index) {
        double[] correctOutput = new double[1];
        Momentum momentum = new Momentum(3);
        correctOutput[0] = momentum.calculate(index + POINTS_LOOK_AHEAD, marketDataSet)/100D;
        System.out.println("Output: " + correctOutput[0]);
        return correctOutput;
    }

}