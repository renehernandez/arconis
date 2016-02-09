package arconis.discovery;

import arconis.*;
import arconis.benchmark.*;
import arconis.interfaces.Message;
import arconis.log.*;

import java.io.IOException;

/**
 * Created by aegis on 07/01/16.
 */
public abstract class PositionNode<TMsg extends Message> extends Node<TMsg> {

    // Private Fields

    PositionData posData;

    // Getters & Setters

    public double getXPos(){
        return this.posData.getXPos();
    }

    public double getYPos(){
        return this.posData.getYPos();
    }

    public double getRadius(){
        return this.posData.getRadius();
    }

    // Constructors

    public PositionNode(int objectID, MessageData<TMsg> msgData, UtilityData utils,
                        PositionData posData) throws IOException {
        super(objectID, msgData, utils);

        this.posData = posData;
    }

    public PositionNode(int objectID, MessageData<TMsg> msgData, PositionData posData) throws IOException {
        super(objectID, msgData);

        this.posData = posData;
    }

}
