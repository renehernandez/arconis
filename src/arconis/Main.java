package arconis;

import java.io.IOException;
import java.util.*;
import arconis.broadcast.*;
import arconis.election.ControlledElectionNode;
import arconis.generators.ClassicNetworks;
import arconis.log.ConsoleLog;

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

        ArrayList<ControlledElectionNode<DefaultMessage>> ring = ClassicNetworks.RingNetwork(
                (i) -> {
                    try {
                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, new ConsoleLog(), i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                },
                20
        );

        for(int i = 0; i < ring.size(); i++){
            ring.get(i).setLeftNodeAddress(ring.get((ring.size() + i - 1) % ring.size()).getAddress());
            ring.get(i).setRightNodeAddress(ring.get((ring.size() + i + 1) % ring.size()).getAddress());
        }

        ring.get(10).sendMessage();
//        ring.get(18).sendMessage();
    }

}