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

    // Tasks

    class ExtraSlotTask extends TimerTask {

        AccLeaderNode<TMsg> node;

        public ExtraSlotTask(AccLeaderNode<TMsg> node) {
            this.node = node;
        }

        @Override
        public void run(){
            if(node.workCondition()) {
                if (node.isAwakenTime(null)) {
                    node.setExtraSlot(System.currentTimeMillis());
                }
            } else {
                cancel();
            }
        }
    }

    class AdjustWeightsTask extends TimerTask {

        AccLeaderNode<TMsg> node;

        public AdjustWeightsTask(AccLeaderNode<TMsg> node) {
            this.node = node;
        }

        @Override
        public void run(){
            if(node.workCondition()) {
                if (node.isAwakenTime()) {
                    node.adjustWeight();
                }
            } else {
                cancel();
            }
        }
    }



    // Private Fields

    private int extraSlot=-1;
    /*private double weightOfDirectGainFrom2HopsNeighbors=0.5;
    private double weightOfDirectGainFrom3HopsNeighbors=0.5;
    private double weightOfIndirectGainFromFrom1HopsNeighbors=0.4;
    private double weightOfIndirectGainFromFrom2HopsNeighbors=0.3;
    private double weightOfIndirectGainFromFrom3HopsNeighbors=0.3;*/

    private double weightOfDirectGainFrom2HopsNeighbors=0.5;
    private double weightOfDirectGainFrom3HopsNeighbors=0.5;
    private double weightOfIndirectGainFromFrom1HopsNeighbors=0;
    private double weightOfIndirectGainFromFrom2HopsNeighbors=0;
    private double weightOfIndirectGainFromFrom3HopsNeighbors=0;


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
    public void sendMessage(){
        super.sendMessage();

        Timer extraSlot = new Timer();
        ExtraSlotTask extraPrimeTask = new ExtraSlotTask(this);

        Timer adjustWeight = new Timer();
        AdjustWeightsTask adjustWeightsTask = new AdjustWeightsTask(this);

        extraSlot.scheduleAtFixedRate(extraPrimeTask, intervalLength + 3000, intervalLength);

        adjustWeight.scheduleAtFixedRate(adjustWeightsTask, intervalLength + 3700, intervalLength);
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
        //System.out.println("ID: " + idOfSendingNode + ", hopsdOfSendingNode: " + hopsdOfSendingNode );

        switch (hopsdOfSendingNode) {
            case 2:  numberOfNewNeighborFrom2HopsNeighbors++;
                //System.out.println("New Neighbor ID: " + idOfSendingNode + ", numberOfNewNeighborFrom2HopsNeighbors: " + numberOfNewNeighborFrom2HopsNeighbors );
                break;
            case 3:  numberOfNewNeighborFrom3HopsNeighbors++;
                //System.out.println("New Neighbor ID: " + idOfSendingNode + ", numberOfNewNeighborFrom3HopsNeighbors: " + numberOfNewNeighborFrom3HopsNeighbors );
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
                            //System.out.println("New Information ID: " + idOfSendingNode + ", numberOfNewInformationFrom1HopsNeighbors: " + numberOfNewInformationFrom1HopsNeighbors );
                            break;
                        case 2:
                            numberOfNewInformationFrom2HopsNeighbors++;
                            //System.out.println("New Information ID: " + idOfSendingNode + ", numberOfNewInformationFrom2HopsNeighbors: " + numberOfNewInformationFrom2HopsNeighbors );
                            break;
                        case 3:
                            numberOfNewInformationFrom3HopsNeighbors++;
                            //System.out.println("New Information ID: " + idOfSendingNode + ", numberOfNewInformationFrom3HopsNeighbors: " + numberOfNewInformationFrom3HopsNeighbors );
                            break;

                        default:
                            break;
                    }
                }
            } else {  //neighbor in the localNeighborTable
                hopsdOfCurrentNeighborEntry = neighborEntry.getHops() + 1;
                NeighborItem current = getNeighborItems().stream().filter(x -> x.getId() == neighborEntry.getId()).findFirst().get();

                if (hopsdOfCurrentNeighborEntry < current.getHops() && hopsdOfCurrentNeighborEntry <= 3) { // only keep one entry for each node with minimum hops to current node and only}
                    current.setDutycycle(neighborEntry.getDutycycle());
                    current.setInitialtime(neighborEntry.getInitialtime());
                    current.setHops(hopsdOfCurrentNeighborEntry);

                    switch (hopsdOfSendingNode) {
                        case 1:
                            numberOfNewInformationFrom1HopsNeighbors++;
                            //System.out.println("Update Information  ID: " + idOfSendingNode + ", numberOfNewInformationFrom1HopsNeighbors: " + numberOfNewInformationFrom1HopsNeighbors );
                            break;
                        case 2:
                            numberOfNewInformationFrom2HopsNeighbors++;
                            //System.out.println("Update Information ID: " + idOfSendingNode + ", numberOfNewInformationFrom2HopsNeighbors: " + numberOfNewInformationFrom2HopsNeighbors );
                            break;
                        case 3:
                            numberOfNewInformationFrom3HopsNeighbors++;
                            //System.out.println("Update Information ID: " + idOfSendingNode + ", numberOfNewInformationFrom3HopsNeighbors: " + numberOfNewInformationFrom3HopsNeighbors );
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
    protected boolean isAwakenTime()
    {
        return isAwakenTime(null) || this.isAwakenTimeAtExtraSlot(null);
    }

    // Private Methods

    @Override
    protected boolean canProcessMessage(TMsg msg) {
        return shouldReceiveMessage(msg) && (isAwakenTime(msg) || isAwakenTimeAtExtraSlot(msg));
    }

    protected boolean isAwakenTimeAtExtraSlot(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long counter = getIntervalCounter(receivedTime);

        if (counter <= 0 || extraSlot == -1)
            return false;

        long extraRem = counter % extraSlot;
        /*if (extraRem == 0){
            System.out.println("leader wake up at Extraslot: "  + counter);
        }*/
        return extraRem == 0 ;
    }

	 private boolean isAwakenTimeAtSlot(long slotTime, long initialTime, int firstPrime, int secondPrime){
         long counter =  (slotTime - initialTime) / intervalLength;
         if (counter <= 0)
             return false;

         long firstRem = counter % firstPrime;
         long secondRem = counter % secondPrime;

         return firstRem == 0 || secondRem == 0;
    }




    private int getHopsOfNode(int id) {
        int hops=0;

        for (NeighborItem neighborEntry : getNeighborItems())
            if (neighborEntry.getId() == id) {
                hops = neighborEntry.getHops();
            }

        return hops;
    }

    private void setExtraSlot(long startTime) {
        List<Long> timeslots = new ArrayList<Long>();
		long timeslot = startTime + this.intervalLength;
		double gainOfCurrentTimeSlot=0;
		double highestGain=0;
        extraSlot=-1;

		while(!isAwakenTimeAtSlot(timeslot, this.initialTime, this.firstPrime, this.secondPrime)){
            timeslots.add(timeslot);
			timeslot = timeslot + this.intervalLength;
		}

        //if (timeslots.size() > 10){

            for (int i=0; i<timeslots.size();i++){
                gainOfCurrentTimeSlot = getPointOfTimeslot(timeslots.get(i));
                if (gainOfCurrentTimeSlot  > 0.0 && gainOfCurrentTimeSlot > highestGain){
                    extraSlot = (int)((timeslots.get(i) - this.initialTime) / this.intervalLength);
                    highestGain = gainOfCurrentTimeSlot;
                }

            }
        //}
        //System.out.println("extraSlot: "  + extraSlot + ", Time Period: " + getIntervalCounter(startTime) + ", Gain: " + highestGain);

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
                            //System.out.println("ID: " + neighborEntry.getId()  +", Time Period: " + getIntervalCounter(timeslot) + ", hops: " + hopsdOfCurrentNeighborEntry);
                            break;
                        case 3: wakeupCountOf3HopNeighbors ++ ;
                            //System.out.println("ID: " + neighborEntry.getId() +", Time Period: " + getIntervalCounter(timeslot) + ", hops: " + hopsdOfCurrentNeighborEntry);
                            break;
                        default: break ;
                    }
            }
        }

        point = 0.6 * (weightOfDirectGainFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfDirectGainFrom3HopsNeighbors * wakeupCountOf3HopNeighbors ) +
                0.4 * (weightOfIndirectGainFromFrom1HopsNeighbors * wakeupCountOf1HopNeighbors + weightOfIndirectGainFromFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfIndirectGainFromFrom3HopsNeighbors * wakeupCountOf3HopNeighbors );

        //point = 0.6 * (weightOfDirectGainFrom2HopsNeighbors * wakeupCountOf2HopNeighbors + weightOfDirectGainFrom3HopsNeighbors * wakeupCountOf3HopNeighbors ) +
                //0.4 * (weightOfIndirectGainFromFrom1HopsNeighbors * wakeupCountOf1HopNeighbors );


        return point;
    }


    public void adjustWeight() {
        /*System.out.println("numberOfNewNeighborFrom2HopsNeighbors: "  + numberOfNewNeighborFrom2HopsNeighbors +
                ", numberOfNewNeighborFrom3HopsNeighbors: "  + numberOfNewNeighborFrom3HopsNeighbors +
                ", numberOfNewInformationFrom1HopsNeighbors: "  + numberOfNewInformationFrom1HopsNeighbors +
                ", numberOfNewInformationFrom2HopsNeighbors: "  + numberOfNewInformationFrom2HopsNeighbors +
                ", numberOfNewInformationFrom3HopsNeighbors: "  + numberOfNewInformationFrom3HopsNeighbors );*/
        if ((numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) > 0){
            weightOfDirectGainFrom2HopsNeighbors = (weightOfDirectGainFrom2HopsNeighbors + numberOfNewNeighborFrom2HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
            weightOfDirectGainFrom3HopsNeighbors = (weightOfDirectGainFrom3HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors / (numberOfNewNeighborFrom2HopsNeighbors + numberOfNewNeighborFrom3HopsNeighbors) ) / 2;
        }

        if ((numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) > 0){
            weightOfIndirectGainFromFrom1HopsNeighbors = (weightOfIndirectGainFromFrom1HopsNeighbors + numberOfNewInformationFrom1HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
            weightOfIndirectGainFromFrom2HopsNeighbors = (weightOfIndirectGainFromFrom2HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
            weightOfIndirectGainFromFrom3HopsNeighbors = (weightOfIndirectGainFromFrom3HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors / (numberOfNewInformationFrom1HopsNeighbors + numberOfNewInformationFrom2HopsNeighbors + numberOfNewInformationFrom3HopsNeighbors) ) / 2;
        }
        /*System.out.println("weightOfDirectGainFrom2HopsNeighbors: "  + weightOfDirectGainFrom2HopsNeighbors +
                ", weightOfDirectGainFrom3HopsNeighbors: "  + weightOfDirectGainFrom3HopsNeighbors +
                ", weightOfIndirectGainFromFrom1HopsNeighbors: "  + weightOfIndirectGainFromFrom1HopsNeighbors +
                ", weightOfIndirectGainFromFrom2HopsNeighbors: "  + weightOfIndirectGainFromFrom2HopsNeighbors +
                ", weightOfIndirectGainFromFrom3HopsNeighbors: "  + weightOfIndirectGainFromFrom3HopsNeighbors );*/

        numberOfNewNeighborFrom2HopsNeighbors=0;
        numberOfNewNeighborFrom3HopsNeighbors=0;
        numberOfNewInformationFrom1HopsNeighbors=0;
        numberOfNewInformationFrom2HopsNeighbors=0;
        numberOfNewInformationFrom3HopsNeighbors=0;


    }

}
