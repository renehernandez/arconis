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

    // Constructors

    public DiscoNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws Exception {
        super(objectID, msgData, posData, dutyCycle);

        setInitialTime(System.currentTimeMillis());
    }

    // Protected Methods

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            if (!this.getKnownNeighbors().contains(msg.getObjectID())) {
                this.getKnownNeighbors().add(msg.getObjectID());
                System.out.println("ID: " + this.getObjectID() + ", known: " + this.getKnownNeighbors()
                        + ", Time Period: " + getIntervalCounter(msg.getReceivedTime()) + ", WakeUp Times: " + getWakeUpTimes());
                this.setLastReceivedTime(msg.getReceivedTime());
                runProcessedMessageEvent();
            }
        }
    }

    @Override
    protected boolean shouldReceiveMessage(TMsg msg) {
        double xDiff = this.getXPos() - msg.getXPos();
        double yDiff = this.getYPos() - msg.getYPos();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff) <= msg.getRadius();
    }

}
