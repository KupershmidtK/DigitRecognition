package recognition;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

class NeuronNet implements Serializable {
    private int numbersOfLayers;
    private NetworkLayer[] layers;
    private NetworkLayer inputLayer;
    private NetworkLayer outputLayer;

    private static final int GENERATION = 10;

    //// CONSTRUCTOR & SERIALIZATION ////////////////////////////////////////
    public NeuronNet(int[] layersParam) {
        InitializeNetwork(layersParam);
    }

    private void InitializeNetwork(int[] layersParam) {
        numbersOfLayers = layersParam.length;
        this.layers = new NetworkLayer[numbersOfLayers];
        for (int i = 0; i < numbersOfLayers; i++) {
            layers[i] = new NetworkLayer(layersParam[i]);
            if (i > 0) {
                layers[i].setPreviousLayer(layers[i - 1]);
            }
        }

        inputLayer = layers[0];
        outputLayer = layers[numbersOfLayers - 1];
    }

    private void writeObject(ObjectOutputStream oos) throws Exception {
        ArrayList<NetworkLayerParams> params = new ArrayList<>(numbersOfLayers);
        for(int i = 0; i < numbersOfLayers; i++) {
            params.add(new NetworkLayerParams(layers[i].getNumberOfNeurons(), layers[i].getWeights(), layers[i].getBias()));
        }
        oos.writeObject(params);
    }

    private void readObject(ObjectInputStream ois) throws Exception {
        ArrayList<NetworkLayerParams> params = (ArrayList<NetworkLayerParams>) ois.readObject();
        numbersOfLayers = params.size();
        int[] layersParam = new int[numbersOfLayers];
        for (int i = 0; i < numbersOfLayers; i++) {
            layersParam[i] = params.get(i).getSize();
        }

        InitializeNetwork(layersParam);
        for (int i = 0; i < numbersOfLayers; i++) {
            layers[i].setWeights(params.get(i).getWeights());
            layers[i].setBias(params.get(i).getBias());
        }
    }

    //// LEARNING /////////////////////////////////////////////////////////////////
    public void learning(File[] files) {
        int sizeOfBatch = 100;
        for (int i = 0; i < GENERATION; i++) {
            int size = files.length;
            int startOfRange = 0;
            int endOfRange = sizeOfBatch;
            learnWithSamplesBatch(files, startOfRange, endOfRange);

            while (endOfRange < size) {
                startOfRange = endOfRange;
                endOfRange += sizeOfBatch;
                endOfRange = endOfRange < size ? endOfRange : size;
                learnWithSamplesBatch(files, startOfRange, endOfRange);
            }
        }
    }

    private double learnWithSamplesBatch(File[] files, int start, int end) {
        int sizeOfBatch = end - start;
        double err = 0;
        LearningData ld = new LearningData();

        for (int j = start; j < end; j++) {
            ld.readFile(files[j].getPath());
            err += forwardPropagation(ld.data, ld.number);
            backPropagation(ld.number);
        }
        updateWeights(sizeOfBatch);
        clearDelta();
        return err / sizeOfBatch;
    }

    private double forwardPropagation(double[] data, int sampleNumber) {
        inputLayer.setNeuronsValue(data);
        for(int i = 1; i < numbersOfLayers; i++) {
            layers[i].calculateLayer();
        }
        return outputLayer.calcError(sampleNumber);
    }

    private void backPropagation(int sampleNumber) {
        for(int i = numbersOfLayers - 1; i > 0; i--) {
            layers[i].calcDelta(sampleNumber);
        }
    }

    private void updateWeights(int sizeOfBatch) {
        for(int i = numbersOfLayers - 1; i > 0; i--) {
            layers[i].updateWeights(sizeOfBatch);
        }
    }

    private void clearDelta() {
        for(int i = numbersOfLayers - 1; i > 0; i--) {
            layers[i].clearDelta();
        }
    }

    ///// RECOGNITON //////////////////////////////
    public int recognize(double[] sample) {
        inputLayer.setNeuronsValue(sample);
        for (int i = 1; i < numbersOfLayers; i++) {
            layers[i].calculateLayer();
        }
        //System.out.println(Arrays.toString(outputLayer.getNeuronsValue()));
        return outputLayer.result();
    }
}