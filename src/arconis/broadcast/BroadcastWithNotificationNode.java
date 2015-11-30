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


    State state;

    final Object lock = new Object();
    HashSet<String> receivedMessages;
    HashMap<Integer, Boolean> confirmedNodes;
    int alreadyConfirmed;
    int parentObjectID;

    public BroadcastWithNotificationNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        super(objectID, generator, decoder);

        this.receivedMessages = new HashSet<>();
        this.state = State.SLEEPING;
        this.confirmedNodes = new HashMap<>();
        this.alreadyConfirmed = 0;
    }

    public HashSet<String> getReceivedMessages(){
        return this.receivedMessages;
    }

    public State getNodeState() {
        return this.state;
    }

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
                this.getReceivedMessages().add(inputMsg.getContent());

                this.setNodeState(State.INITIATOR);

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()) {
                    Address address = entry.getValue();
                    try {
                        Socket output = this.getOutputChannel(address.getHost(), address.getPort());
                        DataOutputStream out = new DataOutputStream(output.getOutputStream());
                        out.writeUTF(inputMsg.encode());
                        output.close();
                        this.confirmedNodes.put(entry.getKey(), false);
                    } catch (Exception e) {
                        System.out.println("Unable to connect to node: " + entry.getKey());
                    }
                }

                String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
                System.out.println("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);
                this.setNodeState(State.PROCESSING);
            }

            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage() {
        synchronized (this.lock) {
            this.setIsBusy(true);
            TMsg inputMsg = this.getIncomingMessages().removeFirst();

            switch (this.getNodeState()) {
                case SLEEPING:
                    System.out.println(this.toString() + " received message in SLEEPING state");
                    handleSLEEPING(inputMsg);
                    break;
                case PROCESSING:
                    System.out.println(this.toString() + " received message in PROCESSING state");
                    handlePROCESSING(inputMsg);
                    break;
            }

            this.setIsBusy(false);
        }
    }

    // Private methods

    private void handleSLEEPING(TMsg inputMsg){
        this.setParentNode(inputMsg.getObjectID());
        TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);

        for (Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()) {
            if (entry.getKey() == inputMsg.getObjectID())
                continue;

            Address address = entry.getValue();
            try {
                Socket output = this.getOutputChannel(address.getHost(), address.getPort());
                DataOutputStream out = new DataOutputStream(output.getOutputStream());
                out.writeUTF(outputMsg.encode());
                output.close();
                this.confirmedNodes.put(entry.getKey(), false);
            } catch (Exception e) {
                System.out.println("Unable to connect to node: " + entry.getKey());
            }
        }
        String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);

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

        System.out.println(message);

        if(this.alreadyConfirmed == this.confirmedNodes.size()){
            sendNotification();
        }
    }

    private void sendNotification(){
        Address address = this.getNeighbors().get(this.parentObjectID);
        try {
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(this.getGenerator().generate(Message.NOTIFY.toString(), this).encode());
            output.close();
        } catch (Exception e) {
            System.out.println("Unable to connect to node: " + this.parentObjectID);
        }

        System.out.println("Sent confirmation to parent: " + this.parentObjectID);
        this.setNodeState(State.DONE);
    }

    private BroadcastWithNotificationNode<TMsg> setNodeState(State state){
        System.out.println("Changed state from: " + this.state + " to: " + state);
        this.state = state;
        return this;
    }

    private BroadcastWithNotificationNode<TMsg> setParentNode(int parentObjectID){
        this.parentObjectID = parentObjectID;
        System.out.println("Set Node: " + this.getObjectID() + ", Parent: " + this.parentObjectID);
        return this;
    }
}