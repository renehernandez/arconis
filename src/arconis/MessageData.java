package arconis;

import arconis.delegates.*;
import arconis.interfaces.*;

/**
 * Created by aegis on 09/02/16.
 */
public class MessageData<TMsg extends Message> {

    // Private Fields

    MessageGenerator<TMsg> generator;
    MessageDecoder<TMsg> decoder;

    // Getters & Setters

    public MessageGenerator<TMsg> getGenerator(){
        return this.generator;
    }

    public MessageDecoder<TMsg> getDecoder(){
        return this.decoder;
    }

    // Constructors

    public MessageData(MessageGenerator<TMsg> generator, MessageDecoder<TMsg> decoder){
        this.generator = generator;
        this.decoder = decoder;
    }

}
