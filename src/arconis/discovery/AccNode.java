package arconis.discovery;

import arconis.*;
import arconis.tests.TestData;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class AccNode<TMsg extends AccMessage> extends DiscoveryNode<TMsg> {

    class AccScheduledTask extends TimerTask {

        AccNode<TMsg> node;

        public AccScheduledTask(AccNode<TMsg> node){
            this.node = node;
        }

        @Override
        public void run(){
            if (node.workCondition()) {
                node.sendMessage(node.getGenerator().generate("HELLO", node));
            } else {
                cancel();
            }
        }

    }

    // Private Fields

 	int extraPrime=-1;
    List<NeighborItem> neighborItems;

    // Getters && Setters

    public List<NeighborItem> getNeighborItems(){
        return this.neighborItems;
    }

    // Constructors

    public AccNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws Exception {
        super(objectID, msgData, posData, dutyCycle);

        setInitialTime(System.currentTimeMillis());

        neighborItems = Collections.synchronizedList(new ArrayList<>());
    }

    // Public Methods

    @Override
    public void sendMessage() {
        Timer intervalBegin = new Timer();
        Timer intervalEnd = new Timer();

        AccScheduledTask scheduleBegin = new AccScheduledTask(this);
        AccScheduledTask scheduleEnd = new AccScheduledTask(this);

        intervalBegin.scheduleAtFixedRate(scheduleBegin, 0, intervalLength);
        intervalEnd.scheduleAtFixedRate(scheduleEnd, intervalLength - epsilon, intervalLength);
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {

            if(this.isAwakenTime()){
                //System.out.println("ID: " + this.getObjectID() + " awake time in sendMessage");
				for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    writeToSocket(entry, outputMsg);
                }
				
            }
        }
    }

    public void updateMyNeighborTable(TMsg msg) {
        int idOfSendingNode = msg.getObjectID();
        long initialtimeOfSendingNode = msg.getInitialtime();
        int firstPrimeOfSendingNode = msg.getFirstPrime();
        int secondPrimeOfSendingNode = msg.getSecondPrime();
        List<NeighborItem> NeighborsOfSendingNode = msg.getNeighborTable();

        int idOfCurrentNeighborEntry;
        int hopsdOfCurrentNeighborEntry;
        long initialtimeOfCurrentNeighborEntry;
        String dutycyclefCurrentNeighborEntry;


        Optional<NeighborItem> find = neighborItems.stream().filter(x -> x.getId() == idOfSendingNode).
                findFirst();
        if (!find.isPresent()){
            neighborItems.add(new NeighborItem(idOfSendingNode, 1, initialtimeOfSendingNode, firstPrimeOfSendingNode+","+secondPrimeOfSendingNode));
            this.lastReceivedTime = msg.getReceivedTime();
            getKnownNeighbors().add(idOfSendingNode);
            System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors() + ", period: " +
                    (lastReceivedTime - initialTime)/intervalLength);
        } else if(find.get().getHops() != 1) {
            find.get().setHops(1);
            this.lastReceivedTime = msg.getReceivedTime();
            getKnownNeighbors().add(idOfSendingNode);
            System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors() + ", period: " +
                    (lastReceivedTime - initialTime)/intervalLength);
        }

        for (NeighborItem neighborEntry : NeighborsOfSendingNode) {
            if (neighborEntry.getId() == this.getObjectID())
                continue;

            if (!neighborItems.stream().anyMatch(x -> x.getId() == neighborEntry.getId())) {  //neighbor NOT in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                if (hopsdOfCurrentNeighborEntry <= 3) { // only keep neighbors with max 3 hops
                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                    initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
                    neighborItems.add(new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
                }
            } else {  //neighbor in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                NeighborItem current = neighborItems.stream().filter(x -> x.getId() == neighborEntry.getId()).findFirst().get();

                if (hopsdOfCurrentNeighborEntry < current.getHops() && hopsdOfCurrentNeighborEntry <= 3) { // only keep one entry for each node with minimum hops to current node and only}
                    current.setDutycycle(neighborEntry.getDutycycle());
                    current.setInitialtime(neighborEntry.getInitialtime());
                }
            }
        }
    }

    // Protected Methods
	
    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            updateMyNeighborTable(msg);
//            System.out.println("ID: " + this.getObjectID() + ", known: " + this.realNeighbors);

            runProcessedMessageEvent();
        }
    }

    protected boolean isAwakenTime(){
        return isAwakenTime(null);
    }


    protected boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long diff = receivedTime - initialTime < 0 ? 0 : receivedTime - initialTime;
        long firstRem = (diff/ intervalLength ) % firstPrime;
        long secondRem = (diff/ intervalLength ) % secondPrime;

        return firstRem == 0 || secondRem == 0;
    }

    @Override
    protected boolean shouldReceiveMessage(TMsg msg){
        double xDiff = this.getXPos() - msg.getXPos();
        double yDiff = this.getYPos() - msg.getYPos();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff) <= msg.getRadius();
    }

}
