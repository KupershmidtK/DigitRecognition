package recognition;
import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // write your code here
        Program p = new Program();
        p.run();
    }
}

class Program {
    NeuronNet net;
    String weightsFileName = "./NetWeights.bin";

    public Program() {
        net = new NeuronNet();
        net.learningNetwork();
        /*
        if (!checkFile()) {
            net = new NeuronNet();
            net.learningNetwork();
            writeNetwork();

        } else {
            readNetwork();
        }

         */
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        prompt();
        int rc = scanner.nextInt();
        while (rc == 1) {
            learnNetwork();
            prompt();
            rc = scanner.nextInt();
        }
        net.readInput();
        System.out.println("This number is " + net.recognize());
        //System.out.println(Arrays.toString(net.getOutputLayer()));
    }

    private void prompt() {
        System.out.println("1. Learn the network");
        System.out.println("2. Guess a number");
        System.out.print("Your choice: ");
    }

    private void writeNetwork() {
        try {
            SerializationUtils.serialize(net, weightsFileName);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void readNetwork() {
        System.out.println("Loading weights from file");
        try {
            net = (NeuronNet) SerializationUtils.deserialize(weightsFileName);
        } catch (IOException | ClassNotFoundException  e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void learnNetwork() {
        net.learningNetwork();
        writeNetwork();
        System.out.println("Learning...");
        System.out.println("Done! Saved to the file.");
    }

    private boolean checkFile() {
        return new File(weightsFileName).exists();
    }
}

class SerializationUtils {
    /**
     * Serialize the given object to the file
     */
    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}