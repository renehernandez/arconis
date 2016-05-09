package arconis;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import arconis.discovery.*;
import arconis.generators.*;
import arconis.tests.*;
import arconis.utils.*;

public class Main {

    public static void main(String[] args) {
//        double[][] positions = new double[][] {
//                {1, 2},
//                {1, 1},
//                {2, 2},
//                {3, 2},
//                {0, 0}
//        };

        ArrayList<TestData> data = GenerateTest(10, 0, 0, 1.5);

        //DiscoTestCase test = new DiscoTestCase(1, "tests_disco.txt", data);
        //test.run();

        AccTestCase test1 = new AccTestCase(1, "tests_acc.txt", data);

        test1.run();

    }

    public static ArrayList<TestData> GenerateTest(int pointsNumber, double xCenter, double yCenter, double radius){
        /*double[][] positions = new double[pointsNumber][2];
        Random r = new Random();

        int xSign, ySign;

        for(int i = 0; i < pointsNumber; i++){
            xSign = r.nextDouble() > 0.5 ? -1 : 1;
            ySign = r.nextDouble() > 0.5 ? -1 : 1;
            positions[i][0] = xSign * Math.abs(radius - xCenter) * r.nextDouble();
            positions[i][1] = ySign * Math.abs(radius - yCenter) * r.nextDouble();
            System.out.print("{"  + positions[i][0] + "," +  positions[i][1] + "}" + ",");
        }*/

        /*double[][] positions = new double[][] {
                {1, 2},
                {1, 1},
                {2, 2},
                {3, 2},
                {0, 0}
        };*/

        double[][] positions = new double[][] {
                {0.9933686635820593,0.8009874509068697},
                {-0.4301983759371652,-1.0888070212504761},
                {1.2956428150199002,0.5329413696549619},
                {-1.4628083826133642,-1.3074985483768244},
                {0.37225133082448875,-0.6735402141480518},
                {1.25753486536426,1.4949338889379389},
                {0.4244929130673936,1.382416401477416},
                {-1.4718804711462359,-0.6332121618608056},
                {1.3757587675796628,0.023685869525894743},
                {-1.1290061487934535,-0.512021259717496}
        };

        ArrayList<TestData> list = new ArrayList<>();
        list.add(new TestData(positions, radius));
        return list;
    }

}