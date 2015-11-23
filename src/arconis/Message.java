package arconis;

public abstract class Message {

    public abstract String encode();

    public abstract String getContent();

    public abstract int getObjectID();

}