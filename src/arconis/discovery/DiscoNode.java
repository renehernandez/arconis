package arconis.discovery;

import arconis.*;
import arconis.tests.TestData;

import javax.swing.plaf.synth.SynthCheckBoxUI;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by aegis on 02/12/15.
 */
public class DiscoNode<TMsg extends DiscoveryMessage> extends DiscoveryNode<TMsg> {

    class DiscoScheduledTask extends TimerTask {

        DiscoNode<TMsg> node;

        public DiscoScheduledTask(DiscoNode<TMsg> node){
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

    // Constructors

    public DiscoNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws Exception {
        super(objectID, msgData, posData, dutyCycle);

        setInitialTime(System.currentTimeMillis());
    }

    // Public Methods

    @Override
    public void sendMessage() {
        Timer intervalBegin = new Timer();
        Timer intervalEnd = new Timer();

        DiscoScheduledTask scheduleBegin = new DiscoScheduledTask(this);
        DiscoScheduledTask scheduleEnd = new DiscoScheduledTask(this);

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

    // Protected Methods

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            if (!this.getKnownNeighbors().contains(msg.getObjectID())) {
                this.getKnownNeighbors().add(msg.getObjectID());
                System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors()
                        + ", Time Period: " + (msg.getReceivedTime() - getInitialTime())/getIntervalLength());
                this.setLastReceivedTime(msg.getReceivedTime());
                runProcessedMessageEvent();
            }
        }
    }

    protected boolean isAwakenTime(){
        return isAwakenTime(null);
    }

    @Override
    protected boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long diff = receivedTime - initialTime < 0 ? 0 : receivedTime - initialTime;
        long firstRem = (diff/ intervalLength ) % firstPrime;
        long secondRem = (diff/ intervalLength ) % secondPrime;

        return firstRem == 0 || secondRem == 0;
    }

    @Override
    protected boolean shouldReceiveMessage(TMsg msg) {
        double xDiff = this.getXPos() - msg.getXPos();
        double yDiff = this.getYPos() - msg.getYPos();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff) <= msg.getRadius();
    }

}
