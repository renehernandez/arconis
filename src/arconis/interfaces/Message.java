package arconis.interfaces;

public abstract class Message {

    protected int objectID;
    long sendTime, receivedTime;

    protected Message(int objectID, long sendTime){
        this.objectID = objectID;
        this.sendTime = sendTime;
    }

    public int getObjectID(){
        return this.objectID;
    }

    public long getSendTime(){
        return this.sendTime;
    }

    public long getReceivedTime(){
        return this.receivedTime;
    }

    public void setReceivedTime(long receivedTime){
        this.receivedTime = receivedTime;
    }

    public abstract String encode();

    public abstract String getContent();

}