package arconis;

import java.io.IOException;
import java.util.*;

import arconis.benchmark.Benchmark;
import arconis.election.ControlledElectionNode;
import arconis.generators.ClassicNetworks;

public class Main {

    public static void main(String[] args) {

//        ArrayList<BroadcastNode<DefaultMessage>> network = ClassicNetworks.CompleteNetwork(
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
//        network.get(3).sendMessage();

//        ArrayList<BroadcastNode<DefaultMessage>> network1 = ClassicNetworks.StarNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                20
//        );
//
//        network1.get(5).sendMessage();

//        ArrayList<BroadcastNode<DefaultMessage>> network2 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                50
//        );
//
//        network2.get(10).sendMessage();

//
//        ArrayList<BroadcastWithNotificationNode<DefaultMessage>> network3 = ClassicNetworks.CompleteNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastWithNotificationNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                5
//        );
//
//        network3.get(3).sendMessage();

//        ArrayList<BroadcastWithNotificationNode<DefaultMessage>> network4 = ClassicNetworks.StarNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastWithNotificationNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                20
//        );
//
//        network4.get(5).sendMessage();

//        ArrayList<BroadcastWithNotificationNode<DefaultMessage>> network5 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new BroadcastWithNotificationNode<>(i, DefaultMessage::create, DefaultMessage::decode);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                50
//        );
//
//        network5.get(10).sendMessage();

        Benchmark.resetMeasures();
//        ArrayList<ControlledElectionNode<DefaultMessage>> ring = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                10
//        );
//        for(int i = 0; i < ring.size(); i++){
//            ring.get(i).setLeftNodeAddress(ring.get((ring.size() + i - 1) % ring.size()).getAddress());
//            ring.get(i).setRightNodeAddress(ring.get((ring.size() + i + 1) % ring.size()).getAddress());
//        }
//
//        ring.get(0).sendMessage();

//        ArrayList<ControlledElectionNode<DefaultMessage>> ring1 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                20
//        );
//        for(int i = 0; i < ring1.size(); i++){
//            ring1.get(i).setLeftNodeAddress(ring1.get((ring1.size() + i - 1) % ring1.size()).getAddress());
//            ring1.get(i).setRightNodeAddress(ring1.get((ring1.size() + i + 1) % ring1.size()).getAddress());
//        }
//
//        ring1.get(0).sendMessage();

//        ArrayList<ControlledElectionNode<DefaultMessage>> ring2 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                50
//        );
//        for(int i = 0; i < ring2.size(); i++){
//            ring2.get(i).setLeftNodeAddress(ring2.get((ring2.size() + i - 1) % ring2.size()).getAddress());
//            ring2.get(i).setRightNodeAddress(ring2.get((ring2.size() + i + 1) % ring2.size()).getAddress());
//        }
//
//        ring2.get(0).sendMessage();

//        ArrayList<ControlledElectionNode<DefaultMessage>> ring3 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                10
//        );
//        for(int i = 0; i < ring3.size(); i++){
//            ring3.get(i).setLeftNodeAddress(ring3.get((ring3.size() + i - 1) % ring3.size()).getAddress());
//            ring3.get(i).setRightNodeAddress(ring3.get((ring3.size() + i + 1) % ring3.size()).getAddress());
//        }
//
//        for(int i = 0; i < ring3.size(); i++)
//            ring3.get(i).sendMessage();

//        ArrayList<ControlledElectionNode<DefaultMessage>> ring4 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                20
//        );
//        for(int i = 0; i < ring4.size(); i++){
//            ring4.get(i).setLeftNodeAddress(ring4.get((ring4.size() + i - 1) % ring4.size()).getAddress());
//            ring4.get(i).setRightNodeAddress(ring4.get((ring4.size() + i + 1) % ring4.size()).getAddress());
//        }
//
//        for(int i = 0; i < ring4.size(); i++)
//            ring4.get(i).sendMessage();
//
//        ArrayList<ControlledElectionNode<DefaultMessage>> ring5 = ClassicNetworks.RingNetwork(
//                (i) -> {
//                    try {
//                        return new ControlledElectionNode<>(i, DefaultMessage::create, DefaultMessage::decode, i);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                },
//                50
//        );
//        for(int i = 0; i < ring5.size(); i++){
//            ring5.get(i).setLeftNodeAddress(ring5.get((ring5.size() + i - 1) % ring5.size()).getAddress());
//            ring5.get(i).setRightNodeAddress(ring5.get((ring5.size() + i + 1) % ring5.size()).getAddress());
//        }
//
//        for(int i = 0; i < ring5.size(); i++)
//            ring5.get(i).sendMessage();

    }

}