package arconis;

import arconis.benchmark.*;
import arconis.delegates.*;
import arconis.events.EventListener;
import arconis.interfaces.*;
import arconis.log.*;
import arconis.tests.*;
import arconis.utils.*;

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
    TestData testData;
    final Object queueAccess = new Object();
    boolean workCondition;

    ArrayList<EventListener> stopListeners;
    ArrayList<EventListener> startListeners;
    ArrayList<EventListener> processedMessageListeners;

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

    public TestData getTestData(){
        return this.testData;
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
        this.stopListeners = new ArrayList<>();
        this.startListeners = new ArrayList<>();
        this.processedMessageListeners = new ArrayList<>();
        this.workCondition = true;
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
        runStartEvent();
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

    public void stopNode(){
        this.workCondition = false;

        runStopEvent();
    }

    // Protected Methods

    protected abstract void processMessage(TMsg msg);

    protected boolean workCondition(){
        return this.workCondition;
    }

    protected boolean canProcessMessage(TMsg msg){
        return true;
    }


    // Private Methods

    private void processEnqueuedMessages(){
        while(workCondition()){
            synchronized (this.queueAccess) {
                if (getIncomingMessages().size() > 0) {
                    final TMsg msg = getIncomingMessages().poll();
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
                }
            }
            in.close();
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Events Section

    public void addStartListener(EventListener listener) { this.startListeners.add(listener); }

    protected void runStartEvent() { this.startListeners.forEach(x -> x.respondTo(this));}

    public void addStopListener(EventListener listener){
        this.stopListeners.add(listener);
    }

    protected void runStopEvent(){
        this.stopListeners.forEach(x -> x.respondTo(this));
    }

    protected void runProcessedMessageEvent() { this.processedMessageListeners.forEach(x -> x.respondTo(this)); }

    public void addProcessedMessageListener(EventListener listener) {this.processedMessageListeners.add(listener); }

}