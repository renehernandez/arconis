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

    int objectID; // This for object differentiation, not confuse with unique IDs in a network.
    boolean isBusy;

    Address address;
    ServerSocket incomingChannel;
    ConcurrentLinkedQueue<TMsg> incomingMessages;
    HashMap<Integer, Address> neighbors;
    MessageGenerator<TMsg> generator;
    MessageDecoder<TMsg> decoder;
    Benchmark benchmark;
    Log log;

    final Object queueAccess = new Object();

    public Node(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder, Log log, Benchmark benchmark) throws IOException{
        this.objectID = objectID;
        this.isBusy = false;
        this.incomingMessages = new ConcurrentLinkedQueue<TMsg>();
        this.incomingChannel = new ServerSocket(0);
        this.address = new Address("127.0.0.1", this.incomingChannel.getLocalPort());
        this.neighbors = new HashMap<>();
        this.generator = generator;
        this.decoder = decoder;
        this.log = log;
        this.benchmark = benchmark;
    }

    public Node(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        this(objectID, generator, decoder, new ConsoleLog(), new Benchmark());
    }

    public Benchmark getBenchmark(){
        return this.benchmark;
    }

    public Log getLog(){
        return this.log;
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

    public Node<TMsg> addNeighbor(Node<TMsg> v){
        this.neighbors.put(v.getObjectID(), v.getAddress());
        return this;
    }

    public Node<TMsg> removeNeighbor(Node<TMsg> v){
        this.neighbors.remove(v.getObjectID());
        return this;
    }

    public MessageGenerator<TMsg> getGenerator(){
        return this.generator;
    }

    public MessageDecoder<TMsg> getDecoder(){
        return this.decoder;
    }

    public ServerSocket getIncomingChannel(){
        return this.incomingChannel;
    }

    public Socket getOutputChannel(String host, int port) throws IOException{
        return new Socket(host, port);
    }

    public void run(){
        new Thread(this::processRemainingMessages).start();

        while(workCondition()){
            try {
                Socket server = this.getIncomingChannel().accept();
                DataInputStream in =
                        new DataInputStream(server.getInputStream());
                TMsg msg = this.getDecoder().decode(in.readUTF());
                msg.setReceivedTime(System.currentTimeMillis());

                synchronized (this.queueAccess) {
                    if (getIncomingMessages().size() > 0) {
                        getIncomingMessages().add(msg);
                        msg = getIncomingMessages().poll();
                    }

                    final TMsg msgParam = msg;

                    // Starting message processing.
                    new Thread(() -> {
                        processMessage(msgParam);
                    }).start();
                }

                server.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void processRemainingMessages(){
        while(workCondition()){
            synchronized (this.queueAccess) {
                if (getIncomingMessages().size() > 0) {
                    TMsg msg = getIncomingMessages().poll();

                    // Starting message processing.
                    new Thread(() -> {
                        processMessage(msg);
                    }).start();
                }
            }
        }
    }

    public abstract void sendMessage();

    public abstract void sendMessage(TMsg inputMsg);

    protected abstract void processMessage(TMsg msg);

    protected abstract boolean workCondition();

    @Override
    public String toString(){
        return "<objectID: " + this.objectID + ">";
    }

}