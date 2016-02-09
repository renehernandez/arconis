package arconis.election;

import arconis.Address;
import arconis.MessageData;
import arconis.Node;
import arconis.UtilityData;
import arconis.delegates.*;
import arconis.interfaces.*;


import java.io.IOException;

/**
 * Created by aegis on 15/12/15.
 */
public abstract class RingNode<TMsg extends Message> extends Node<TMsg> {

    // Private Fields

    private Address leftAddress;
    private Address rightAddress;

    // Getters & Setters

    public Address getLeftNodeAddress(){
        return this.leftAddress;
    }

    public Address getRightNodeAddress(){
        return this.rightAddress;
    }

    public RingNode<TMsg> setLeftNodeAddress(Address address) {
        this.leftAddress = address;
        return this;
    }

    public RingNode<TMsg> setRightNodeAddress(Address address){
        this.rightAddress = address;
        return this;
    }

    // Constructors

    public RingNode(int objectID, MessageData<TMsg> msgData, UtilityData utils) throws IOException {
        super(objectID, msgData, utils);
    }

    public RingNode(int objectID, MessageData<TMsg> msgData) throws IOException {
        super(objectID, msgData);
    }

}