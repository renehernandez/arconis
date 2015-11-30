package arconis;

import arconis.delegates.MessageDecoder;
import arconis.delegates.MessageGenerator;
import arconis.interfaces.Message;

import java.io.*;
import java.net.*;
import java.util.*;

public abstract class Node<TMsg extends Message> extends Thread {

    int objectID; // This for object differentiation, not confuse with unique IDs in a network.
    boolean isBusy;

    Address address;
    ServerSocket incomingChannel;
    LinkedList<TMsg> incomingMessages;
    HashMap<Integer, Address> neighbors;
    MessageGenerator<TMsg> generator;
    MessageDecoder<TMsg> decoder;

    public Node(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException{
        this.objectID = objectID;
        this.isBusy = false;
        this.incomingMessages = new LinkedList<TMsg>();
        this.incomingChannel = new ServerSocket(0);
        this.address = new Address("127.0.0.1", this.incomingChannel.getLocalPort());
        this.neighbors = new HashMap<>();
        this.generator = generator;
        this.decoder = decoder;
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

    public LinkedList<TMsg> getIncomingMessages(){
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
        while(true){
            try {
                Socket server = this.getIncomingChannel().accept();
                DataInputStream in =
                        new DataInputStream(server.getInputStream());
                TMsg msg = this.getDecoder().decode(in.readUTF());
                this.incomingMessages.add(msg);

                // Starting message processing.
                new Thread(this::processMessage).start();

                server.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public abstract void sendMessage();

    public abstract void sendMessage(TMsg inputMsg);

    protected abstract void processMessage();

    @Override
    public String toString(){
        return "<objectID: " + this.objectID + ">";
    }

}