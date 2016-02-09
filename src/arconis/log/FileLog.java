package arconis.log;

/**
 * Created by aegis on 30/11/15.
 */
public class FileLog extends Log {

    // Private Fields

    String path;

    // Constructors

    public FileLog(String path){
        this.path = path;
    }

    // Public Methods

    @Override
    public void print(String path){

    }

}
