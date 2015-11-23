package arconis;

public class Address {

    String host;
    int port;

    public Address(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String getHost(){
        return this.host;
    }

    public int getPort(){
        return this.port;
    }
}