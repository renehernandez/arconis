package arconis.delegates;

import arconis.Node;
import arconis.interfaces.Message;

/**
 * Created by aegis on 29/11/15.
 */
public interface NodeGenerator<TNode extends Node<TMsg>, TMsg extends Message> {
    TNode generate(int objectID);
}
