package arconis.discovery;

import arconis.interfaces.*;
import arconis.*;

/**
 * Created by aegis on 07/01/16.
 */
public class DiscoveryMessage extends Message {

    String content;
    double xPos, yPos, radius;

    public DiscoveryMessage(int objectID, double xPos, double yPos, double radius, String content){
        super(objectID, System.currentTimeMillis());
        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
        this.content = content;
    }

    public static DiscoveryMessage decode(String msgEncoded){
        String[] data = msgEncoded.split(":");
        return new DiscoveryMessage(Integer.parseInt(data[0]),
                Double.parseDouble(data[1]), Double.parseDouble(data[2]),
                Double.parseDouble(data[3]), data[4]);
    }

    public static DiscoveryMessage create(String content, Node<DiscoveryMessage> node) {
        PositionNode<DiscoveryMessage> realNode = (PositionNode<DiscoveryMessage>)node;
        return new DiscoveryMessage(realNode.getObjectID(), realNode.getXPos(), realNode.getYPos(),
                realNode.getRadius(), content);
    }


    @Override
    public String encode() {
        return objectID + ":" + xPos + ":" + yPos + ":" + radius + ":" + content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    public double getXPos(){
        return this.xPos;
    }

    public double getYPos(){
        return this.yPos;
    }

    public double getRadius(){
        return this.radius;
    }

    @Override
    public int hashCode(){
        return (content + objectID + xPos + yPos + radius).hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(this == other) return true;

        if(!(other instanceof DiscoveryMessage)) return false;

        DiscoveryMessage msg = (DiscoveryMessage)other;

        return content.equals(msg.getContent()) && objectID == msg.getObjectID()
                && xPos == msg.getXPos() && yPos == msg.getYPos() && radius == msg.getRadius();
    }

    @Override
    public String toString(){
        return "<objectID:" + objectID + " | xPos:" + xPos + " | yPos:" + yPos + ">";
    }
}
