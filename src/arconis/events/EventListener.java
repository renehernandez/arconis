package arconis.events;

import arconis.Node;
import arconis.discovery.DiscoNode;
import arconis.discovery.DiscoveryMessage;
import arconis.interfaces.Message;

/**
 * Created by aegis on 10/04/16.
 */
public interface EventListener<TNode extends Node<TMsg>, TMsg extends Message> {

    void respondTo(TNode node);
}
