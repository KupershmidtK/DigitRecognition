package recognition;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

class LearningData {
    public double[] data;
    public int number;

    public void readFile(String fileName) {
        double[] res;
        try {
            String arr = readFileAsString(fileName);
            res = Arrays.stream(arr.substring(0, arr.length()).split("\\s+")).mapToDouble(Double::parseDouble).toArray();

            if (data == null) { data = new double[res.length - 1]; }
            for (int i = 0; i < res.length - 1; i++) {
                data[i] = res[i] / 255.0;
            }
            number = (int) res[res.length - 1];
        } catch (IOException e) {
            System.out.println("File " + fileName + " not found.");
        }
    }

    private String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}