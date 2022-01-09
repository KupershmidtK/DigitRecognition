package recognition;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

class Weights implements Serializable {
    private static final long serialVersionUID = 1L;

    public double[] values;

    public Weights() {
        values = new double[16];
        Random random = new Random();
        for (int i = 0; i < values.length; i++) {
            //values[i] = 0d;
            values[i] = random.nextGaussian() / 10;
        }
    }
}

class NeuronNet implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient int generations = 10;

    private transient Scanner scanner;
    private ArrayList<Weights> weights;

    private transient int[][] firstLayer;
    private transient final static int N = 5;
    private transient final static int M = 3;

    private transient double[] outputLayer;

    public double getOutputLayer(int i) {
        return outputLayer[i];
    }

    public double[] getOutputLayer() {
        return outputLayer;
    }

    public NeuronNet() {
        weights = new ArrayList<>(10);
        for(int i = 0; i < 10; i++) {
            weights.add(new Weights());
        }
        Initialize();
    }

    private void Initialize() {
        scanner = new Scanner(System.in);
        firstLayer = new int[N][M];
        outputLayer = new double[10];
    }

    private void writeObject(ObjectOutputStream oos) throws Exception {
        oos.writeObject(weights);
    }

    private void readObject(ObjectInputStream ois) throws Exception {
        //ois.defaultReadObject();
        weights = (ArrayList) ois.readObject();
        Initialize();
    }
/////// LEARNING //////////////////////////////////////////////////////
    private double sigmoid(double val) {
        return 1.0 / (1.0 + Math.pow(Math.E, -val));
    }

    private double calculateNeuronForNumber(int neuronIndex, int numberIndex) {
        Weights w = weights.get(neuronIndex);
        int[] number = LearningData.numbers[numberIndex];

        double res = 0d;
        for(int i = 0; i < 15; i++) {
            res += w.values[i] * number[i];
        }
        res += w.values[15]; // add bias
        return sigmoid(res);
    }

    private void calculateWeightsDelta(double[] deltaValues, int neuronIndex, int numberIndex, double neuronValue) {
        int[] number = LearningData.numbers[numberIndex];
        int[] correctNumber = LearningData.numbers[neuronIndex];
        for (int i = 0; i < deltaValues.length; i++) {
            deltaValues[i] += 0.5 * number[i] * (correctNumber[i] - neuronValue);
        }
    }

    public void learningNeuron(int outNeuronIndex) {
        double[] delta = new double[15];
        for (int numberIndex = 0; numberIndex < 10; numberIndex++) {
            double neuronVal = calculateNeuronForNumber(outNeuronIndex, numberIndex);
            //Arrays.fill(delta, 0.0);
            calculateWeightsDelta(delta, outNeuronIndex, numberIndex, neuronVal);
        }

        Weights w = weights.get(outNeuronIndex);
            // calculate mean delta and upgrade weight for neuron
        for (int i = 0; i < delta.length; i++) {
            delta[i] /= 10.0;
            w.values[i] += delta[i];
        }
        //System.out.println(Arrays.toString(delta));
    }

    public void learningNetwork() {
        for (int i = 0; i < generations; i++) {
            for (int outputNeuronIndex = 0; outputNeuronIndex < 10; outputNeuronIndex++) {
                learningNeuron(outputNeuronIndex);
            }
            // learningNeuron(0);
        }
    }
///// END LEARNING ////////////////////////////////////////

    @Override
    public String toString() {
        return weights.get(0).toString();
    }

    public void readInput() {
        for (int i = 0; i < N; i++) {
            String line = scanner.nextLine();
            for (int j = 0; j < M; j++) {
                firstLayer[i][j] = line.charAt(j) == 'X' ? 1 : 0;
            }
        }
    }

    public int recognize() {
        calculateOutput();
        int res = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < 10; i++) {
            if(outputLayer[i] > max) {
                max = outputLayer[i];
                res = i;
            }
        }
        return res;
    }

    private void calculateOutput() {
        for (int i = 0; i < 10; i++) {
            outputLayer[i] = calculateNeuron(i);
        }
    }

    private double calculateNeuron(int n) {
        double res = 0d;
        Weights w = weights.get(n);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                res += firstLayer[i][j] * w.values[i*M + j];
            }
        }
        return sigmoid(res);
    }

/*
    private int calculateNeuron(int n) {
        int res = bias[n];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                res += firstLayer[i][j] * weights[n][i*M + j];
            }
        }
        return res;
    }

 */
}
