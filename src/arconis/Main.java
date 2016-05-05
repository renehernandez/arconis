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

        ArrayList<TestData> data = GenerateTest(5, 0, 0, 1.5);

        DiscoTestCase test = new DiscoTestCase(1, "tests_disco.txt", data);

        test.run();

//        AccTestCase test1 = new AccTestCase(1, "tests_acc.txt", data);
//
//        test1.run();

    }

    public static ArrayList<TestData> GenerateTest(int pointsNumber, double xCenter, double yCenter, double radius){
//        double[][] positions = new double[pointsNumber][2];
//        Random r = new Random();
//
//        int xSign, ySign;
//
//        for(int i = 0; i < pointsNumber; i++){
//            xSign = r.nextDouble() > 0.5 ? -1 : 1;
//            ySign = r.nextDouble() > 0.5 ? -1 : 1;
//            positions[i][0] = xSign * Math.abs(radius - xCenter) * r.nextDouble();
//            positions[i][1] = ySign * Math.abs(radius - yCenter) * r.nextDouble();
//        }

        double[][] positions = new double[][] {
                {1, 2},
                {1, 1},
                {2, 2}
//                {3, 2},
//                {0, 0}
        };

        ArrayList<TestData> list = new ArrayList<>();
        list.add(new TestData(positions, radius));
        return list;
    }

}