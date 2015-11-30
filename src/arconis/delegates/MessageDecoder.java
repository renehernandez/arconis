package arconis.delegates;

import arconis.interfaces.Message;

public interface MessageDecoder<TMsg extends Message> {
    TMsg decode(String msgEncoded);
}