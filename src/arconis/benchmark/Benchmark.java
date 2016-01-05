package arconis.benchmark;

/**
 * Created by aegis on 16/12/15.
 */
public class Benchmark {

    static long longestTime;
    static int totalNumberOfMessages;
    static final Object globalLock = new Object();

    long initialTime;
    long finalTime;
    int numberOfMessages;
    final Object lock = new Object();

    public Benchmark(){
        this.numberOfMessages = 0;
    }

    public Benchmark incrementMessageCount(){
        synchronized (lock) {
            this.numberOfMessages++;
        }
        return this;
    }

    public int getNumberOfMessages(){
        return this.numberOfMessages;
    }

    public long getTimeElapsed(){
        return this.finalTime - this.initialTime;
    }

    public long getInitialTime(){
        return this.initialTime;
    }

    public long getFinalTime(){
        return this.finalTime;
    }

    public Benchmark start(){
        synchronized (lock) {
            this.initialTime = System.nanoTime();
        }
        return this;
    }

    public Benchmark increaseTotalNumberOfMessages(int messages){
        synchronized (globalLock) {
            totalNumberOfMessages += messages;
        }
        return this;
    }

    public static int getTotalNumberOfMessages(){
        return totalNumberOfMessages;
    }

    public Benchmark updateLongestTime(long elapsedTime){
        synchronized (globalLock) {
            if(longestTime < elapsedTime){
                longestTime = elapsedTime;
            }
        }
        return this;
    }

    public static long getLongestTime(){
        return longestTime;
    }

    public static void resetMeasures(){
        longestTime = 0;
        totalNumberOfMessages = 0;
    }

    public Benchmark stop(){
        synchronized (lock) {
            this.finalTime = System.nanoTime();
        }
        return this;
    }
}
