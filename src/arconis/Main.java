package arconis;

import java.io.IOException;
import java.util.*;
import arconis.broadcast.*;

public class Main {

    public static void main(String[] args) {

        ArrayList<BroadcastNode<DefaultMessage>> network = new ArrayList<>();

        for(int i = 0; i < 8; i++){
            try {
                BroadcastNode<DefaultMessage> node = new BroadcastNode<>(i,
                        DefaultMessage::create,
                        DefaultMessage::decode);
                network.add(node);
                node.start();
                System.out.println("Started node with objectID: " + i + " and port: " + node.getPort());
            } catch (IOException e) {
                System.out.println("Unable to create node with objectID: " + i);
            }
        }

        for(int i = 0; i < network.size(); i++){
            for(int j = 1; j <= 2; j++){
                network.get(i).addNeighbor(network.get((i + j) % network.size()));
                network.get((i + j) % network.size()).addNeighbor(network.get(i));
            }
        }

        network.get(5).sendMessage(new DefaultMessage("Hello", network.get(5).getObjectID()));
//		BroadcastNode<DefaultMessage> node = network.get(5);

//		HashSet<DefaultMessage> msgs = node.getReceivedMessages();
//		DefaultMessage a = new DefaultMessage("Hello", 5);
//		DefaultMessage a1 = new DefaultMessage("Hello", 6);
//		msgs.add(a);
//		if(!msgs.contains(a1)){
//			System.out.println("Failed test");
//		}
    }

}