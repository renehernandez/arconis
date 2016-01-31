package arconis.discovery;

import arconis.Node;
import arconis.interfaces.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aegis on 07/01/16.
 */
public class AccMessage extends Message {

    double xPos, yPos, radius;
    List<NeighborItem> items;

    public AccMessage(int objectID, double xPos, double yPos, double radius, List<NeighborItem> items){
        super(objectID, System.currentTimeMillis());
        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
        this.items = items;
    }

    public static AccMessage decode(String msgEncoded){
        String[] data = msgEncoded.split(":");
        ArrayList<NeighborItem> items = new ArrayList<>();

        for(int i = 4; i <= data.length - 4; i+= 4)
        {
            items.add(new NeighborItem(
                    Integer.parseInt(data[i]),
                    Integer.parseInt(data[i + 1]),
                    Integer.parseInt(data[i + 2]),
                    data[i + 3]
                    ));
        }

        return new AccMessage(Integer.parseInt(data[0]),
                Double.parseDouble(data[1]), Double.parseDouble(data[2]),
                Double.parseDouble(data[3]),items);
    }

    public static AccMessage create(String content, Node<AccMessage> node) {
        AccNode<AccMessage> realNode = (AccNode<AccMessage>)node;
        return new AccMessage(realNode.getObjectID(), realNode.getXPos(), realNode.getYPos(),
                realNode.getRadius(), realNode.getKnownNeighbors());
    }


    @Override
    public String encode() {
        String content = "";

        for(int i = 0; i < items.size() - 1; i++){
            content += items.get(i).toString() + ":";
        }
        content += items.get(items.size() - 1).toString();

        return objectID + ":" + xPos + ":" + yPos + ":" + radius + ":" + content;
    }

//    @Override
//    public String getContent() {
//        return this.content;
//    }

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

        if(!(other instanceof AccMessage)) return false;

        AccMessage msg = (AccMessage)other;

        return content.equals(msg.getContent()) && objectID == msg.getObjectID()
                && xPos == msg.getXPos() && yPos == msg.getYPos() && radius == msg.getRadius();
    }

    @Override
    public String toString(){
        return "<objectID:" + objectID + " | xPos:" + xPos + " | yPos:" + yPos + ">";
    }
}
