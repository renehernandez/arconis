package arconis;

public class Address {

    // Private Fields

    String host;
    int port;

    // Getters & Setters

    public String getHost(){
        return this.host;
    }

    public int getPort(){
        return this.port;
    }

    // Constructors

    public Address(String host, int port){
        this.host = host;
        this.port = port;
    }

    // Public Methods

    @Override
    public String toString(){
        return "<Host:" + this.host + " | Port:" + this.port + ">";
    }
}