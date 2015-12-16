package arconis.benchmark;

/**
 * Created by aegis on 16/12/15.
 */
public class Benchmark {

    long initialTime;
    long finalTime;
    int[] messagesExecutions;
    long[] timeExecutions;
    int executions;
    final Object lock = new Object();
    int currentExecution;

    public Benchmark(int executions){
        this.messagesExecutions = new int[executions];
        this.timeExecutions = new long[executions];
        this.executions = executions;
        this.currentExecution = 0;
    }

    public int getCurrentExecution(){
        return this.currentExecution;
    }

    public Benchmark nextExecution(){
        this.currentExecution++;
        return this;
    }

    public Benchmark setTimeExecution(long time){
        synchronized (lock){
            this.timeExecutions[this.currentExecution] = time;
        }
        return this;
    }

    public Benchmark incrementMessageCount(){
        this.messagesExecutions[this.currentExecution]++;
        return this;
    }

    public int[] getMessagesExecutions(){
        return this.messagesExecutions;
    }

    public long[] getTimeExecutions(){
        return this.timeExecutions;
    }

    public long getInitialTime(){
        return this.initialTime;
    }

    public long getFinalTime(){
        return this.finalTime;
    }

    public Benchmark start(){
        this.initialTime = System.nanoTime();
        return this;
    }

    public Benchmark stop(){
        this.finalTime = System.nanoTime();
        return this;
    }
}
