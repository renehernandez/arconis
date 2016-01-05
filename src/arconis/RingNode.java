package arconis;

import arconis.log.*;
import arconis.delegates.*;
import arconis.interfaces.*;


import java.io.IOException;

/**
 * Created by aegis on 15/12/15.
 */
public abstract class RingNode<TMsg extends Message> extends Node<TMsg> {

    private Address leftAddress;

    private Address rightAddress;

    public RingNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        super(objectID, generator, decoder);
    }

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

//
//    @Override
//    public Node<TMsg> addNeighbor(Node<TMsg> node){
//        if(this.neighborsCount == 0){
//            this.leftAddress = node.getAddress();
//            this.neighborsCount++;
//        }
//        else if(this.neighborsCount == 1){
//            this.rightAddress = node.getAddress();
//            this.neighborsCount++;
//        }
//        super.addNeighbor(node);
//
//        return this;
//    }

}