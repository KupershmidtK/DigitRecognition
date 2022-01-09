package recognition;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Program {
    NeuronNet net;
    Scanner scanner;
    String weightsFileName = "./weights.data";
    String path = "./DigitSamples/data/";

    public Program() {
        scanner = new Scanner(System.in);

        if (!isFileExist()) {
            net = new NeuronNet(new int[]{784, 16, 16, 10});
            File dir = new File(path);
            File[] files = dir.listFiles();
            net.learning(files);
            saveParams();
        }
    }

    public void run() {
        switch (prompt()) {
            case "1":
                learning();
                break;
            case "2":
                guessAllNumbers();
                break;
            case "3":
                guessNumber();
                break;
            default:
                break;
        }
    }

    private String prompt() {
        System.out.println("1. Learn the network");
        System.out.println("2. Guess all the numbers");
        System.out.println("3. Guess number from text file");
        System.out.print("Your choice: ");
        return scanner.nextLine();
    }

    private void learning() {
        System.out.print("Enter the sizes of the layers: ");
        String[] strings = scanner.nextLine().split(" ");

        int[] netParams = Arrays.stream(strings).mapToInt(Integer::parseInt).toArray();
        net = new NeuronNet(netParams);
        System.out.println("Learning...");

        File dir = new File(path);
        File[] files = dir.listFiles();
        net.learning(files);
        saveParams();
        System.out.println("Done! Saved to the file.");
    }

    private void guessAllNumbers() {
        loadParams();
        File dir = new File(path);
        File[] files = dir.listFiles();
        LearningData ld = new LearningData();


        int count = files.length;
        int correctAnswers = 0;
        System.out.println("Guessing ...");
        for (int i = 0; i < count; i++) {
            ld.readFile(files[i].getPath());
            int rc = net.recognize(ld.data);
            if (rc == ld.number)
                correctAnswers++;
        }

        double percents = (double) correctAnswers /  count;
        System.out.printf("%d/%d, %d%%\n", correctAnswers, count, (int)(percents * 100));
    }

    private void guessNumber() {
        loadParams();
        System.out.print("Enter filename: ");
        String fileName = scanner.nextLine();
        fileName = path + fileName;

        LearningData ld = new LearningData();
        ld.readFile(fileName);
        if (ld.data != null) {
            System.out.println("This number is  " + net.recognize(ld.data));
        }
    }

    private void saveParams() {
        try {
            SerializationUtils.serialize(net, weightsFileName);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadParams() {
        try {
            net = (NeuronNet) SerializationUtils.deserialize(weightsFileName);
        } catch (IOException | ClassNotFoundException  e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isFileExist() {
        return new File(weightsFileName).exists();
    }
}