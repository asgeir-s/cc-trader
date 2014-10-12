package com.cctrader.systems.ann.fourWayClassify

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator
import com.cctrader.indicators.technical._
import com.typesafe.config.ConfigFactory
import org.encog.engine.network.activation.{ActivationSigmoid, ActivationCompetitive, ActivationTANH}
import org.encog.ml.data.basic.{BasicMLData, BasicMLDataSet}
import org.encog.ml.data.{MLData, MLDataSet}
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing
import org.encog.neural.networks.training.propagation.back.Backpropagation
import org.encog.neural.networks.training.{Train, TrainingSetScore}

/**
 *
 */
class FourWayClassifyANN(settingsPath: String) {
  println("ForwardIndicator has started")
  var network = new BasicNetwork

  // sett configs
  val config = ConfigFactory.load(settingsPath)
  var trainingIterations = config.getInt("initialTrainingIterations")
  val neuronsInHiddenLayer1: Int = config.getInt("ml.neuronsInLayer1")
  val neuronsInHiddenLayer2: Int = config.getInt("ml.neuronsInLayer2")
  val neuronsInHiddenLayer3: Int = config.getInt("ml.neuronsInLayer3")
  val learningRate: Double = config.getDouble("ml.learningRate")
  val momentum: Double = config.getDouble("ml.momentum")
  val pointsToLookAhed: Int = config.getInt("ml.pointsToLookAhed")
  val numberOfInputPeriods: Int = config.getInt("ml.numberOfInputPeriods")
  val normalizeInput = config.getBoolean("ml.normalizeInput")
  var initialtraining = true

  // inputs
  val stochasticK = new StochasticK(config.getInt("indicators.stochasticK"))
  val stochasticD = new StochasticD(stochasticK, config.getInt("indicators.stochasticD"))
  //val roc =

  val indicatorsINPUT: List[InputIndicator] = List(
    new AccumulationDistributionOscillator,
    new AroonOscillator(config.getInt("indicators.aroonOscillator")),
    new DisparityIndex(config.getInt("indicators.disparityIndex")),
    new Momentum(config.getInt("indicators.momentum")),
    new MovingAverageExponentialConvergence(config.getInt("indicators.movingAverageExponentialConvergenceFast"), config.getInt("indicators.movingAverageExponentialConvergenceSlow")),
    new PriceOscillator(config.getInt("indicators.priceOscillatorFast"), config.getInt("indicators.priceOscillatorSlow")),
    new RateOfChange(config.getInt("indicators.rateOfChange")),
    new RelativeStrengthIndex(config.getInt("indicators.relativeStrengthIndex")),
    stochasticK,
    stochasticD,
    new StochasticSlowD(stochasticD, config.getInt("indicators.stochasticSlowD")),
    new VolumeOscillator(config.getInt("indicators.volumeOscillatorFast"), config.getInt("indicators.volumeOscillatorSlow")),
    new WilliamsR(config.getInt("indicators.williamsR"))//,
  )

  private final val pointsNeededToCompute: Int = numberOfInputPeriods * config.getInt("pointsNeededToCompute") + 1

  //Builds the network
  network.addLayer(new BasicLayer(new ActivationTANH, false, indicatorsINPUT.size * numberOfInputPeriods))

  if (neuronsInHiddenLayer1 > 0) {
    network.addLayer(new BasicLayer(new ActivationTANH, true, neuronsInHiddenLayer1))
    println("Add hidden layer #1, with size: " + neuronsInHiddenLayer1)
  }
  if (neuronsInHiddenLayer2 > 0) {
    network.addLayer(new BasicLayer(new ActivationTANH, true, neuronsInHiddenLayer2))
    println("Add hidden layer #2, with size: " + neuronsInHiddenLayer2)
  }
  if (neuronsInHiddenLayer3 > 0) {
    network.addLayer(new BasicLayer(new ActivationTANH, true, neuronsInHiddenLayer3))
    println("Add hidden layer #3, with size: " + neuronsInHiddenLayer3)
  }
  network.addLayer(new BasicLayer(new ActivationSigmoid, false, 4)) //ActivationCompetitive
  network.getStructure.finalizeStructure()
  network.reset()

  def inputMaker(index: Int, data: MarketDataSet): Array[Double] = {

    var input: Array[Double] = Array[Double]()
    if(normalizeInput) {
      for (j <- 0 until numberOfInputPeriods) {
        input = input ++: indicatorsINPUT.map(x => x.getReScaled(index-j, data)).toArray
      }
    }
    else {
      for (j <- 0 until numberOfInputPeriods) {
        input = input ++: indicatorsINPUT.map(x => x(index-j, data)).toArray
      }
    }
    input
  }

  /**
   * Train the network with this MarketDataSet as training set.
   * @param data training set
   * @return the final error of the network
   */
  def train(data: MarketDataSet): Double = {

    // setting max and min input based on training-set
    if(initialtraining && normalizeInput) {
      indicatorsINPUT.foreach(_.setNormalizationBounds(data, pointsNeededToCompute))
      indicatorsINPUT.foreach(_.normOutRange(-1, 1))
    }

    val mlDataSet: MLDataSet = {
      println("data.size:" + data.size + ", pointsNeededToCompute:" + pointsNeededToCompute + ", pointsToLookAhed:" + pointsToLookAhed)
      val input: Array[Array[Double]] = new Array[Array[Double]](data.size - pointsNeededToCompute - pointsToLookAhed)
      val ideal: Array[Array[Double]] = new Array[Array[Double]](data.size - pointsNeededToCompute - pointsToLookAhed)

      for (i <- pointsNeededToCompute until data.size - pointsToLookAhed) {
        input(i - pointsNeededToCompute) = inputMaker(i, data)
        ideal(i - pointsNeededToCompute) = idealOUTPUTClassify(data, i)
        if(i%100 == 0) {
          println("Input #" + i + " (size:" + input(i - pointsNeededToCompute).size + "):")
          println(input(i - pointsNeededToCompute).toVector)
          println("Output #" + i + ":")
          println(ideal(i - pointsNeededToCompute).toVector)
        }
      }
      new BasicMLDataSet(input, ideal)
    }
    println("START TRAINING")

    val train: Train = new Backpropagation(network, mlDataSet, learningRate, momentum)


    var lastError: Double = Double.MaxValue
    var lastAnneal: Int = 0
    var epoch = 0
    do {
      train.iteration()

      val error: Double = train.getError

      if(epoch%1000==0) {
        println("Iteration(Backprop) #" + epoch + " Error:" + error)
      }
      /*
      if (error > 0.05) {
        if ((lastAnneal > 30) && (error > lastError || Math.abs(error - lastError) < 0.0001)) {
          trainNetworkAnneal(mlDataSet)
          lastAnneal = 0
        }
      }
      */
      lastError = train.getError
      lastAnneal += 1
      epoch += 1
    } while (epoch < trainingIterations)

    trainingIterations = config.getInt("laterTrainingIterations")
    println("Training done! Final error: " + train.getError)
    initialtraining = false
    train.getError
  }

  /**
   * Train the network using Simulated Anniling
   *
   * @param mlDataSet the MLDataSet to use for training
   */
  private def trainNetworkAnneal(mlDataSet: MLDataSet) {
    System.out.println("Training with simulated annealing for 5 iterations")
    val train: NeuralSimulatedAnnealing = new NeuralSimulatedAnnealing(network, new TrainingSetScore(mlDataSet), 10, 2, 100)
    for (epoch <- 0 until 5) {
      train.iteration()
      System.out.println("Iteration(Anneal) #" + epoch + " Error:" + train.getError)
    }
  }

  /**
   * Predict.
   *
   * @param data marketDataSet to predict the feature of
   * @return the prediction. Based on the selected idealOUTPUT
   */
  def apply(data: MarketDataSet): Array[Double] = {
    network.compute(new BasicMLData(inputMaker(data.size-1, data))).getData
  }


  /**
   * Possible idealOUTPUT:
   * @param marketDataSet the marketDataSet to use for computing the ideal output
   * @param index the index of the point that should predict the output
   * @return the predicted output
   */
  private def idealOUTPUTClassify(marketDataSet: MarketDataSet, index: Int): Array[Double] = {
    val diff = marketDataSet(index + pointsToLookAhed).close - marketDataSet(index).close
    val percentDiff = (100D/marketDataSet(index).close) * diff
    var array = Array(0D,0D,0D,0D)

    if(percentDiff >= 2) {
      array = Array(1D,0D,0D,0D)
    }
    else if(percentDiff <= -2) {
      array = Array(0D,0D,0D,1D)
    }
    else if(percentDiff > 0 && percentDiff < 2) {
      array = Array(0D,1D,0D,0D)
    }
    else if(percentDiff < 0 && percentDiff > -2) {
      array = Array(0D,0D,1D,0D)
    }
    array
  }

}
