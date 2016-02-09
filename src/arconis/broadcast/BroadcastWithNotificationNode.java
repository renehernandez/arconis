package arconis.broadcast;

import arconis.*;
import arconis.delegates.*;
import arconis.interfaces.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

/**
 * Created by aegis on 29/11/15.
 */
public class BroadcastWithNotificationNode<TMsg extends Message> extends Node<TMsg> {

    // Private Fields

    State state;
    final Object lock = new Object();
    HashSet<String> receivedMessages;
    HashMap<Integer, Boolean> confirmedNodes;
    int alreadyConfirmed;
    int parentObjectID;
    boolean initiator;

    // Public Enums

    public enum State{
        INITIATOR,
        SLEEPING,
        PROCESSING,
        DONE
    }

    public enum Message{
        HELLO,
        NOTIFY
    }

    // Getters & Setters

    public HashSet<String> getReceivedMessages(){
        return this.receivedMessages;
    }

    public State getNodeState() {
        return this.state;
    }

    private BroadcastWithNotificationNode<TMsg> setNodeState(State state){
        this.getLog().print("Changed state from: " + this.state + " to: " + state);
        this.state = state;
        return this;
    }

    private BroadcastWithNotificationNode<TMsg> setParentNode(int parentObjectID){
        this.parentObjectID = parentObjectID;
        this.getLog().print("Set Node: " + this.getObjectID() + ", Parent: " + this.parentObjectID);
        return this;
    }

    // Constructors

    public BroadcastWithNotificationNode(int objectID, MessageData<TMsg> msgData) throws IOException {
        super(objectID, msgData);

        this.receivedMessages = new HashSet<>();
        this.state = State.SLEEPING;
        this.confirmedNodes = new HashMap<>();
        this.alreadyConfirmed = 0;
        this.initiator = false;
    }

    // Public Methods

    @Override
    public void sendMessage(){
        sendMessage(this.getGenerator().generate(Message.HELLO.toString(), this));
    }

    @Override
    public void sendMessage(TMsg inputMsg) {
        synchronized (this.lock){
            this.setIsBusy(true);
            TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);

            if(this.state == State.SLEEPING){
                this.getBenchmark().start();
                this.getReceivedMessages().add(inputMsg.getContent());

                this.setNodeState(State.INITIATOR);
                this.initiator = true;

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()) {
                    Address address = entry.getValue();

                    writeToSocket(address, inputMsg, entry);
                }

                String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
                this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);
                this.setNodeState(State.PROCESSING);
            }

            this.setIsBusy(false);
        }
    }

    // Fix this code
    @Override
    protected void processMessage(TMsg msg) {
        synchronized (this.lock) {
            this.setIsBusy(true);
            TMsg inputMsg = this.getIncomingMessages().poll();

            switch (this.getNodeState()) {
                case SLEEPING:
                    this.getLog().print(this.toString() + " received message in SLEEPING state");
                    handleSLEEPING(inputMsg);
                    break;
                case PROCESSING:
                    this.getLog().print(this.toString() + " received message in PROCESSING state");
                    handlePROCESSING(inputMsg);
                    break;
            }

            this.setIsBusy(false);
        }
    }

    // Fix this code
    @Override
    protected boolean workCondition(){
        return false;
    }

    // Private Methods

    private void handleSLEEPING(TMsg inputMsg){
        this.setParentNode(inputMsg.getObjectID());
        TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);

        this.getBenchmark().start();
        this.getReceivedMessages().add(inputMsg.getContent());

        for (Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()) {
            if (entry.getKey() == inputMsg.getObjectID())
                continue;

            Address address = entry.getValue();
            writeToSocket(address, outputMsg, entry);
        }

        String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);

        this.setNodeState(State.PROCESSING);
        if (this.confirmedNodes.size() == 0){
            sendNotification();
        }
    }

    private void handlePROCESSING(TMsg inputMsg){
        String message = "Input message: " + inputMsg;
        if(this.getReceivedMessages().contains(inputMsg.getContent())) {
            this.confirmedNodes.put(inputMsg.getObjectID(), true);
            this.alreadyConfirmed++;
            message += "\nReceived confirmation as a question from: " + inputMsg.getObjectID();
        }
        else if(inputMsg.getContent().equals(Message.NOTIFY.toString()) && !this.confirmedNodes.get(inputMsg.getObjectID())){
            this.confirmedNodes.put(inputMsg.getObjectID(), true);
            this.alreadyConfirmed++;
            message += "\nReceived confirmation as a notification from: " + inputMsg.getObjectID();
        }

        this.getLog().print(message);

        if(this.alreadyConfirmed == this.confirmedNodes.size()){
            sendNotification();
        }
    }

    private void sendNotification(){
        boolean successfulConnection = true;
        Address address = this.getNeighbors().get(this.parentObjectID);
        try {
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(this.getGenerator().generate(Message.NOTIFY.toString(), this).encode());
            output.close();

            if(!this.initiator)
                this.getBenchmark().incrementMessageCount();
        } catch (Exception e) {
            this.getLog().print("Unable to connect to node: " + this.parentObjectID);
            successfulConnection = false;
        }

        if(successfulConnection)
            this.getLog().print("Sent confirmation to parent: " + this.parentObjectID);

        this.getBenchmark().stop();
        this.getLog().print(this.toString() + " Number of Messages: " + this.getBenchmark().getNumberOfMessages() + "\nTime Elapsed: " + this.getBenchmark().getTimeElapsed() + "\n");
        this.setNodeState(State.DONE);
    }

    private void writeToSocket(Address address, TMsg outputMsg, Map.Entry<Integer, Address> entry){
        try {
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(outputMsg.encode());
            output.close();
            this.getBenchmark().incrementMessageCount();
            this.confirmedNodes.put(entry.getKey(), false);
        } catch (Exception e) {
            this.getLog().print("Unable to connect to node: " + entry.getKey());
        }
    }

}
