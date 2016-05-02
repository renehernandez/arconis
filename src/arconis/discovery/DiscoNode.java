package arconis.discovery;

import arconis.*;
import arconis.tests.TestData;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class DiscoNode<TMsg extends DiscoveryMessage> extends PositionNode<TMsg> {

    static ArrayList<Integer> primes;

    private static void sieve() {
        boolean[] mask = new boolean[DiscoNode.MAX];
        int sqrt = (int) (Math.sqrt(DiscoNode.MAX) + 1);

        for (int i = 2; i < sqrt; i++) {
            if (!mask[i]) {
                primes.add(i);
                for (int j = i * i; j < mask.length; j += i)
                    mask[j] = true;
            }
        }
        for (int i = sqrt + 1; i < mask.length; i++)
            if (!mask[i])
                primes.add(i);
    }

    static {
        primes = new ArrayList<>();
        sieve();
    }

    // Private Fields

    static final long intervalLength = 100;
    final static int MAX = 100000;
    int firstPrime;
    int secondPrime;
    double dutyCycle;
    long initialTime;
    long lastReceivedTime;
    int numberOfWakeUp;
//    NetData netData;
    final Object lock = new Object();
    Set<Integer> knownNeighbors;

    // Getters & Setters

    public double getDutyCycle(){
        return this.dutyCycle;
    }

    public static long getIntervalLength() {
        return intervalLength;
    }

    public long getInitialTime(){
        return this.initialTime;
    }

    public void setInitialTime(long initialTime){
        this.initialTime = initialTime;
    }

    public long getLastReceivedTime() {
        return this.lastReceivedTime;
    }

    public int getNumberOfWakeUp(){
        return this.numberOfWakeUp;
    }

    public Set<Integer> getKnownNeighbors(){
        return this.knownNeighbors;
    }

    // Constructors

    public DiscoNode(int objectID, MessageData<TMsg> msgData, PositionData posData, double dutyCycle) throws Exception {
        super(objectID, msgData, posData);

        this.knownNeighbors = Collections.synchronizedSet(new HashSet<>());

        this.dutyCycle = dutyCycle;
        //this.initialTime = System.currentTimeMillis();
        this.initialTime =0;
//        this.selectPrimes();

        this.firstPrime = 3;
        this.secondPrime = 5;
    }

    // Public Methods

    @Override
    public void sendMessage() {
        new Thread(() -> {
            while (workCondition()) {
                sendMessage(this.getGenerator().generate("HELLO", this));
                /*try {
                    sleep(intervalLength - 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessage(this.getGenerator().generate("HELLO", this));*/
            }
        }).start();
    }

    @Override
    public void sendMessage(TMsg outputMsg) {
        synchronized(this.lock) {

            if(this.isAwakenTime()){
                for(Map.Entry<Integer, Address> entry : this.getNeighbors().entrySet()){
                    writeToSocket(entry, outputMsg);
                }
            }
        }
    }

    // Protected Methods

    @Override
    protected void processMessage(TMsg msg) {
        synchronized(this.lock) {
            if (!this.knownNeighbors.contains(msg.getObjectID())) {
                this.knownNeighbors.add(msg.getObjectID());
                System.out.println("ID: " + this.getObjectID() + ", known: " + this.knownNeighbors
                        + ", Time Period: " + (msg.getReceivedTime() - initialTime)/intervalLength);
                lastReceivedTime = msg.getReceivedTime();
                runProcessedMessageEvent();
            }
        }
    }

    @Override
    protected boolean canProcessMessage(TMsg msg){
        return isAwakenTime(msg) && shouldReceiveMessage(msg);
    }

    // Private Methods

    private void selectPrimes() throws Exception {
        double eps = 1e-2;

        ArrayList<Integer[]> combs = new ArrayList<>();

        for(int i = 0; i < primes.size(); i++)
            for(int j = i + 1; j < primes.size(); j++)
                if(Math.abs(1.0/primes.get(i) + 1.0/primes.get(j) - dutyCycle) <= eps){
                    combs.add(new Integer[]{ primes.get(i), primes.get(j)});
                }
        if(combs.size() == 0)
            throw new Exception("Prime combination not found");

        Random r = new Random();
        int pos = r.nextInt(combs.size());
        firstPrime = combs.get(pos)[0];
        secondPrime = combs.get(pos)[1];
        System.out.println("Combination: (" + firstPrime + ", " + secondPrime + ")");
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
       if (!(initialTime==0)) {


           long diff = receivedTime - initialTime < 0 ? 0 : receivedTime - initialTime;

           long firstRem = (diff / intervalLength) % firstPrime;
           long secondRem = (diff / intervalLength) % secondPrime;
           if (msg != null) {
               //System.out.println("ID: " + this.getObjectID() + "receive from: " + receivedTime
                       //+ ": " + msg.getObjectID() + ":" + initialTime + ":diff:" + diff + ":firstRem:" + firstRem + ":secondRem:" + secondRem);

               if (diff > intervalLength) {
                   return firstRem == 0 || secondRem == 0;
               } else {
                   return false;
               }
           } else{
               //System.out.println("Null ID: " + this.getObjectID() + "receive from: " + receivedTime+ ": "
                      //+ initialTime + ":firstRem:" + firstRem + ":secondRem:" + secondRem);
                return firstRem == 0 || secondRem == 0;
            }
        }
        else {
           return false;}
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
