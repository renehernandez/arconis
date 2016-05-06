package arconis.discovery;

import arconis.Node;
import arconis.interfaces.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aegis on 07/01/16.
 */
public class AccMessage extends Message {

    // Private Fields

    String content;
    double xPos, yPos, radius;
    long initialTime;
    List<NeighborItem> neighbors;
	int firstPrime;
	int secondPrime;

    // Getters && Setters

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

    public long  getInitialtime(){
        return this.initialTime;
    }

    public int  getFirstPrime(){ return this.firstPrime; }

    public int  getSecondPrime(){
        return this.secondPrime;
    }

    public List<NeighborItem> getNeighborTable(){
        return this.neighbors;
    }

    // Constructors

    public AccMessage(int objectID, double xPos, double yPos, double radius,
                      long initialTime, int firstPrime, int secondPrime, List<NeighborItem> items,
                      String content){
        super(objectID, System.currentTimeMillis());
        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
        this.content = content;
		this.firstPrime=firstPrime;
        this.initialTime = initialTime;
		this.secondPrime=secondPrime;
        this.neighbors = items;
    }

    // Public Methods

    public static AccMessage decode(String msgEncoded){
        String[] data = msgEncoded.split(":");
        ArrayList<NeighborItem> items = new ArrayList<>();

        if(!data[7].equals("_")) {
            for (int i = 7; i < data.length - 1; i += 4) {
                items.add(new NeighborItem(
                        Integer.parseInt(data[i]),
                        Integer.parseInt(data[i + 1]),
                        Long.parseLong(data[i + 2]),
                        data[i + 3]
                ));
            }
        }

        return new AccMessage(Integer.parseInt(data[0]),Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]), Long.parseLong(data[4]),
                Integer.parseInt(data[5]),Integer.parseInt(data[6]), items, data[data.length - 1]);
    }

    public static AccMessage create(String content, Node<AccMessage> node) {
        AccNode<AccMessage> realNode = (AccNode<AccMessage>)node;
        return new AccMessage(realNode.getObjectID(), realNode.getXPos(), realNode.getYPos(),
                realNode.getRadius(),realNode.getInitialTime(), realNode.getFirstPrime(),
                realNode.getSecondPrime(), realNode.getNeighborItems(), content);
    }


    @Override
    public String encode() {
        String neighborsString = "_";

        if(neighbors.size() > 0) {
            neighborsString = "";
            for (int i = 0; i < neighbors.size() - 1; i++) {
                neighborsString += neighbors.get(i).toString() + ":";
            }
            neighborsString += neighbors.get(neighbors.size() - 1).toString();
        }

        return objectID + ":" + xPos + ":" + yPos + ":" + radius + ":" + initialTime + ":"
                + firstPrime + ":" + secondPrime + ":"+ neighborsString + ":" + content;
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
//fixme
        return content.equals(msg.getContent()) && objectID == msg.getObjectID()
                && xPos == msg.getXPos() && yPos == msg.getYPos() && radius == msg.getRadius();
    }

    @Override
    public String toString(){
        return "<Id: " + getObjectID() + ", Content: " + content + ", xPos: " + xPos + ", yPos: " + yPos + ", radius: " + radius;
    }

}
