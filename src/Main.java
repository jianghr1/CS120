// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import Project2.Project2;
import physics.base.CarrierGenerator;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.text.DecimalFormat;
public class Main {
    public static int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        int result = 0;

        while (a != 0) {
            if ((a & 1) == 1) {
                result += b;
            }
            a >>= 1;
            b <<= 1;
        }
        return result;
    }
    public static float multiply(float num, int multiplier) {
        if (multiplier == 0) {
            return 0.0f;
        }

        float result = 0.0f;
        int absMultiplier = Math.abs(multiplier);

        for (int i = 0; i < absMultiplier; i++) {
            result += num;
        }

        if (multiplier < 0) {
            result = -result;
        }

        return result;
    }

    public static void main(String[] args) {
        int num = 312423543;
        int multiplier = 4;

        int result = multiply(num, multiplier);
        System.out.println(num + " * " + multiplier + " = " + result);
    }
//    public static void main(String[] args) {
//        Project2 project2 = new Project2();
//        try {
//            CarrierGenerator carrierGenerator = new CarrierGenerator(48000, 512);
//            String filePath1 = "carrier1.txt";
//            String filePath2 = "carrier2.txt";
//            String filePath3 = "preamble.txt";
//            float[] carrier1 = carrierGenerator.genCarrierArray(4000, "sin", 10000);
//            float[] carrier2 = carrierGenerator.genCarrierArray(8000, "cos", 10000);
//            float[] preamble = carrierGenerator.genPrembleArray(128);
//            FileWriter fileWriter = new FileWriter(filePath1);
//            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//            for (float value : carrier1) {
//                float t = 32767*value;
//                int temp = (int) t;
//                bufferedWriter.write(temp + " ");
//            }
//            bufferedWriter.close();
//            FileWriter fileWriter1 = new FileWriter(filePath3);
//            BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
//            for (float value : preamble) {
//                float t = 32767*value;
//                int temp = (int) t;
//                bufferedWriter1.write(temp + " ");
//            }
//            bufferedWriter1.close();
//
//
//
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
}