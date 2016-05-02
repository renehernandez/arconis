package arconis.tests;

import arconis.*;
import arconis.discovery.*;
import arconis.generators.*;
import arconis.interfaces.Message;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

/**
 * Created by aegis on 10/04/16.
 */
public class DiscoTestCase extends TestCase {

    ArrayList<TestData> data;
    ArrayList<DiscoNode<DiscoveryMessage>> network;
    int finishedNodes, index;

    final Object lock = new Object();

    public DiscoTestCase(int numberOfTimes, String outputFilePath, ArrayList<TestData> data){
        super(numberOfTimes, outputFilePath);

        this.data = data;
        this.finishedNodes = 0;
        this.index = 0;
    }

    @Override
    public void run() {
//        for(index = 0; index < data.getFirst().length; index++) {

            this.network = ClassicNetworks.CompleteNetwork(
                    (i) -> {
                        try {
                            return new DiscoNode<>(
                                    i,
                                    new MessageData<>(DiscoveryMessage::create, DiscoveryMessage::decode),
                                    new PositionData(data.get(index).getPositions()[i][0],
                                            data.get(index).getPositions()[i][1],
                                            data.get(index).getRadius()),
                                    0.2
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    },
                    data.get(index).getNumberOfNodes()
            );

            for (int i = 0; i < network.size(); i++) {
                network.get(i).addProcessedMessageListener(this::checkNode);
            }

            for (int i = 0; i < network.size(); i++) {
                network.get(i).addStopListener(this::writeResult);
            }

            for (int i = 0; i < network.size(); i++) {
                network.get(i).sendMessage();
            }
//        }
    }

    private <TMsg extends Message> void checkNode(Node<TMsg> node){
        synchronized (lock) {
            DiscoNode<DiscoveryMessage> disco = (DiscoNode<DiscoveryMessage>) node;

            int pos = 0;
            for (int i = 0; i < network.size(); i++)
                if (network.get(i).getObjectID() == disco.getObjectID()) {
                    pos = i;
                    break;
                }

            if (disco.getKnownNeighbors().size() == data.get(index).getRealNeighborsIndices().get(pos).size()) {
                this.finishedNodes++;
            }

            if (finishedNodes == this.network.size()) {
                this.network.forEach(Node::stopNode);
            }
        }
    }


    private <TMsg extends Message> void writeResult(Node<TMsg> node){
        synchronized (lock) {
            DiscoNode<DiscoveryMessage> disco = (DiscoNode<DiscoveryMessage>) node;

            Path file = Paths.get(disco.getObjectID() + "_" + this.getOutputFileName());

            long period = (disco.getLastReceivedTime() - disco.getInitialTime()) / disco.getIntervalLength();

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write("Known:" + disco.getKnownNeighbors() + ", Period: " + period);
            } catch (IOException e) {
                System.out.println("Error writing to file: " + file);
            }
        }
    }
}
