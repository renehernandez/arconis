package arconis.broadcast;

import arconis.Node;
import arconis.delegates.*;
import arconis.interfaces.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by aegis on 29/11/15.
 */
public class BroadcastWithNotificationNode<TMsg extends Message> extends Node<TMsg> {

    final Object lock = new Object();
    HashSet<String> receivedMessages;

    public BroadcastWithNotificationNode(int objectID, MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder) throws IOException {
        super(objectID, generator, decoder);

        this.receivedMessages = new HashSet<>();
    }

    @Override
    public void sendMessage(TMsg tMsg) {
        
    }

    @Override
    protected void processMessage() {
    }

    public enum State{
        INITIATOR,
        SLEEPING,
        PROCESSING,
        DONE
    }
}
