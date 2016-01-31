package arconis.discovery;

import arconis.*;
import arconis.delegates.*;
import arconis.interfaces.*;

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
    final static int MAX = 100000;
    int firstPrime;
    int secondPrime;
    //double dutyCycle;
    long initialTime;
//    Status status;

    private int localCounter = -1;
    private int startTimeSlot;
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

    final String protocol="DISCO";

    final Object lock = new Object();

    //Set<Integer> knownNeighbors;


    public AccNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder, double xPos, double yPos, double radius) throws Exception {
        super(objectID, generator, decoder, xPos, yPos, radius);

        //this.knownNeighbors = Collections.synchronizedSet(new HashSet<>());
        this.knownNeighbors = Collections.synchronizedList(new ArrayList<>());
        this.primes = new ArrayList<>();
        //this.eratosthenesSieve();

        //this.dutyCycle = dutyCycle;
//        this.status = Status.SLEEP;

        this.initialTime = System.currentTimeMillis();

        this.selectPrimes();
        this.knownNeighbors.add(new NeighborItem(objectID, 0, 0, "37:43"));
    }

    /*public double getDutyCycle(){
        return dutyCycle;
    }*/
    public ArrayList<Integer> getPrimes(){
        return primes;
    }

    @Override
    public void sendMessage() {
        new Thread(() -> {
            while (true) {
                TMsg msg = this.getGenerator().generate("HELLO", this);
                sendMessage(msg);
                try {
                    sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TMsg msg1 = this.getGenerator().generate("HELLO", this);
                sendMessage(msg1);
                if(System.currentTimeMillis() - msg.getSendTime() < 5){
                    try {
                        sleep(System.currentTimeMillis() - msg.getReceivedTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {
            this.setIsBusy(true);

            if(this.isAwakenTime()){
//                System.out.println("ID: " + this.getObjectID() + " awake time in sendMessage");
                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
//                    if(knownNeighbors.contains(entry.getKey()))
//                        continue;

                    writeToSocket(entry, outputMsg);
                }
            }
            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            this.setIsBusy(true);
            if (this.isAwakenTime(msg)) {
//                System.out.println("ID: " + this.getObjectID() + ", inputMsg: " + msg);
                if (this.shouldReceiveMessage(msg) && !this.knownNeighbors.contains(msg.getObjectID())) {
                    this.knownNeighbors.add(msg.getObjectID());
                    System.out.println("ID: " + this.getObjectID() + ", known: " + this.knownNeighbors);
                }
            }
            this.setIsBusy(false);
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
        firstPrime = 37;
        secondPrime = 43;

    }

    private double distFrom(double x, double y){
        double xDiff = this.getXPos() - x;
        double yDiff = this.getYPos() - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    private boolean isAwakenTime(){
        return isAwakenTime(null);
    }

    private boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long firstRem = (receivedTime - this.initialTime) % this.firstPrime;
        long secondRem = (receivedTime - this.initialTime) % this.secondPrime;

        return firstRem <= intervalLength || secondRem <= intervalLength;
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
     * @return the counter
     */
    public int getCounter() {
        return (int) (Math.floor ((System.currentTimeMillis() - this.initialTime) / intervalLength));
    }
    /**
     * @param counter the counter to set
     */
    /*public void setCounter(int counter) {
        this.localCounter = counter;
    }*/

    /**
     * @return the startTimeSlot
     */
   /* public int getstartTimeSlot() {
        return startTimeSlot;
    }*/
    /**
     * @param startTimeSlot the startTimeSlot to set
     */
    /*public void setstartTimeSlot(int startTimeSlot) {
        this.startTimeSlot = startTimeSlot;
    }*/

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

    public void initializeLocalCounter() {
        localCounter = 0;

    }

    public void increaseLocalCounterByOne() {
        if (localCounter>-1){
            localCounter++;
        }
    }


    public void updateMyNeighborTable(Node otherWakeupNode) {
        String idOfCurrentNeighborEntry;
        int hopsdOfCurrentNeighborEntry;
        int offsetdOfCurrentNeighborEntry;
        String dutycyclefCurrentNeighborEntry;

        int hopsdOfotherWakeupNode = 0;

        ArrayList<NeighborItem> NeighborsOfOtherWakeupNode = otherWakeupNode.getNeighbors();
        if (localId == "SOURCENODE"){
            hopsdOfotherWakeupNode =  getHopsOfNode(otherWakeupNode.getId());

            switch (hopsdOfotherWakeupNode) {
                case 2:  numberOfNewNeighborFrom2HopsNeighbors++;
                    break;
                case 3:  numberOfNewNeighborFrom3HopsNeighbors++;
                    break;
                default: break;
            }
        }

        for (NeighborItem neighborEntry : NeighborsOfOtherWakeupNode) {
            if (neighborEntry.getId()!= localId){ //current neighbor entry is not current node
                if (!knownNeighbors.contains(neighborEntry)){  //neighbor NOT in the localNeighborTable
                    hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                    if (hopsdOfCurrentNeighborEntry <=3){ // only keep neighbors with max 3 hops
                        idOfCurrentNeighborEntry= neighborEntry.getId();
                        dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                        if (neighborEntry.getId() == otherWakeupNode.getId()){ // current neighbor entry is the node sending this message
                            offsetdOfCurrentNeighborEntry = localCounter -  otherWakeupNode.getCounter();
                        }
                        else{// current neighbor entry is NOT the node sending this message
                            offsetdOfCurrentNeighborEntry = neighborEntry.getOffset() + (localCounter -  otherWakeupNode.getCounter());
                        }
                        knownNeighbors.add(new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry,offsetdOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
                        if (localId == "S"){
                            switch (hopsdOfotherWakeupNode) {
                                case 1:  numberOfNewInformationFrom1HopsNeighbors++;
                                    break;
                                case 2:  numberOfNewInformationFrom2HopsNeighbors++;
                                    break;
                                case 3:  numberOfNewInformationFrom3HopsNeighbors++;
                                    break;

                                default: break;
                            }
                        }
                    }
                }
                else {  //neighbor in the localNeighborTable
                    hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                    if (hopsdOfCurrentNeighborEntry < knownNeighbors.get(knownNeighbors.indexOf(neighborEntry)).getHops() && hopsdOfCurrentNeighborEntry <=3){ // only keep one entry for each node with minimum hops to current node and only}
                        idOfCurrentNeighborEntry= neighborEntry.getId();
                        dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                        if (neighborEntry.getId() == otherWakeupNode.getId()){ // current neighbor entry is the node sending this message
                            offsetdOfCurrentNeighborEntry = localCounter -  otherWakeupNode.getCounter();
                        }
                        else{// current neighbor entry is NOT the node sending this message
                            offsetdOfCurrentNeighborEntry = neighborEntry.getOffset() + (localCounter -  otherWakeupNode.getCounter());
                        }
                        knownNeighbors.set(knownNeighbors.indexOf(neighborEntry), new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry,offsetdOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
                        if (localId == "SOURCENODE"){
                            switch (hopsdOfotherWakeupNode) {
                                case 1:  numberOfNewInformationFrom1HopsNeighbors++;
                                    break;
                                case 2:  numberOfNewInformationFrom2HopsNeighbors++;
                                    break;
                                case 3:  numberOfNewInformationFrom3HopsNeighbors++;
                                    break;

                                default: break;
                            }
                        }
                    }
                }
            }
        }
    }


    private int getHopsOfNode(String id) {
        int hops=0;

        for (NeighborItem neighborEntry : knownNeighbors) {
            if (neighborEntry.getId() == id){
                hops = neighborEntry.getHops();
            }
        }

        return hops;
    }

    public void setExtraWakeupSlot(int startTimeSlot) {
        double gainOfCurrentTimeSlot = 0;
        double gainOfPreviousTimeSlot =0;

        int endTimeSlot =0;

        switch (protocol) {
            case "DISCO":
                if ((startTimeSlot - 1) % getDutycycle()[0] == 0  ){
                    endTimeSlot = (getDutycycle()[1] * ((startTimeSlot - 1) / getDutycycle()[0])) - 1;
                }
                else if((startTimeSlot - 1) % getDutycycle()[1] == 0){
                    endTimeSlot = (getDutycycle()[0] * ((startTimeSlot - 1) / getDutycycle()[1])) - 1;
                }
                break;
            case "UCONNECT":
                if ((startTimeSlot - 1) % getDutycycle()[0] == 0  ){
                    endTimeSlot = (getDutycycle()[1] * ((startTimeSlot - 1) / getDutycycle()[0])) - 1;
                }
                else if((startTimeSlot - 1) % getDutycycle()[1] == 0){
                    endTimeSlot = (getDutycycle()[0] * ((startTimeSlot - 1) / getDutycycle()[1])) - 1;
                }
                break;
            default: break ;
        }

        if ((endTimeSlot - startTimeSlot) > 20){

            for (int i=startTimeSlot; i<= endTimeSlot;i++){
                gainOfCurrentTimeSlot = getPointOfTimeslot(i);
                if (gainOfCurrentTimeSlot  > 0 && gainOfCurrentTimeSlot > gainOfPreviousTimeSlot){
                    extraWakeupTimeslot = i;
                }
                gainOfPreviousTimeSlot = gainOfCurrentTimeSlot;
            }
        }

    }

    private double getPointOfTimeslot(int timeSlot) {
        String idOfCurrentNeighborEntry;
        int hopsdOfCurrentNeighborEntry;
        int offsetdOfCurrentNeighborEntry;
        int dutycyclefCurrentNeighborEntry1;
        int dutycyclefCurrentNeighborEntry2;
        int wakeupCountOf1HopNeighbors=0;
        int wakeupCountOf2HopNeighbors=0;
        int wakeupCountOf3HopNeighbors=0;
        double point=0;

        for (NeighborItem neighborEntry : knownNeighbors) {
            idOfCurrentNeighborEntry= neighborEntry.getId();
            dutycyclefCurrentNeighborEntry1 = neighborEntry.getDutycycles()[0];
            dutycyclefCurrentNeighborEntry2 = neighborEntry.getDutycycles()[1];
            offsetdOfCurrentNeighborEntry = neighborEntry.getOffset();
            hopsdOfCurrentNeighborEntry = neighborEntry.getHops();
            boolean wakeupCondition = false;
            if (idOfCurrentNeighborEntry != localId){ //current neighbor entry is not current node

                switch (protocol) {
                    case "DISCO":
                        if ((timeSlot - offsetdOfCurrentNeighborEntry) % dutycyclefCurrentNeighborEntry1 == 0 || (timeSlot - offsetdOfCurrentNeighborEntry) % dutycyclefCurrentNeighborEntry2 == 0){//if the node of current neighbor entry will awake at this timeslot
                            wakeupCondition=true;
                        }
                        break;
                    case "UCONNECT":
                        if ((timeSlot - offsetdOfCurrentNeighborEntry) % dutycyclefCurrentNeighborEntry1 == 0 || (timeSlot - offsetdOfCurrentNeighborEntry) % dutycyclefCurrentNeighborEntry2 == 0){//if the node of current neighbor entry will awake at this timeslot
                            wakeupCondition=true;
                        }
                        break;
                    default: break ;
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
        }

        point = 0.6 * (weightOfDirectGainFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfDirectGainFrom3HopsNeighbors * wakeupCountOf3HopNeighbors ) +
                0.4 * (weightOfIndirectGainFromFrom1HopsNeighbors * wakeupCountOf1HopNeighbors + weightOfIndirectGainFromFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfIndirectGainFromFrom3HopsNeighbors * wakeupCountOf3HopNeighbors );

        return point;
    }
    /*public boolean wakeupAtDutycycle() {
        boolean returnValue=false;
        switch (protocol) {
            case "DISCO":
                if (localCounter % getDutycycle()[0] == 0 || localCounter % getDutycycle()[1] == 0 ){
                    returnValue=true;
                }
                break;
            case "UCONNECT":
                if (localCounter % getDutycycle()[0] == 0 || localCounter % getDutycycle()[1] == 0 ){
                    returnValue=true;
                }
                break;
            default: break ;
        }
        return returnValue;
    }

    public boolean wakeupNotAtDutycycle(int globalCounter) {
        if (extraWakeupTimeslot== globalCounter){
            return true;
        }
        else {
            return false;
        }
    }*/

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
