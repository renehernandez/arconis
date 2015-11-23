package arconis;

public interface MessageDecoder<TMsg extends Message> {
    TMsg decode(String msgEncoded);
}