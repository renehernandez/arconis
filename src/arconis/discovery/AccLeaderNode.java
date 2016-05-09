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

    // Constructors

    public AccLeaderNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws Exception {
        super(objectID, msgData, posData, dutyCycle);

    }

    // Public Methods

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {

            if(this.isAwakenTime()){
                //System.out.println("ID: " + this.getObjectID() + " awake time in sendMessage");
               if (isAwakenTime(null)){
                    setExtraPrime(System.currentTimeMillis());
                } //fixme
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

        int hopsdOfSendingNode = 0;
        hopsdOfSendingNode =  getHopsOfNode(idOfSendingNode);

        switch (hopsdOfSendingNode) {
            case 2:  numberOfNewNeighborFrom2HopsNeighbors++;
                break;
            case 3:  numberOfNewNeighborFrom3HopsNeighbors++;
                break;
            default: break;
        }
        Optional<NeighborItem> find = getNeighborItems().stream().filter(x -> x.getId() == idOfSendingNode).
                findFirst();
        if (!find.isPresent()){
            getNeighborItems().add(new NeighborItem(idOfSendingNode, 1, initialtimeOfSendingNode, firstPrimeOfSendingNode+","+secondPrimeOfSendingNode));
            this.lastReceivedTime = msg.getReceivedTime();
            getKnownNeighbors().add(idOfSendingNode);
            System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors()  + ", period: " +
                    (lastReceivedTime - initialTime)/intervalLength + ", WakeUp Times: " + getWakeUpTimes());
        } else if(find.get().getHops() != 1) {
            find.get().setHops(1);
            getKnownNeighbors().add(idOfSendingNode);
            this.lastReceivedTime = msg.getReceivedTime();
            System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors()  + ", period: " +
                    (lastReceivedTime - initialTime)/intervalLength + ", WakeUp Times: " + getWakeUpTimes());
        }

        for (NeighborItem neighborEntry : NeighborsOfSendingNode){
            if(neighborEntry.getId() == this.getObjectID())
                continue;

            if (!getNeighborItems().stream().anyMatch(x -> x.getId() == neighborEntry.getId())) {  //neighbor NOT in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                if (hopsdOfCurrentNeighborEntry <= 3) { // only keep neighbors with max 3 hops
                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    dutycyclefCurrentNeighborEntry = neighborEntry.getDutycycle();
                    initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
                    getNeighborItems().add(new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));
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
                NeighborItem current = getNeighborItems().stream().filter(x -> x.getId() == neighborEntry.getId()).findFirst().get();

                if (hopsdOfCurrentNeighborEntry < current.getHops() && hopsdOfCurrentNeighborEntry <= 3) { // only keep one entry for each node with minimum hops to current node and only}
//                    idOfCurrentNeighborEntry = neighborEntry.getId();
                    current.setDutycycle(neighborEntry.getDutycycle());
                    current.setInitialtime(neighborEntry.getInitialtime());

//                    getNeighborItems().set(getNeighborItems().indexOf(neighborEntry), new NeighborItem(idOfCurrentNeighborEntry, hopsdOfCurrentNeighborEntry, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry));

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

    // Protected Methods

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
//            System.out.println("ID: " + this.getObjectID() + ", inputMsg: " + msg);
            updateMyNeighborTable(msg);
//            System.out.println("ID: " + this.getObjectID() + ", known: " + this.realNeighbors);

            runProcessedMessageEvent();
        }
    }

//    @Override
//    protected boolean canProcessMessage(TMsg msg) {
//        return shouldReceiveMessage(msg) && (this.isAwakenTime(msg) || this.isAwakenTimeAtExtraPrime(msg));
//    }

    @Override
    protected boolean isAwakenTime()
    {
        return isAwakenTime(null) || this.isAwakenTimeAtExtraPrime(null);
    }

    // Private Methods

    @Override
    protected boolean canProcessMessage(TMsg msg) {
        return shouldReceiveMessage(msg) && isAwakenTime(msg) && isAwakenTimeAtExtraPrime(msg);
    }

    protected boolean isAwakenTimeAtExtraPrime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long counter = getIntervalCounter(receivedTime);

        if (counter <= 0)
            return false;

        long extraRem = counter % extraPrime;
        return extraRem == 0 ;
    }

	 private boolean isAwakenTimeAtSlot(long receivedTime, long initialTime, int firstPrime, int secondPrime){
         long counter =  (receivedTime - initialTime) / intervalLength;
         if (counter <= 0)
             return false;

         long firstRem = counter % firstPrime;
         long secondRem = counter % secondPrime;

         return firstRem == 0 || secondRem == 0;
    }



//    public int allDirectNeighborsFound() {
//        int neighborsFound =0;
//
//        for (NeighborItem neighborEntry : getNeighborItems()) {
//            if (neighborEntry.getHops() == 1 || neighborEntry.getHops() == 0){
//                neighborsFound++;
//            }
//        }
//
//        return neighborsFound;
//
//    }


    private int getHopsOfNode(int id) {
        int hops=0;

        for (NeighborItem neighborEntry : getNeighborItems())
            if (neighborEntry.getId() == id) {
                hops = neighborEntry.getHops();
            }

        return hops;
    }

    private void setExtraPrime(long startTime) {
        List<Long> timeslots = new ArrayList<Long>();
		long timeslot = startTime + this.intervalLength;
		double gainOfCurrentTimeSlot=0;
		double gainOfPreviousTimeSlot=0;

		while(!isAwakenTimeAtSlot(timeslot, this.initialTime, this.firstPrime, this.secondPrime)){
            timeslots.add(timeslot);
			timeslot = timeslot + this.intervalLength;
		}

        if (timeslots.size() > 10){

            for (int i=0; i<timeslots.size();i++){
                gainOfCurrentTimeSlot = getPointOfTimeslot(timeslots.get(i));
                if (gainOfCurrentTimeSlot  > 0 && gainOfCurrentTimeSlot > gainOfPreviousTimeSlot){
                    extraPrime = (int)((timeslots.get(i) - this.initialTime) / this.intervalLength);

                }
                gainOfPreviousTimeSlot = gainOfCurrentTimeSlot;
            }
        }
        System.out.print("extraPrime:"  + extraPrime +"\n");

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

        for (NeighborItem neighborEntry : getNeighborItems()) {
            dutycyclefCurrentNeighborEntry1 = neighborEntry.getDutycycles()[0];
            dutycyclefCurrentNeighborEntry2 = neighborEntry.getDutycycles()[1];
            initialtimeOfCurrentNeighborEntry = neighborEntry.getInitialtime();
            hopsdOfCurrentNeighborEntry = neighborEntry.getHops();
            boolean wakeupCondition = false;
            if (isAwakenTimeAtSlot(timeslot, initialtimeOfCurrentNeighborEntry, dutycyclefCurrentNeighborEntry1, dutycyclefCurrentNeighborEntry2)){//if the node of current neighbor entry will awake at this timeslot
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


//    public void adjustWeight() {
//        if ((numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) > 0){
//            weightOfDirectGainFrom2HopsNeighbors = (weightOfDirectGainFrom2HopsNeighbors + numberOfNewNeighborFrom2HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
//            weightOfDirectGainFrom3HopsNeighbors = (weightOfDirectGainFrom3HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
//        }
//
//        if ((numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) > 0){
//            weightOfIndirectGainFromFrom1HopsNeighbors = (weightOfIndirectGainFromFrom1HopsNeighbors + numberOfNewInformationFrom1HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
//            weightOfIndirectGainFromFrom2HopsNeighbors = (weightOfIndirectGainFromFrom2HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
//            weightOfIndirectGainFromFrom3HopsNeighbors = (weightOfIndirectGainFromFrom3HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
//        }
//
//    }

}
