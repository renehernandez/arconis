package arconis;

import java.io.IOException;
import java.util.*;

import arconis.benchmark.*;
import arconis.discovery.*;
import arconis.generators.*;

public class Main {

    public static void main(String[] args) {

        double[][] position = new double[][] {
                {1, 2},
                {1, 1},
                {2, 2},
                {3, 2},
                {0, 0}
        };
        ArrayList<DiscoNode<DiscoveryMessage>> network = ClassicNetworks.CompleteNetwork(
                (i) -> {
                    try {
                        return new DiscoNode<>(
                                i, DiscoveryMessage::create, DiscoveryMessage::decode,
                                position[i][0], position[i][1], 1.5,0.1
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    return null;
                },
                5
        );
//        System.out.println(network.get(0).getNeighbors());

        for(int i = 0; i < network.size(); i++) {
//            System.out.println(network.get(3).getObjectID());
            network.get(i).sendMessage();
        }

    }

}