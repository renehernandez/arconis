package arconis.discovery;

import arconis.*;
import arconis.tests.TestData;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class AccNode<TMsg extends AccMessage> extends PositionNode<TMsg> {

//    public enum Status {
//        SLEEP,
//        LISTEN,
//        TRANSMITTING
//    }

    static final int intervalLength = 5;

    ArrayList<Integer> primes;
	int firstPrime=-1;
	int secondPrime=-1;
 	int extraPrime=-1;
    //double dutyCycle;
    long initialTime;
//    Status status;
    private List<NeighborItem> knownNeighbors;
    protected Set<Integer> realNeighbors;
    private int extraWakeupTimeslot=-1;
    private int currentDutyCycle;

    protected long lastReceivedTime;

    private double weightOfDirectGainFrom2HopsNeighbors=0.5;
    private double weightOfDirectGainFrom3HopsNeighbors=0.5;
    private double weightOfIndirectGainFromFrom1HopsNeighbors=0.4;
    private double weightOfIndirectGainFromFrom2HopsNeighbors=0.3;
    private double weightOfIndirectGainFromFrom3HopsNeighbors=0.3;


    private int numberOfNewNeighborFrom2HopsNeighbors=0;
    private int numberOfNewNeighborFrom3HopsNeighbors=0;
    private int numberOfNewInformationFrom1HopsNeighbors=0;
    private int numberOfNewInformationFrom2HopsNeighbors=0;
    private int numberOfNewInformationFrom3HopsNeighbors=0;

    final Object lock = new Object();

    public AccNode(int objectID, MessageData<TMsg> msgData, PositionData posData) throws Exception {
        super(objectID, msgData, posData);

        this.knownNeighbors = Collections.synchronizedList(new ArrayList<>());
        //this.eratosthenesSieve();
        this.initialTime = System.currentTimeMillis();

        this.selectPrimes();
        realNeighbors = new HashSet<>();
    }

    public int getFirstPrime(){
        return this.firstPrime;
    }
	
    public int getSecondPrime(){
        return this.secondPrime;
    }

    public void setInitialTime() {
        this.initialTime = System.currentTimeMillis();
    }
	
	private void selectPrimes() throws Exception {
        /*double eps = 1e-2;

        for(int i = 0; i < primes.size(); i++)
            for(int j = i + 1; j < primes.size(); j++)
                if(Math.abs(1.0/primes.get(i) + 1.0/primes.get(j) - dutyCycle) <= eps){
                    firstPrime = primes.get(i);
                    secondPrime = primes.get(j);
                    System.out.println("Combination: (" + firstPrime + ", " + secondPrime + ")");
                    return;
                }
        throw new Exception("Prime combination not found");*/
        this.firstPrime = 37;
        this.secondPrime = 43;
    }
	
	    /**
     * @return the counter
     */
    /*public int getCounter() {
        return (int) (Math.floor ((System.currentTimeMillis() - this.initialTime) / intervalLength));
    }*/
	
	public long getInitialTime() {
        return this.initialTime;
    }

    public long getLastReceivedTime() {
        return this.lastReceivedTime;
    }

    public static int getIntervalLength() {
        return intervalLength;
    }

    public Set<Integer> getRealNeighbors() {
        return this.realNeighbors;
    }

    @Override
    public void sendMessage() {
        new Thread(() -> {
            while (workCondition()) {
                sendMessage(this.getGenerator().generate("HELLO", this));
                try {
                    sleep(intervalLength - 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TMsg msg1 = this.getGenerator().generate("HELLO", this);
                sendMessage(msg1);
//                if(System.currentTimeMillis() - msg1.getSendTime() < 5){
//                    try {
//                        sleep(System.currentTimeMillis() - msg1.getReceivedTime() - 10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }).start();
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {

            if(this.isAwakenTime() || this.isAwakenTimeAtExtraPrime()){
                //System.out.println("ID: " + this.getObjectID() + " awake time in sendMessage");
				for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    writeToSocket(entry, outputMsg);
                }
				
            }
        }
    }
	
    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            updateMyNeighborTable(msg);
//            System.out.println("ID: " + this.getObjectID() + ", known: " + this.knownNeighbors);

            runProcessedMessageEvent();
        }
    }

    // private methods

   /*private void eratosthenesSieve(){
        boolean[] mask = new boolean[DiscoNode.MAX];
        int sqrt = (int)(Math.sqrt(DiscoNode.MAX) + 1);

        for(int i = 2; i < sqrt; i++){
            if(!mask[i]){
                primes.add(i);
                for(int j = i * i; j < mask.length; j += i)
                    mask[j] = true;
            }
        }
        for(int i = sqrt + 1; i < mask.length; i++)
            if(!mask[i])
                primes.add(i);
    }*/

    @Override
    protected boolean canProcessMessage(TMsg msg) {
        return shouldReceiveMessage(msg) && (this.isAwakenTime(msg) || this.isAwakenTimeAtExtraPrime(msg));
    }

    private double distFrom(double x, double y){
        double xDiff = this.getXPos() - x;
        double yDiff = this.getYPos() - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    private boolean isAwakenTime(){
        return isAwakenTime(null);
    }
	
	private boolean isAwakenTimeAtExtraPrime(){
        return isAwakenTimeAtExtraPrime(null);
    }

    private boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
		return isAwakenTime(receivedTime, this.initialTime, this.firstPrime, this.secondPrime);
    }
	
	private boolean isAwakenTimeAtExtraPrime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
		return isAwakenTimeAtExtraPrime(receivedTime, this.initialTime, this.extraPrime);
    }
	
	 private boolean isAwakenTime(long receivedTime, long initialTime, int firstPrime, int secondPrime){
        long firstRem = ((receivedTime - initialTime)/ intervalLength ) % firstPrime;
        long secondRem = ((receivedTime - initialTime)/ intervalLength ) % secondPrime;

        return firstRem == 0 || secondRem == 0;
    }
	
	private boolean isAwakenTimeAtExtraPrime(long receivedTime, long initialTime, int extraPrime){
        long extraRem = (receivedTime - initialTime) / intervalLength;
        return extraRem == extraPrime;
    }

    private boolean shouldReceiveMessage(TMsg msg){
        return this.distFrom(msg.getXPos(), msg.getYPos()) <= msg.getRadius();
    }

    private void writeToSocket(Map.Entry<Integer, Address> entry, TMsg outputMsg){
        Address address = entry.getValue();
        int objectID = entry.getKey();
        try{
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(outputMsg.encode());
            output.close();
        } catch(Exception e){
            this.getLog().print("Unable to connect to node: " + objectID);
        }
    }

    /**
     * @return the Neighbors
     */
    public List<NeighborItem> getKnownNeighbors() {
        return knownNeighbors;
    }


    public int allDirectNeighborsFound() {
        int neighborsFound =0;

        for (NeighborItem neighborEntry : knownNeighbors) {
            if (neighborEntry.getHops() == 1 || neighborEntry.getHops() == 0){
                neighborsFound++;
            }
        }

        return neighborsFound;

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


        Optional<NeighborItem> find = knownNeighbors.stream().filter(x -> x.getId() == idOfSendingNode).
                findFirst();
        if (!find.isPresent()){
            knownNeighbors.add(new NeighborItem(idOfSendingNode, 1, initialtimeOfSendingNode, firstPrimeOfSendingNode+","+secondPrimeOfSendingNode));
            this.lastReceivedTime = msg.getReceivedTime();
            realNeighbors.add(idOfSendingNode);
        } else if(find.get().getHops() != 1) {
            find.get().setHops(1);
            this.lastReceivedTime = msg.getReceivedTime();
            realNeighbors.add(idOfSendingNode);
        }

        for (NeighborItem neighborEntry : NeighborsOfSendingNode) {
            if (neighborEntry.getId() == this.getObjectID())
                continue;

            if (!knownNeighbors.stream().anyMatch(x -> x.getId() == neighborEntry.getId())) {  //neighbor NOT in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                if (hopsdOfCurrentNeighborEntry <= 3) { // only keep neighbors with max 3 hops
                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                    initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
                    knownNeighbors.add(new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
                }
            } else {  //neighbor in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                NeighborItem current = knownNeighbors.stream().filter(x -> x.getId() == neighborEntry.getId()).findFirst().get();

                if (hopsdOfCurrentNeighborEntry < current.getHops() && hopsdOfCurrentNeighborEntry <= 3) { // only keep one entry for each node with minimum hops to current node and only}
                    current.setDutycycle(neighborEntry.getDutycycle());
                    current.setInitialtime(neighborEntry.getInitialtime());
                }
            }
        }
    }

}
