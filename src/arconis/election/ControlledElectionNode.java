package arconis.election;

import arconis.*;
import arconis.delegates.*;
import arconis.interfaces.*;
import arconis.log.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.stream.*;

/**
 * Created by aegis on 16/12/15.
 */
public class ControlledElectionNode<TMsg extends Message> extends RingNode<TMsg> {

    public enum State{
        SLEEPING,
        ACTIVE,
        PASSIVE,
        DONE
    }

    public enum Message{
        NOTIFY
    }


    State state;

    final Object lock = new Object();
    HashSet<String> receivedMessages;
    int ID;
    int minimumID;
    int currentDistance;

    public ControlledElectionNode(int objectID, MessageGenerator generator, MessageDecoder decoder, Log log, int ID) throws IOException {
        super(objectID, generator, decoder, log);
        this.ID = ID;
        this.minimumID = this.ID;
        this.receivedMessages = new HashSet<>();
        this.state = State.SLEEPING;
        this.currentDistance = 1;
    }

    public int getID(){
        return this.ID;
    }

    public int getMinimumID() {
        return this.minimumID;
    }

    public HashSet<String> getReceivedMessages(){
        return this.receivedMessages;
    }

    public State getNodeState(){
        return this.state;
    }

    public int getCurrentDistance(){
        return this.currentDistance;
    }

    @Override
    public void sendMessage() throws IOException {
        sendMessage(this.getGenerator().generate(this.getID() + ":FORWARD:LEFT:" + this.getCurrentDistance(), this));
    }

    @Override
    public void sendMessage(TMsg inputMsg) throws IOException {
        synchronized (this.lock){
            this.setIsBusy(true);
            TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);

            if(this.state == State.SLEEPING){
                this.getReceivedMessages().add(inputMsg.getContent());

                this.setNodeState(State.ACTIVE);

                Address address = this.getLeftNodeAddress();
                this.writeToSocket(address, inputMsg);

                address = this.getRightNodeAddress();
                this.writeToSocket(address, inputMsg);
            }

            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage() throws IOException {
        synchronized (this.lock) {
            this.setIsBusy(true);
            TMsg inputMsg = this.getIncomingMessages().removeFirst();

            switch (this.getNodeState()) {
                case SLEEPING:
                    this.getLog().print(this.toString() + " received message in SLEEPING state");
                    handleSLEEPING(inputMsg);
                    break;
                case ACTIVE:
                    this.getLog().print(this.toString() + " received message in SLEEPING state");
                    handleACTIVE(inputMsg);
                    break;
                case PASSIVE:
                    this.getLog().print(this.toString() + " received message in PROCESSING state");
                    handlePASSIVE(inputMsg);
                    break;
            }

            this.setIsBusy(false);
        }
    }

    // Private methods

    private void handleSLEEPING(TMsg inputMsg) throws IOException {
        String[] parts = inputMsg.getContent().split(":");
        TMsg outputMsg = null;

        int idValue = Integer.parseInt(parts[0]);
        String sense = parts[1];
        String direction = parts[2];
        int distance = sense.equals("FORWARD") ? Integer.parseInt(parts[3]) : -1;

        this.setNodeState(this.getID() < idValue ? State.ACTIVE : State.PASSIVE);

        if (this.getNodeState() == State.PASSIVE){
            if (distance > 0){
                outputMsg = this.getGenerator().generate(parts[0] + ":" + sense + ":" + direction + ":" + (distance - 1), this);
                Address address = direction.equals("LEFT") ? this.getLeftNodeAddress() : this.getRightNodeAddress();
                writeToSocket(address, outputMsg);
            }
            else if (distance == 0){
                if (direction.equals("LEFT")){
                    outputMsg = this.getGenerator().generate(parts[0] + "BACKWARD:RIGHT", this);
                    writeToSocket(this.getRightNodeAddress(), outputMsg);
                }
                else{
                    outputMsg = this.getGenerator().generate(parts[0] + "BACKWARD:LEFT", this);
                    writeToSocket(this.getLeftNodeAddress(), outputMsg);
                }
            }
        }

//        String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + (outputMsg == null ? "No message transmission" : outputMsg) + "\n");

//        this.setNodeState(State.PROCESSING);
//        if (this.confirmedNodes.size() == 0){
//            sendNotification();
//        }
    }

    private void handleACTIVE(TMsg inputMsg) throws IOException {
        String[] parts = inputMsg.getContent().split(":");
        TMsg outputMsg = null;

        int idValue = Integer.parseInt(parts[0]);
        String sense = parts[1];
        String direction = parts[2];
        int distance = sense.equals("FORWARD") ? Integer.parseInt(parts[3]) : -1;

        this.setNodeState(this.getID() <= idValue ? State.ACTIVE : State.PASSIVE);

        switch(this.getNodeState()){
            case ACTIVE:
                if (this.getID() == idValue){
                   if (sense.equals("FORWARD")){
                       this.sendNotification();
                   } else {
                       this.setCurrentDistance(this.getCurrentDistance() * 2);

                       outputMsg = this.getGenerator().generate(this.getID() + ":FORWARD:LEFT:" + this.getCurrentDistance(), this);
                       writeToSocket(this.getLeftNodeAddress(), outputMsg);

                       outputMsg = this.getGenerator().generate(this.getID() + ":FORWARD:RIGHT:" + this.getCurrentDistance(), this);
                       writeToSocket(this.getRightNodeAddress(), outputMsg);
                   }
                }
                break;
            case PASSIVE:
                handlePASSIVE(inputMsg);
                break;
        }

//        String ids = this.confirmedNodes.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + (outputMsg == null ? "No message transmission" : outputMsg) + "\n");

    }

    private void handlePASSIVE(TMsg inputMsg) throws IOException {
        String[] parts = inputMsg.getContent().split(":");
        TMsg outputMsg = null;

        int idValue = parts[0].equals(Message.NOTIFY.toString()) ? -1 : Integer.parseInt(parts[0]);
        String sense = parts[1];
        String direction = parts[2];
        int distance = sense.equals("FORWARD") ? Integer.parseInt(parts[3]) : -1;

        if(parts[0].equals(Message.NOTIFY.toString())){
            this.sendNotification();
            return;
        }

        if (distance > 0){
            outputMsg = this.getGenerator().generate(parts[0] + ":" + sense + ":" + direction + ":" + (distance - 1), this);
            Address address = direction.equals("LEFT") ? this.getLeftNodeAddress() : this.getRightNodeAddress();
            writeToSocket(address, outputMsg);
        }
        else if (distance == 0){
            if (direction.equals("LEFT")){
                outputMsg = this.getGenerator().generate(parts[0] + "BACKWARD:RIGHT", this);
                writeToSocket(this.getRightNodeAddress(), outputMsg);
            }
            else{
                outputMsg = this.getGenerator().generate(parts[0] + "BACKWARD:LEFT", this);
                writeToSocket(this.getLeftNodeAddress(), outputMsg);
            }
        }

    }

    private ControlledElectionNode<TMsg> sendNotification() throws IOException {
        TMsg outputMsg = this.getGenerator().generate(Message.NOTIFY.toString() + ":FORWARD:LEFT", this);
        writeToSocket(this.getLeftNodeAddress(), outputMsg);

        outputMsg = this.getGenerator().generate(Message.NOTIFY.toString() + ":FORWARD:RIGHT:", this);
        writeToSocket(this.getRightNodeAddress(), outputMsg);

        this.setNodeState(State.DONE);

        return this;
    }

    private ControlledElectionNode<TMsg> setNodeState(State state){
        this.getLog().print("Changed state from: " + this.state + " to: " + state);
        this.state = state;
        return this;
    }

    private ControlledElectionNode<TMsg> setCurrentDistance(int distance){
        this.currentDistance = distance;
        return this;
    }

    private ControlledElectionNode<TMsg> setMinimunID(int ID){
        this.minimumID = ID;
        return this;
    }

    private ControlledElectionNode<TMsg> writeToSocket(Address address, TMsg inputMsg) throws IOException {
        Socket output = this.getOutputChannel(address.getHost(), address.getPort());
        DataOutputStream out = new DataOutputStream(output.getOutputStream());
        out.writeUTF(inputMsg.encode());
        output.close();
        return this;
    }

}
