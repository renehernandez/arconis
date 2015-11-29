package arconis.delegates;

import arconis.Node;
import arconis.interfaces.Message;

public interface MessageGenerator<TMsg extends Message> {
    TMsg generate(String content, Node<TMsg> node);
}