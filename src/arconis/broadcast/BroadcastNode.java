package arconis.broadcast;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.net.*;

import arconis.*;

public class BroadcastNode<TMsg extends Message> extends Node<TMsg> {

    final Object lock = new Object();
    HashSet<String> receivedMessages;

    public BroadcastNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        super(objectID, generator, decoder);

        this.receivedMessages = new HashSet<>();
    }

    public HashSet<String> getReceivedMessages(){
        return this.receivedMessages;
    }

    public void sendMessage(TMsg msg){
        synchronized(this.lock){
            this.setIsBusy(true);

            ArrayList<Integer> broadcastNodes = new ArrayList<>();

            if(!this.receivedMessages.contains(msg.getContent())) {
                this.receivedMessages.add(msg.getContent());
                System.out.println();

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    Address address = entry.getValue();

                    try{
                        Socket output = this.getOutputChannel(address.getHost(), address.getPort());
                        DataOutputStream out = new DataOutputStream(output.getOutputStream());
                        out.writeUTF(msg.encode());
                        output.close();
                        broadcastNodes.add(entry.getKey());
                    } catch(Exception e){
                        System.out.println("Unable to connect to node: " + entry.getKey());
                    }
                }
            }

            String ids = broadcastNodes.stream().map(String::valueOf).collect(Collectors.joining(","));
            System.out.println("From sendMessage: " + msg + "\nReached nodes: " + ids);

            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage(){
        synchronized(this.lock){
            this.setIsBusy(true);
            TMsg msg = this.getIncomingMessages().removeFirst();
            TMsg outputMsg = this.getGenerator().generate(msg.getContent(), this);

            ArrayList<Integer> broadcastedNode = new ArrayList<>();

            if(!this.receivedMessages.contains(msg.getContent())) {
                this.receivedMessages.add(msg.getContent());

                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    if(entry.getKey() == msg.getObjectID())
                        continue;

                    Address address = entry.getValue();

                    try{
                        Socket output = this.getOutputChannel(address.getHost(), address.getPort());
                        DataOutputStream out = new DataOutputStream(output.getOutputStream());
                        out.writeUTF(outputMsg.encode());
                        output.close();
                        broadcastedNode.add(entry.getKey());
                    } catch(Exception e){
                        System.out.println("Unable to connect to node: " + entry.getKey());
                    }
                }
            }

            String ids = broadcastedNode.stream().map(String::valueOf).collect(Collectors.joining(","));
            System.out.println("Received message: " + msg + "\nOutput message: " + outputMsg + "\nReached nodes: " + ids);

            this.setIsBusy(false);
        }
    }

}
