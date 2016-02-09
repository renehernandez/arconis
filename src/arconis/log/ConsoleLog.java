package arconis.log;

/**
 * Created by aegis on 30/11/15.
 */
public class ConsoleLog extends Log {

    // Public Methods

    @Override
    public void print(String msg){
        long threadID = Thread.currentThread().getId();
        System.out.println("Thread Id:" + threadID + ", Logged Message:\n" + msg);
    }

}
