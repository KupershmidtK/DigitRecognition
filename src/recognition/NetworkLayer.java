package recognition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

class NetworkLayerParams implements Serializable {
    public static final long serialVersionUID = 1L;

    private int size;
    private double[][] weights;
    private double[] bias;

    public int getSize() {
        return size;
    }

    public double[][] getWeights() {
        return weights;
    }

    public double[] getBias() {
        return bias;
    }

    public NetworkLayerParams(int size, double[][] weights, double[] bias) {
        this.size = size;
        if(weights != null) { this.weights = weights.clone(); }
        if(bias != null) { this.bias = bias; }
    }
}

class NetworkLayer {
    //// params and getters & setters///////////////////
    private NetworkLayer previousLayer;
    private NetworkLayer nextLayer;

    private int numberOfNeurons;
    private int numberOfPrevNeurons;

    private double[] neuronsValue;
    private double[][] weights;
    private double[] bias;

    private double[] delta;
    private double[] aggregatedDelta;
    private double[][] aggregatedError;

    public double[][] getWeights() {
        return weights;
    }

    public void setWeights(double[][] weights) {
        if (weights != null)
            this.weights = weights.clone();
    }

    public double[] getBias() {
        return bias;
    }

    public void setBias(double[] bias) {
        if (bias != null)
            this.bias = bias.clone();
    }

    public double[] getDelta() {
        return delta;
    }

    public int getNumberOfNeurons() {
        return numberOfNeurons;
    }

    public double[] getNeuronsValue() {
        return neuronsValue;
    }

    public void setNeuronsValue(double[] data) {
        if (neuronsValue.length != data.length) {
            throw new IllegalArgumentException("setNeuronsValue. Incorrect length");
        }
        System.arraycopy(data, 0, neuronsValue, 0, neuronsValue.length);
    }
    ////// Constructor and Initialization //////////////////////////////////////////////
    public NetworkLayer(int numberOfNeurons) {
        this.numberOfNeurons = numberOfNeurons;
        neuronsValue = new double[numberOfNeurons];
        delta = null;
        aggregatedDelta = null;
        aggregatedError = null;

        previousLayer = null;
        nextLayer = null;
        weights = null;
    }

    public void setPreviousLayer(NetworkLayer previousLayer) {
        if (previousLayer != null) {
            this.previousLayer = previousLayer;
            previousLayer.setNextLayer(this);

            numberOfPrevNeurons = previousLayer.getNumberOfNeurons();
            weights = new double[numberOfNeurons][numberOfPrevNeurons];
            bias = new double[numberOfNeurons];
            delta = new double[numberOfNeurons];
            aggregatedDelta = new double[numberOfNeurons];
            aggregatedError = new double[numberOfNeurons][numberOfPrevNeurons];

            initWeights();
        }
    }

    public void setNextLayer(NetworkLayer nextLayer) {
        this.nextLayer = nextLayer;
    }

    private void initWeights() {
        if (weights == null) return;

        Random random = new Random();
        for (int i = 0; i < numberOfNeurons; i++) {
            for (int j = 0; j < numberOfPrevNeurons; j++) {
                weights[i][j] = random.nextGaussian() % 1;
                //weights[i][j] = 0;
            }
            bias[i] = random.nextGaussian() % 1;
            //bias[i] = 0.33;
        }
    }

    ///// FORWARDPROPGATION ////////////////////////////////////////////
    public void calculateLayer() {
        if(previousLayer == null) {
            throw new IllegalArgumentException("calculateLayer. This is input layer");
        }

        for (int i = 0; i < numberOfNeurons; i++) {
            neuronsValue[i] = calculateNeuronValue(calculateNeuronActivation(i));
        }
    }

    private double calculateNeuronActivation(int neuronNumber) {
        double[] prevNeurons = previousLayer.getNeuronsValue();
        double val = 0;
        double[] w = weights[neuronNumber];
        for (int j = 0; j < numberOfPrevNeurons; j++) {
            val += w[j] * prevNeurons[j];
        }
        val +=  bias[neuronNumber];
        return val;
    }

    private double calculateNeuronValue(double activation) {
        return sigmoid(activation);
    }

    //// FUNCTOINS ////////////////////////////////////////
    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.pow(Math.E, -z));
    }

    private double sigmoidDrv(double z) {
        return z * (1 - z);
    }

    private double error(double out, double target) {
        return Math.pow(out - target, 2) / 2.0;
    }
    private double errorDrv(double out, double target) {
        return out - target;
    }

    //////// BACKPROPOGATION /////////////////////////////////////////
    public void calcDelta(int sampleNumber) {
        if (previousLayer == null)
            return;
        else if (nextLayer == null)
            calcFinalDelta(sampleNumber);
        else
            calcInnerDelta();

        updateAggregates();
    }

    private void calcFinalDelta(int sampleNumber) {
        for (int i = 0; i < numberOfNeurons; i++) {
            double out = neuronsValue[i];
            double ideal = i == sampleNumber ? 1d : 0d;
            delta[i] = errorDrv(out, ideal) * sigmoidDrv(out);
        }
    }

    private void calcInnerDelta() {
        double[][] nextWeights = nextLayer.getWeights();
        double[] nextDelta = nextLayer.getDelta();
        for (int i = 0; i < numberOfNeurons; i++) {
            double val = 0d;
            for (int j = 0; j < nextLayer.getNumberOfNeurons(); j++) {
                val += nextWeights[j][i] * nextDelta[j] ;
            }
            val *= sigmoidDrv(neuronsValue[i]);
            delta[i] = val;
        }
    }

    private void updateAggregates() {
        for (int i = 0; i < numberOfNeurons; i++) {
            aggregatedDelta[i] += delta[i];

            for (int j = 0; j < previousLayer.getNumberOfNeurons(); j++) {
                aggregatedError[i][j] += delta[i] * previousLayer.getNeuronsValue()[j];
            }
        }
    }

    ////////// UPDATE WEIGHTS ///////////////////////////////
    public void updateWeights(int sizeOfBatch) {
        double ETA = 0.5;
        for (int i = 0; i < numberOfNeurons; i++) {
            for (int j = 0; j < previousLayer.getNumberOfNeurons(); j++) {
                weights[i][j] -= ETA * (aggregatedError[i][j] / sizeOfBatch);
            }
            bias[i] -= ETA * (aggregatedDelta[i] / sizeOfBatch);
        }
    }

    public void clearDelta() {
        Arrays.fill(delta, 0.0);
        Arrays.fill(aggregatedDelta, 0.0);

        for (double[] val : aggregatedError) Arrays.fill(val, 0.0);
    }

    /// MSE ///////////////////////////////////////////
    public double calcError(int sampleNumber) {
        double val = 0d;
        if (nextLayer != null) return val;

        for (int i = 0; i < numberOfNeurons; i++) {
            double out = neuronsValue[i];
            double idealVal = i == sampleNumber ? 1d : 0d;
            val += error(out, idealVal);
        }
        return val;
    }

    ///// RECOGNITION ////////////////////////////
    public int result() {
        int res = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < numberOfNeurons; i++) {
            if(neuronsValue[i] > max) {
                max = neuronsValue[i];
                res = i;
            }
        }
        return res;
    }
}
