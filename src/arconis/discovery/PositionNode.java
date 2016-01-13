package arconis.discovery;

import arconis.*;
import arconis.benchmark.*;
import arconis.delegates.*;
import arconis.interfaces.Message;
import arconis.log.*;

import java.io.IOException;

/**
 * Created by aegis on 07/01/16.
 */
public abstract class PositionNode<TMsg extends Message> extends Node<TMsg> {

    double xPos, yPos, radius;


    public PositionNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder, Log log, Benchmark benchmark) throws IOException {
        super(objectID, generator, decoder, log, benchmark);
    }

    public PositionNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder,
                        double xPos, double yPos, double radius) throws IOException {
        super(objectID, generator, decoder, new ConsoleLog(), new Benchmark());

        this.xPos = xPos;
        this.yPos = yPos;
        this.radius = radius;
    }

    public double getXPos(){
        return this.xPos;
    }

    public double getYPos(){
        return this.yPos;
    }

    public double getRadius(){
        return this.radius;
    }
}
