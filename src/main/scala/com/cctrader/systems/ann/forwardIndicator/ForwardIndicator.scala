package com.cctrader.systems.ann.forwardIndicator

import com.cctrader.data.MarketDataSet
import com.cctrader.indicators.InputIndicator
import com.cctrader.indicators.fundamental.MovingAverageTransactionsPerBlockOscillator
import com.cctrader.indicators.technical._
import com.typesafe.config.ConfigFactory
import org.encog.engine.network.activation.{ActivationSigmoid, ActivationTANH}
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
class ForwardIndicator(settingsPath: String) {
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
  val stochasticK = new StochasticK(10)
  val stochasticD = new StochasticD(stochasticK, 3)

  val indicatorsINPUT: List[InputIndicator] = List(
    new AccumulationDistribution,               // kan ikke normalizered nå
    new AroonOscillator(25),
    new DisparityIndex(10),                                    // kan ikke normalizered nå
    new Momentum(5),                                      // kan ikke normalizered nå
    new MovingAverageExponentialConvergence(9, 26),       // kan ikke normalizered nå
    new PriceOscillator(9, 26),                           // kan ikke normalizered nå
    new RateOfChange(10),                                 // kan ikke normalizered nå
    new RelativeStrengthIndex(15),
    stochasticK,
    stochasticD,
    new StochasticSlowD(stochasticD, 6),
    new VolumeOscillator(9, 26),                          // kan ikke normalizered nå
    new WilliamsR(25)//,
    //new MovingAverageTransactionsPerBlockOscillator(9, 26)// kan ikke normalizered nå
  )

  private final val pointsNeededToCompute: Int = numberOfInputPeriods * 26 + 1
 /*
  // NORMALIZED INPUT
val indicatorsINPUT: List[Normalizable] = List(
  new AroonOscillator(25),
  new RelativeStrengthIndex(15),
  stochasticK,
  stochasticD,
  new StochasticSlowD(stochasticD, 5),
  new WilliamsR(25)
)
*/



  //outputHelper
  val movingAveragePriceOut: MovingAveragePrice = new MovingAveragePrice(3) //10
  val relativeStrengthIndex: RelativeStrengthIndex = new RelativeStrengthIndex(10)
  relativeStrengthIndex.normOutRange(-1, 1) // TODO: should match activation function

  /**
   * Check that the number is a valide number between 1 and -1,
   * if not sett it to 1 if number > 1 and
   * -1 if number < -1.
   * It not a number set to 0
   *
   * num the number
   * @return a value between -1 and 1

  def normal(num: Double): Double = {
    if (num > 1) {1}
    else if (num < -1) {-1}
    else if (num > -1 && num < 1) {num}
    else {0}
  }
  */


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
  network.addLayer(new BasicLayer(new ActivationTANH, false, 1))
  network.getStructure.finalizeStructure()
  network.reset()

  def inputMaker(index: Int, data: MarketDataSet): Array[Double] = {
    var input: Array[Double] = Array[Double]()
    if(normalizeInput) {
      for (j <- 0 until numberOfInputPeriods) {
        input = input ++: indicatorsINPUT.map(x => x.getNormalized(index-j, data)).toArray
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
      val input: Array[Array[Double]] = new Array[Array[Double]](data.size - pointsNeededToCompute - pointsToLookAhed)
      val ideal: Array[Array[Double]] = new Array[Array[Double]](data.size - pointsNeededToCompute - pointsToLookAhed) //TODO: put in config

      for (i <- pointsNeededToCompute until data.size - pointsToLookAhed) {
        input(i - pointsNeededToCompute) = inputMaker(i, data)
        ideal(i - pointsNeededToCompute) = idealOUTPUTRelativeStrengthIndex(data, i)
        if(i%100 == 0) {
          println("Input #" + i + " (size:" + input(i - pointsNeededToCompute).size + "):")
          println(input(i - pointsNeededToCompute).toVector)
          println("Output #" + i + ":")
          println(ideal(i - pointsNeededToCompute).toVector)
        }
      }
      new BasicMLDataSet(input, ideal)
    }

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
  def apply(data: MarketDataSet): Double = {
    val predictData: MLData = network.compute(new BasicMLData(inputMaker(data.size-1, data)))
    val predict: Double = predictData.getData(0)
    predict
  }


  /**
   * Possible idealOUTPUT:
   * @param marketDataSet the marketDataSet to use for computing the ideal output
   * @param index the index of the point that should predict the output
   * @return the predicted output
   */
  private def idealOUTPUTMaxClose(marketDataSet: MarketDataSet, index: Int): Array[Double] = {
    var maxClose: Double = 0
    for (i <- 0 until pointsToLookAhed) {
      if (marketDataSet(index + i).close > maxClose) {
        maxClose = marketDataSet.apply(index + i).close
      }
    }
    Array((maxClose - marketDataSet(index).close) / marketDataSet(index).close)
  }

  /**
   * Possible idealOUTPUT:
   * @param marketDataSet the marketDataSet to use for computing the ideal output
   * @param index the index of the point that should predict the output
   * @return the predicted output
   */
  private def idealOUTPUTMMaxMovingAverage(marketDataSet: MarketDataSet, index: Int): Array[Double] = {
    var maxMovingAverage: Double = 0
    for (i <- 0 until pointsToLookAhed) {
      if (movingAveragePriceOut(index + i, marketDataSet) > maxMovingAverage) {
        maxMovingAverage = movingAveragePriceOut(index + i, marketDataSet)
      }
    }
    Array((maxMovingAverage - marketDataSet.apply(index).close) / marketDataSet(index).close)
  }

  /**
   * Possible idealOUTPUT:
   * @param marketDataSet the marketDataSet to use for computing the ideal output
   * @param index the index of the point that should predict the output
   * @return the predicted output
   */
  private def idealOUTPUTMovingAverage(marketDataSet: MarketDataSet, index: Int): Array[Double] = {
    Array((movingAveragePriceOut(index + pointsToLookAhed, marketDataSet) - marketDataSet(index).close) / marketDataSet(index).close)
  }

  /**
   * Possible idealOUTPUT:
   * @param marketDataSet the marketDataSet to use for computing the ideal output
   * @param index the index of the point that should predict the output
   * @return the predicted output
   */
  private def idealOUTPUTRelativeStrengthIndex(marketDataSet: MarketDataSet, index: Int): Array[Double] = {
    Array(relativeStrengthIndex.getNormalized(index + pointsToLookAhed, marketDataSet))
  }

}
