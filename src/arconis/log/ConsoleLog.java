package arconis.log;

/**
 * Created by aegis on 30/11/15.
 */
public class ConsoleLog extends Log {

    @Override
    public void print(String msg){
        System.out.println(msg);
    }

}
