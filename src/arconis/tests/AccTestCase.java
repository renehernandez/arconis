package arconis.tests;

import arconis.MessageData;
import arconis.Node;
import arconis.discovery.*;
import arconis.generators.ClassicNetworks;
import arconis.interfaces.Message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by aegis on 10/04/16.
 */
public class AccTestCase extends TestCase {

    ArrayList<TestData> data;
    ArrayList<AccNode<AccMessage>> network;
    boolean[] mark;
    int finishedNodes, index;

    final Object lock = new Object();

    public AccTestCase(int numberOfTimes, String outputFilePath, ArrayList<TestData> data) {
        super(numberOfTimes, outputFilePath);

        this.data = data;
        this.finishedNodes = 0;
        this.index = 0;
    }

    @Override
    public void run() {
        network = ClassicNetworks.CompleteNetwork(
                (i) -> {
                    try {
                        if (i == 0) {
                            return new AccLeaderNode<>(
                                    i,
                                    new MessageData<>(AccMessage::create, AccMessage::decode),
                                    new PositionData(data.get(index).getPositions()[i][0],
                                            data.get(index).getPositions()[i][1],
                                            data.get(index).getRadius()),
                                    0.2
                            );
                        } else {
                            return new AccNode<>(
                                    i,
                                    new MessageData<>(AccMessage::create, AccMessage::decode),
                                    new PositionData(data.get(index).getPositions()[i][0],
                                            data.get(index).getPositions()[i][1],
                                            data.get(index).getRadius()),
                                    0.2
                            );
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    return null;
                },
                data.get(index).getNumberOfNodes()
        );

        mark = new boolean[data.get(index).getNumberOfNodes()];

        for (int i = 0; i < network.size(); i++) {
            network.get(i).addProcessedMessageListener(this::checkNode);
        }

        for (int i = 0; i < network.size(); i++) {
            network.get(i).addStopListener(this::writeResult);
            network.get(i).setInitialTime(System.currentTimeMillis());
            try {
                Thread.sleep(3000);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < network.size(); i++) {
            network.get(i).sendMessage();
        }
    }

    private <TMsg extends Message> void checkNode(Node<TMsg> node){
        synchronized (lock) {
            AccNode<AccMessage> acc = (AccNode<AccMessage>) node;

            int pos = 0;
            for (int i = 0; i < network.size(); i++)
                if (network.get(i).getObjectID() == acc.getObjectID()) {
                    pos = i;
                    break;
                }

            if (!mark[pos] && acc.getKnownNeighbors().size() == data.get(index).getRealNeighborsIndices().get(pos).size()) {
                this.finishedNodes++;
                mark[pos] = true;
            }

            if (finishedNodes == this.network.size()) {
                this.network.forEach(Node::stopNode);
            }
        }
    }


    private <TMsg extends Message> void writeResult(Node<TMsg> node){
        synchronized (lock) {
            AccNode<AccMessage> acc = (AccNode<AccMessage>) node;

            Path file = Paths.get(acc.getObjectID() + "_" + this.getOutputFileName());

            long period = acc.getIntervalCounter(acc.getLastReceivedTime());

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write("Known:" + acc.getKnownNeighbors() + ", Real:" + acc.getKnownNeighbors() + ", Period: " + period + ", WakeUp Times: " + acc.getWakeUpTimes());
            } catch (IOException e) {
                System.out.println("Error writing to file: " + file);
            }
        }
    }

}
