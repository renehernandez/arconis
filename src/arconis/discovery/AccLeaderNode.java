package arconis.discovery;

import arconis.Address;
import arconis.MessageData;
import arconis.tests.TestData;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class AccLeaderNode<TMsg extends AccMessage> extends AccNode<TMsg> {

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
    private int extraWakeupTimeslot=-1;
    private int currentDutyCycle;


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

    //Set<Integer> knownNeighbors;


    public AccLeaderNode(int objectID, MessageData<TMsg> msgData, PositionData posData) throws Exception {
        super(objectID, msgData, posData);

        this.knownNeighbors = Collections.synchronizedList(new ArrayList<>());
        //this.eratosthenesSieve();
        this.initialTime = System.currentTimeMillis();

        this.selectPrimes();
        //this.knownNeighbors.add(new NeighborItem(objectID, 0, 0, "37:43"));
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

    public static int getIntervalLength() {
        return intervalLength;
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
//                        sleep(System.currentTimeMillis() - msg1.getReceivedTime());
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
                if (!this.isAwakenTimeAtExtraPrime()){
                    setExtraPrime(System.currentTimeMillis());
                } //fixme
				for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    writeToSocket(entry, outputMsg);
                }
				
            }
        }
    }
	
    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
//            System.out.println("ID: " + this.getObjectID() + ", inputMsg: " + msg);
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

        int hopsdOfSendingNode = 0;
        hopsdOfSendingNode =  getHopsOfNode(idOfSendingNode);

        switch (hopsdOfSendingNode) {
             case 2:  numberOfNewNeighborFrom2HopsNeighbors++;
                  break;
             case 3:  numberOfNewNeighborFrom3HopsNeighbors++;
                  break;
             default: break;
        }
        Optional<NeighborItem> find = knownNeighbors.stream().filter(x -> x.getId() == idOfSendingNode).
                                    findFirst();
		if (!find.isPresent()){
			 knownNeighbors.add(new NeighborItem(idOfSendingNode, 1, initialtimeOfSendingNode, firstPrimeOfSendingNode+","+secondPrimeOfSendingNode));
            this.lastReceivedTime = msg.getReceivedTime();
            realNeighbors.add(idOfSendingNode);
		} else if(find.get().getHops() != 1) {
            find.get().setHops(1);
            realNeighbors.add(idOfSendingNode);
            this.lastReceivedTime = msg.getReceivedTime();
        }
			
        for (NeighborItem neighborEntry : NeighborsOfSendingNode){
            if(neighborEntry.getId() == this.getObjectID())
                continue;

            if (!knownNeighbors.stream().anyMatch(x -> x.getId() == neighborEntry.getId())) {  //neighbor NOT in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                if (hopsdOfCurrentNeighborEntry <= 3) { // only keep neighbors with max 3 hops
                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                    initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
                    knownNeighbors.add(new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
                    switch (hopsdOfSendingNode) {
                        case 1:
                            numberOfNewInformationFrom1HopsNeighbors++;
                            break;
                        case 2:
                            numberOfNewInformationFrom2HopsNeighbors++;
                            break;
                        case 3:
                            numberOfNewInformationFrom3HopsNeighbors++;
                            break;

                        default:
                            break;
                    }
                }
            } else {  //neighbor in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                NeighborItem current = knownNeighbors.stream().filter(x -> x.getId() == neighborEntry.getId()).findFirst().get();

                if (hopsdOfCurrentNeighborEntry < current.getHops() && hopsdOfCurrentNeighborEntry <= 3) { // only keep one entry for each node with minimum hops to current node and only}
//                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    current.setDutycycle(neighborEntry.getDutycycle());
                    current.setInitialtime(neighborEntry.getInitialtime());

//                    knownNeighbors.set(knownNeighbors.indexOf(neighborEntry), new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));

                    switch (hopsdOfSendingNode) {
                        case 1:
                            numberOfNewInformationFrom1HopsNeighbors++;
                            break;
                        case 2:
                            numberOfNewInformationFrom2HopsNeighbors++;
                            break;
                        case 3:
                            numberOfNewInformationFrom3HopsNeighbors++;
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }


    private int getHopsOfNode(int id) {
        int hops=0;

        for (NeighborItem neighborEntry : knownNeighbors)
            if (neighborEntry.getId() == id) {
                hops = neighborEntry.getHops();
            }

        return hops;
    }

    public void setExtraPrime(long startTime) {
        List<Long> timeslots = new ArrayList<Long>();
		long timeslot = startTime + 5;
		double gainOfCurrentTimeSlot=0;
		double gainOfPreviousTimeSlot=0;

		while(!isAwakenTime(timeslot, this.initialTime, this.firstPrime, this.secondPrime)){
            timeslots.add(timeslot);
			timeslot = timeslot + 5;
		}

        if (timeslots.size() > 20){

            for (int i=0; i<timeslots.size();i++){
                gainOfCurrentTimeSlot = getPointOfTimeslot(timeslots.get(i));
                if (gainOfCurrentTimeSlot  > 0 && gainOfCurrentTimeSlot > gainOfPreviousTimeSlot){
                    extraPrime = (int)((timeslots.get(i) - this.initialTime) / this.intervalLength);
                }
                gainOfPreviousTimeSlot = gainOfCurrentTimeSlot;
            }
        }

    }

    private double getPointOfTimeslot(long timeslot) {
        int hopsdOfCurrentNeighborEntry;
        long initialtimeOfCurrentNeighborEntry;
        int dutycyclefCurrentNeighborEntry1;
        int dutycyclefCurrentNeighborEntry2;
        int wakeupCountOf1HopNeighbors=0;
        int wakeupCountOf2HopNeighbors=0;
        int wakeupCountOf3HopNeighbors=0;
        double point=0;

        for (NeighborItem neighborEntry : knownNeighbors) {
            dutycyclefCurrentNeighborEntry1 = neighborEntry.getDutycycles()[0];
            dutycyclefCurrentNeighborEntry2 = neighborEntry.getDutycycles()[1];
            initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
            hopsdOfCurrentNeighborEntry = neighborEntry.getHops();
            boolean wakeupCondition = false;
            if (isAwakenTime(timeslot, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry1, dutycyclefCurrentNeighborEntry2)){//if the node of current neighbor entry will awake at this timeslot
                    wakeupCondition=true;
                 }
 
                if (wakeupCondition){//if the node of current neighbor entry will awake at this timeslot
                    switch (hopsdOfCurrentNeighborEntry) {
                        case 1: wakeupCountOf1HopNeighbors ++ ;
                            break;
                        case 2: wakeupCountOf2HopNeighbors ++ ;
                            break;
                        case 3: wakeupCountOf3HopNeighbors ++ ;
                            break;
                        default: break ;
                    }
            }
        }

        point = 0.6 * (weightOfDirectGainFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfDirectGainFrom3HopsNeighbors * wakeupCountOf3HopNeighbors ) +
                0.4 * (weightOfIndirectGainFromFrom1HopsNeighbors * wakeupCountOf1HopNeighbors + weightOfIndirectGainFromFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfIndirectGainFromFrom3HopsNeighbors * wakeupCountOf3HopNeighbors );

        return point;
    }


    public void adjustWeight() {
        if ((numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) > 0){
            weightOfDirectGainFrom2HopsNeighbors = (weightOfDirectGainFrom2HopsNeighbors + numberOfNewNeighborFrom2HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
            weightOfDirectGainFrom3HopsNeighbors = (weightOfDirectGainFrom3HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
        }

        if ((numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) > 0){
            weightOfIndirectGainFromFrom1HopsNeighbors = (weightOfIndirectGainFromFrom1HopsNeighbors + numberOfNewInformationFrom1HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
            weightOfIndirectGainFromFrom2HopsNeighbors = (weightOfIndirectGainFromFrom2HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
            weightOfIndirectGainFromFrom3HopsNeighbors = (weightOfIndirectGainFromFrom3HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
        }

    }

}
