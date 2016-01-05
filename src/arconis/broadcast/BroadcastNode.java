package arconis.broadcast;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.net.*;

import arconis.*;
import arconis.delegates.MessageDecoder;
import arconis.delegates.MessageGenerator;
import arconis.interfaces.*;

public class BroadcastNode<TMsg extends Message> extends Node<TMsg> {

    public enum Message {
        HELLO
    }

    final Object lock = new Object();
    HashSet<String> receivedMessages;

    public BroadcastNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        super(objectID, generator, decoder);

        this.receivedMessages = new HashSet<>();
    }

    public HashSet<String> getReceivedMessages(){
        return this.receivedMessages;
    }

    @Override
    public void sendMessage(){
        sendMessage(this.getGenerator().generate(Message.HELLO.toString(), this));
    }

    @Override
    public void sendMessage(TMsg inputMsg){
        synchronized(this.lock){
            this.setIsBusy(true);
            TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);
            ArrayList<Integer> broadcastNodes = new ArrayList<>();

            if(!this.getReceivedMessages().contains(inputMsg.getContent())) {
                this.getReceivedMessages().add(inputMsg.getContent());

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    Address address = entry.getValue();

                    writeToSocket(address, inputMsg, broadcastNodes, entry);
                }
                this.getLog().print(this.toString() + " Number of Messages: " + this.getBenchmark().getNumberOfMessages() + "\n");
                String ids = broadcastNodes.stream().map(String::valueOf).collect(Collectors.joining(","));
                this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);
            }

            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage(){
        synchronized(this.lock){
            this.setIsBusy(true);
            TMsg inputMsg = this.getIncomingMessages().removeFirst();
            TMsg outputMsg = this.getGenerator().generate(inputMsg.getContent(), this);
            ArrayList<Integer> broadcastNodes = new ArrayList<>();

            if(!this.getReceivedMessages().contains(inputMsg.getContent())) {
                this.getReceivedMessages().add(inputMsg.getContent());

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    if(entry.getKey() == inputMsg.getObjectID())
                        continue;

                    Address address = entry.getValue();
                    writeToSocket(address, outputMsg, broadcastNodes, entry);
                }

                this.getLog().print(this.toString() + " Number of Messages: " + this.getBenchmark().getNumberOfMessages() + "\n");
                String ids = broadcastNodes.stream().map(String::valueOf).collect(Collectors.joining(","));
                this.getLog().print("Input message: " + inputMsg + "\nOutput message: " + outputMsg + "\nBroadcast message to: " + ids);
            }

            this.setIsBusy(false);
        }
    }

    // Private Methods

    private void writeToSocket(Address address, TMsg outputMsg, ArrayList<Integer> broadcastNodes, Map.Entry<Integer, Address> entry){
        try{
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(outputMsg.encode());
            output.close();
            broadcastNodes.add(entry.getKey());
            this.getBenchmark().incrementMessageCount();
        } catch(Exception e){
            this.getLog().print("Unable to connect to node: " + entry.getKey());
        }
    }

}
