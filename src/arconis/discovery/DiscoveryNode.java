package arconis.discovery;

import arconis.Address;
import arconis.MessageData;
import arconis.interfaces.Message;
import arconis.utils.UtilityData;
import sun.misc.resources.Messages_es;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * Created by aegis on 28/04/16.
 */
public abstract class DiscoveryNode<TMsg extends Message> extends PositionNode<TMsg> {

    // Tasks

    class SendMessageTask extends TimerTask {

        DiscoveryNode<TMsg> node;

        public SendMessageTask(DiscoveryNode<TMsg> node){
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

    class WakeUpTask extends TimerTask {

        DiscoveryNode<TMsg> node;

        public WakeUpTask(DiscoveryNode<TMsg> node) {
            this.node = node;
        }

        @Override
        public void run(){
            if (node.workCondition()) {
                if(node.isAwakenTime()) {
                    node.increaseWakeUpTimes();
                }
            } else {
                cancel();
            }
        }
    }

    // Private Fields
    long intervalLength = 1000;
    long epsilon = 10;
    double dutyCycle;
    long initialTime;
    long lastReceivedTime;
    int firstPrime;
    int secondPrime;
    final Object lock = new Object();
    Set<Integer> knowNeighbors;
    int wakeUpTimes;
    int localCounter;

    // Getters && Setters
    public long getIntervalLength() {
        return this.intervalLength;
    }

    public DiscoveryNode<TMsg> setIntervalLength(long interval) {
        this.intervalLength = interval;
        return this;
    }

    public int getFirstPrime() {
        return this.firstPrime;
    }

    public int getSecondPrime() {
        return this.secondPrime;
    }

    public DiscoveryNode<TMsg> setFirstPrime(int prime) {
        this.firstPrime = prime;
        return this;
    }

    public DiscoveryNode<TMsg> setSecondPrime(int prime) {
        this.secondPrime = prime;
        return this;
    }

    public long getInitialTime() {
        return this.initialTime;
    }

    public long getLastReceivedTime() {
        return this.lastReceivedTime;
    }

    public DiscoveryNode<TMsg> setInitialTime(long time) {
        this.initialTime = time;
        return this;
    }

    public DiscoveryNode<TMsg> setLastReceivedTime(long time) {
        this.lastReceivedTime = time;
        return this;
    }

    public double getDutyCycle(){
        return this.dutyCycle;
    }

    public Set<Integer> getKnownNeighbors() {
        return this.knowNeighbors;
    }

    public int getLocalCounter() {
        return this.localCounter;
    }

    public long getIntervalCounter(long time){
        return (time - initialTime) / intervalLength;
    }

    public int getWakeUpTimes(){
        return this.wakeUpTimes;
    }

    // Constructors
    public DiscoveryNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws IOException {
        super(objectID, msgData, posData);

        this.knowNeighbors = Collections.synchronizedSet(new HashSet<>());
        this.dutyCycle = dutyCycle;

        this.firstPrime = 37;
        this.secondPrime = 43;
        this.wakeUpTimes = 0;
    }

    // Public Methods

    @Override
    public void sendMessage() {
        Timer intervalBegin = new Timer();
        Timer intervalEnd = new Timer();

        SendMessageTask scheduleBegin = new SendMessageTask(this);
        SendMessageTask scheduleEnd = new SendMessageTask(this);

        WakeUpTask wakeUpTask = new WakeUpTask(this);

        intervalBegin.scheduleAtFixedRate(wakeUpTask, 0, intervalLength);
        intervalBegin.scheduleAtFixedRate(scheduleBegin, 0, intervalLength);
        intervalEnd.scheduleAtFixedRate(scheduleEnd, intervalLength - epsilon, intervalLength);
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {

            if(this.isAwakenTime()){
                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    writeToSocket(entry, outputMsg);
                }
            }
        }
    }

    public void increaseWakeUpTimes(){
        this.wakeUpTimes++;
    }

    // Protected Methods

    protected boolean isAwakenTime(){
        return isAwakenTime(null);
    }

    protected boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long counter = getIntervalCounter(receivedTime);

        if (counter <= 0)
            return false;

        long firstRem = counter % firstPrime;
        long secondRem = counter % secondPrime;

        return firstRem == 0 || secondRem == 0;
    }

    protected abstract boolean shouldReceiveMessage(TMsg msg);

    @Override
    protected boolean canProcessMessage(TMsg msg) {
        return shouldReceiveMessage(msg) && isAwakenTime(msg);
    }

    protected void writeToSocket(Map.Entry<Integer, Address> entry, TMsg outputMsg){
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
}
