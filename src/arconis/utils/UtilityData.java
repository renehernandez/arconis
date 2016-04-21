package arconis.utils;

import arconis.benchmark.Benchmark;
import arconis.log.ConsoleLog;
import arconis.log.Log;

/**
 * Created by aegis on 09/02/16.
 */
public class UtilityData {

    // Private Fields

    Log log;
    Benchmark benchmark;

    // Getters & Setters

    public Log getLog(){
        return this.log;
    }

    public Benchmark getBenchmark(){
        return this.benchmark;
    }

    // Constructors

    public UtilityData(Log log, Benchmark benchmark){
        this.log = log;
        this.benchmark = benchmark;
    }

    // Public Methods

    public static UtilityData DefaultUtility(){
        return new UtilityData(new ConsoleLog(), new Benchmark());
    }

}
