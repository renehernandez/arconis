package arconis.discovery;

import arconis.*;
import arconis.delegates.*;
import arconis.interfaces.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class DiscoNode<TMsg extends DiscoveryMessage> extends PositionNode<TMsg> {

//    public enum Status {
//        SLEEP,
//        LISTEN,
//        TRANSMITTING
//    }

    static final int intervalLength = 5;

    ArrayList<Integer> primes;
    final static int MAX = 100000;
    int firstPrime;
    int secondPrime;
    double dutyCycle;
    long initialTime;
//    Status status;

    final Object lock = new Object();

    Set<Integer> knownNeighbors;


    public DiscoNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder, double xPos, double yPos, double radius, double dutyCycle) throws Exception {
        super(objectID, generator, decoder, xPos, yPos, radius);

        this.knownNeighbors = Collections.synchronizedSet(new HashSet<>());
        this.primes = new ArrayList<>();
        this.eratosthenesSieve();

        this.dutyCycle = dutyCycle;
//        this.status = Status.SLEEP;

        this.initialTime = System.currentTimeMillis();

        this.selectPrimes();
    }

    public double getDutyCycle(){
        return dutyCycle;
    }

    @Override
    public void sendMessage() {
        new Thread(() -> {
            while (workCondition()) {
                sendMessage(this.getGenerator().generate("HELLO", this));
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {
            this.setIsBusy(true);

            if(this.isAwakenTime()){
//                System.out.println("ID: " + this.getObjectID() + " awake time in sendMessage");
                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    if(knownNeighbors.contains(entry.getKey()))
                        continue;

                    writeToSocket(entry, outputMsg);
                }
            }
            this.setIsBusy(false);
        }
    }

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            this.setIsBusy(true);
            if (this.isAwakenTime(msg)) {
//                System.out.println("ID: " + this.getObjectID() + ", inputMsg: " + msg);
                if (this.shouldReceiveMessage(msg) && !this.knownNeighbors.contains(msg.getObjectID())) {
                    this.knownNeighbors.add(msg.getObjectID());
                    System.out.println("ID: " + this.getObjectID() + ", known: " + this.knownNeighbors);
                }
            }
            this.setIsBusy(false);
        }
    }

    // private methods

    private void eratosthenesSieve(){
        boolean[] mask = new boolean[DiscoNode.MAX];
        int sqrt = (int)(Math.sqrt(DiscoNode.MAX) + 1);

        for(int i = 2; i < sqrt; i++){
            if(!mask[i]){
                primes.add(i);
                for(int j = i * i; j < mask.length; j += i)
                    mask[j] = true;
            }
        }
        for(int i = sqrt + 1; i < mask.length; i++)
            if(!mask[i])
                primes.add(i);
    }

    private void selectPrimes() throws Exception {
        double eps = 1e-2;

        for(int i = 0; i < primes.size(); i++)
            for(int j = i + 1; j < primes.size(); j++)
                if(Math.abs(1.0/primes.get(i) + 1.0/primes.get(j) - dutyCycle) <= eps){
                    firstPrime = primes.get(i);
                    secondPrime = primes.get(j);
                    System.out.println("Combination: (" + firstPrime + ", " + secondPrime + ")");
                    return;
                }
        throw new Exception("Prime combination not found");
    }

    private double distFrom(double x, double y){
        double xDiff = this.getXPos() - x;
        double yDiff = this.getYPos() - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    private boolean isAwakenTime(){
        return isAwakenTime(null);
    }

    private boolean isAwakenTime(TMsg msg){
        long receivedTime = msg != null ? msg.getReceivedTime() : System.currentTimeMillis();
        long firstRem = ((receivedTime - this.initialTime)/ intervalLength ) % this.firstPrime;
        long secondRem = ((receivedTime - this.initialTime)/ intervalLength ) % this.secondPrime;

        return firstRem == 0 || secondRem == 0;
    }

    private boolean shouldReceiveMessage(TMsg msg){
        return this.distFrom(msg.getXPos(), msg.getYPos()) <= msg.getRadius();
    }

    private void writeToSocket(Map.Entry<Integer, Address> entry, TMsg outputMsg){
        Address address = entry.getValue();
        int objectID = entry.getKey();
        try{
            Socket output = this.getOutputChannel(address.getHost(), address.getPort());
            DataOutputStream out = new DataOutputStream(output.getOutputStream());
            out.writeUTF(outputMsg.encode());
            output.close();
        } catch(Exception e){
            this.getLog().print("Unable to connect to node: " + objectID);
        }
    }

}
