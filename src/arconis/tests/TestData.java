package arconis.tests;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by aegis on 10/04/16.
 */
public class TestData {

    // Private Fields

    private double[][] positions;
    private double radius;
    private int numberOfNodes;
    private HashMap<Integer, ArrayList<Integer>> realNeighborsIndices;

    // Constructors

    public TestData(double[][] positions, double radius){
        this.positions = positions;
        this.radius = radius;
        this.realNeighborsIndices = new HashMap<>();
        this.numberOfNodes = positions.length;

        findRealNeighborsIndices();
    }

    // Properties

    public double[][] getPositions(){
        return this.positions;
    }

    public double getRadius(){
        return this.radius;
    }

    public int getNumberOfNodes() {
        return this.numberOfNodes;
    }

    public HashMap<Integer, ArrayList<Integer>> getRealNeighborsIndices(){
        return this.realNeighborsIndices;
    }

    // Private Methods

    private void findRealNeighborsIndices(){
        for(int i = 0; i < positions.length; i++)
            realNeighborsIndices.put(i, new ArrayList<>());

        for (int i = 0; i < positions.length; i++){
            for (int j = 0; j < positions.length; j++) {
                if (i == j) continue;

                if (pointDistance(i, j) <= this.radius) {
                    this.realNeighborsIndices.get(i).add(j);
                }
            }
        }
    }

    private double pointDistance(int first, int sec){
        double xDiff = positions[first][0] - positions[sec][0];
        double yDiff = positions[first][1] - positions[sec][1];
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }
}
