package arconis;

import arconis.interfaces.Message;

public class DefaultMessage extends Message {

    int objectID;
    String content;
    static final int size = 32;

    public DefaultMessage(String content, int objectID){
        this.content = content;
        this.objectID = objectID;
    }

    @Override
    public String getContent(){
        return this.content;
    }

    @Override
    public int getObjectID(){
        return this.objectID;
    }

    @Override
    public String encode() {
        return String.format("%0" + size + "d", this.objectID) + this.content;
    }

    public static DefaultMessage decode(String msgEncoded){
        int objectID = Integer.parseInt(msgEncoded.substring(0, 32));
        String content = msgEncoded.substring(32);
        return new DefaultMessage(content, objectID);
    }

    public static DefaultMessage create(String content, Node<DefaultMessage> node) {
        return new DefaultMessage(content, node.getObjectID());
    }

    @Override
    public int hashCode(){
        return this.content.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(this == other) return true;

        if(!(other instanceof DefaultMessage)) return false;

        DefaultMessage msg = (DefaultMessage)other;

        return this.content == msg.getContent();
    }

    @Override
    public String toString(){
        return "<objectID: " + this.objectID + ", content: " + this.content + ">";
    }

}