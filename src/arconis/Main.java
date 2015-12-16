package arconis;

import java.io.IOException;
import java.util.*;
import arconis.broadcast.*;
import arconis.generators.ClassicNetworks;

public class Main {

    public static void main(String[] args) {

//        ArrayList<BroadcastNode<DefaultMessage>> network = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                10
//        );
//
//        ArrayList<BroadcastWithNotificationNode<DefaultMessage>> network1 = ClassicNetworks.StarNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastWithNotificationNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                10
//        );
//
//        network1.get(0).sendMessage();
    }

}