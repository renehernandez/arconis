package arconis.discovery;

import arconis.*;
import arconis.delegates.*;
import arconis.interfaces.*;

import java.io.*;
import java.util.*;

/**
 * Created by aegis on 02/12/15.
 */
public class DiscoNode<TMsg extends Message> extends Node<TMsg> {

    public enum Status {
        SLEEP,
        LISTEN,
        TRANSMITTING
    }

    ArrayList<Integer> primes;
    final static int MAX = 1 << 20;
    int firstPrime;
    int secondPrime;
    int posX, posY, radius, slotTime;
    double dutyCycle;
    Status status;


    public DiscoNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder, int posX, int posY, int radius, int slotTime, double dutyCycle) throws IOException {
        super(objectID, generator, decoder);

        this.primes = new ArrayList<>();
        this.eratosthenesSieve();
        Random rand = new Random();
        int half = this.primes.size()/2;
        this.firstPrime = this.primes.get(rand.nextInt(half));
        if(this.primes.size() % 2 == 1)
            half++;
        this.secondPrime = this.primes.get(half + rand.nextInt(half));
        this.posX = posX;
        this.posY = posY;
        this.radius = radius;
        this.slotTime = slotTime;
        this.dutyCycle = dutyCycle;
        this.status = Status.SLEEP;
    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void sendMessage(TMsg inputMsg) {

    }

    @Override
    protected void processMessage() {
    }

    // private methods

    private void eratosthenesSieve(){
        boolean[] mask = new boolean[DiscoNode.MAX];
        int sqrt = (int)(Math.sqrt(DiscoNode.MAX) + 1);

        for(int i = 2; i <= sqrt; i++){
            if(!mask[i]){
                primes.add(i);
                for(int j = i * i; j <= DiscoNode.MAX; j += i)
                    mask[j] = true;
            }
        }
    }

}
