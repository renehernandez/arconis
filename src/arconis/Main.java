package arconis;

import java.io.IOException;
import java.util.*;

import arconis.benchmark.*;
import arconis.discovery.*;
import arconis.generators.*;

public class Main {

    public static void main(String[] args) {
        double[][] positions = new double[][] {
                {1, 2},
                {1, 1},
                {2, 2},
                {3, 2},
                {0, 0}
        };
        NetData netData = new NetData(positions);

//        ArrayList<DiscoNode<DiscoveryMessage>> network = ClassicNetworks.CompleteNetwork(
//                (i) -> {
//                    try {
//                        return new DiscoNode<>(
//                                i, new MessageData<>(DiscoveryMessage::create, DiscoveryMessage::decode),
//                                netData,
//                                new PositionData(positions[i][0], positions[i][1], 1.5),
//                                0.2
//                        );
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch(Exception e){
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                5
//        );

        ArrayList<AccNode<AccMessage>> network = ClassicNetworks.CompleteNetwork(
                (i) -> {
                    try {
                        if (i==0){
                            return new AccLeaderNode<>(
                                    i, new MessageData<>(AccMessage::create, AccMessage::decode),
                                    new PositionData(positions[i][0], positions[i][1], 1.5)
                            );
                        }else{
                            return new AccNode<>(
                                    i, new MessageData<>(AccMessage::create, AccMessage::decode),
                                    new PositionData(positions[i][0], positions[i][1], 1.5)
                            );
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    return null;
                },
                5
        );


        for(int i = 0; i < network.size(); i++) {
//            System.out.println(network.get(3).getObjectID());
            network.get(i).sendMessage();
        }
    }

    public static double[][] GenerateTest(int pointsNumber, double xCenter, double yCenter, double radius){
        double[][] positions = new double[pointsNumber][2];
        Random r = new Random();

        int xSign, ySign;

        for(int i = 0; i < pointsNumber; i++){
            xSign = r.nextDouble() > 0.5 ? -1 : 1;
            ySign = r.nextDouble() > 0.5 ? -1 : 1;
            positions[i][0] = xSign * Math.abs(radius - xCenter) * r.nextDouble();
            positions[i][1] = ySign * Math.abs(radius - yCenter) * r.nextDouble();
        }

        return positions;
    }

}