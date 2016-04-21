package arconis.tests;

/**
 * Created by aegis on 10/04/16.
 */
public abstract class TestCase {

    // Private Fields

    int numberOfTimes;
    String outputFileName;

    protected TestCase(int numberOfTimes, String outputFileName){
        this.numberOfTimes = numberOfTimes;
        this.outputFileName = outputFileName;
    }

    public int getNumberOfTimes(){
        return this.numberOfTimes;
    }

    public String getOutputFileName(){
        return this.outputFileName;
    }

    public abstract void run();

}
