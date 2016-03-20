package arconis;

import arconis.benchmark.Benchmark;
import arconis.delegates.MessageDecoder;
import arconis.delegates.MessageGenerator;
import arconis.interfaces.Message;
import arconis.log.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class Node<TMsg extends Message> extends Thread {

    // Private Fields

    int objectID; // This for object differentiation, not confuse with unique IDs in a network.
    boolean isBusy;
    Address address;
    ServerSocket incomingChannel;
    ConcurrentLinkedQueue<TMsg> incomingMessages;
    HashMap<Integer, Address> neighbors;
    MessageData<TMsg> msgData;
    UtilityData utils;
    final Object queueAccess = new Object();

    // Getters & Setters

    public Benchmark getBenchmark(){
        return this.utils.getBenchmark();
    }

    public Log getLog(){
        return this.utils.getLog();
    }

    public Address getAddress(){
        return this.address;
    }

    public String getHost(){
        return this.address.getHost();
    }

    public int getPort(){
        return this.address.getPort();
    }

    public HashMap<Integer, Address> getNeighbors(){
        return this.neighbors;
    }

    public int getObjectID(){
        return this.objectID;
    }

    public boolean getIsBusy(){
        return this.isBusy;
    }

    public Node<TMsg> setIsBusy(boolean flag){
        this.isBusy = flag;
        return this;
    }

    public ConcurrentLinkedQueue<TMsg> getIncomingMessages(){
        return this.incomingMessages;
    }

    public MessageGenerator<TMsg> getGenerator(){
        return this.msgData.getGenerator();
    }

    public MessageDecoder<TMsg> getDecoder(){
        return this.msgData.getDecoder();
    }

    public ServerSocket getIncomingChannel(){
        return this.incomingChannel;
    }

    public Socket getOutputChannel(String host, int port) throws IOException{
        return new Socket(host, port);
    }

    // Constructors

    public Node(int objectID, MessageData<TMsg> msgData, UtilityData utils) throws IOException{
        this.objectID = objectID;
        this.isBusy = false;
        this.incomingMessages = new ConcurrentLinkedQueue<>();
        this.incomingChannel = new ServerSocket(0);
        this.address = new Address("127.0.0.1", this.incomingChannel.getLocalPort());
        this.neighbors = new HashMap<>();
        this.msgData = msgData;
        this.utils = utils;
    }

    public Node(int objectID, MessageData<TMsg> msgData) throws IOException {
        this(objectID, msgData, UtilityData.DefaultUtility());
    }

    // Public methods

    public Node<TMsg> addNeighbor(Node<TMsg> v){
        this.neighbors.put(v.getObjectID(), v.getAddress());
        return this;
    }

    public Node<TMsg> removeNeighbor(Node<TMsg> v){
        this.neighbors.remove(v.getObjectID());
        return this;
    }

    public void run(){
        new Thread(this::processEnqueuedMessages).start();

        while(workCondition()){
            try {
                Socket server = this.getIncomingChannel().accept();
                new Thread(() -> {
                    processIncomingMessage(server);
                }).start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString(){
        return "<objectID: " + this.objectID + ">";
    }

    public abstract void sendMessage();

    public abstract void sendMessage(TMsg inputMsg);

    // Protected Methods

    protected abstract void processMessage(TMsg msg);

    protected abstract boolean workCondition();

    protected boolean canProcessMessage(TMsg msg){
        return true;
    }


    // Private Methods

    private void processEnqueuedMessages(){
        while(workCondition()){
            synchronized (this.queueAccess) {
                if (getIncomingMessages().size() > 0) {
                    TMsg msg = getIncomingMessages().poll();
                    if (canProcessMessage(msg)) {
                        // Starting message processing.
                        processMessage(msg);
                    }
                }
            }
        }
    }

    private void processIncomingMessage(Socket server){
        try {
            DataInputStream in =
                    new DataInputStream(server.getInputStream());
            TMsg msg = this.getDecoder().decode(in.readUTF());
            msg.setReceivedTime(System.currentTimeMillis());

            synchronized (this.queueAccess) {
                if (canProcessMessage(msg)) {
                    getIncomingMessages().add(msg);
                    msg = getIncomingMessages().poll();

                    final TMsg msgParam = msg;

                    // Starting message processing.
                    processMessage(msgParam);
                }
            }
            in.close();
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}