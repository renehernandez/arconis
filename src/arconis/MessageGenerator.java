package arconis;

public interface MessageGenerator<TMsg extends Message> {
    TMsg generate(String content, Node<TMsg> node);
}