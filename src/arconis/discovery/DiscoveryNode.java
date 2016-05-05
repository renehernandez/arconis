package arconis.discovery;

import arconis.Address;
import arconis.MessageData;
import arconis.interfaces.Message;
import arconis.utils.UtilityData;
import sun.misc.resources.Messages_es;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by aegis on 28/04/16.
 */
public abstract class DiscoveryNode<TMsg extends Message> extends PositionNode<TMsg> {

    // Private Fields
    long intervalLength = 100;
    long epsilon = 10;
    double dutyCycle;
    long initialTime;
    long lastReceivedTime;
    int firstPrime;
    int secondPrime;
    final Object lock = new Object();
    Set<Integer> knowNeighbors;

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

    public long getIntervalCounter(long time){
        return (time - initialTime) / intervalLength;
    }

    // Constructors
    public DiscoveryNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws IOException {
        super(objectID, msgData, posData);

        this.knowNeighbors = Collections.synchronizedSet(new HashSet<>());
        this.dutyCycle = dutyCycle;

        this.firstPrime = 3;
        this.secondPrime = 5;
    }

    // Protected Methods

    protected abstract boolean isAwakenTime(TMsg msg);

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
