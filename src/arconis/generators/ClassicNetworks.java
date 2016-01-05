package arconis.generators;

import arconis.*;
import arconis.delegates.NodeGenerator;
import arconis.interfaces.Message;

import java.util.ArrayList;

/**
 * Created by aegis on 29/11/15.
 */
public class ClassicNetworks {

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> StarNetwork(NodeGenerator<TNode, TMsg> generator, int n) {
        return BipartiteNetwork(generator, 1, n - 1);
    }

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> BipartiteNetwork(NodeGenerator<TNode, TMsg> generator, int n, int m){
        ArrayList<TNode> network = new ArrayList<>(n + m);

        for(int i = 0; i < n + m; i++){
            network.add(generator.generate(i));
            network.get(i).start();
        }

        for(int i = 0; i < n; i++)
            for(int j = 0; j < m; j++){
                network.get(i).addNeighbor(network.get(n + j));
                network.get(n + j).addNeighbor(network.get(i));
            }

        return network;
    }

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> CompleteNetwork(NodeGenerator<TNode, TMsg> generator, int n){
        ArrayList<TNode> network = new ArrayList<>(n);

        for(int i = 0; i < n; i++) {
            network.add(generator.generate(i));
            network.get(i).start();
            for (int j = i - 1; j >= 0; j--) {
                network.get(i).addNeighbor(network.get(j));
                network.get(j).addNeighbor(network.get(i));
            }
        }

        return network;
    }

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> PathNetwork(NodeGenerator<TNode, TMsg> generator, int n) {
        ArrayList<TNode> network = new ArrayList<>(n);
        network.add(generator.generate(0));
        network.get(0).start();

        for(int i = 1; i < n; i++){
            network.add(generator.generate(i));
            network.get(i).start();
            network.get(i - 1).addNeighbor(network.get(i));
            network.get(i).addNeighbor(network.get(i - 1));
        }

        return network;
    }

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> MeshNetwork(NodeGenerator<TNode, TMsg> generator, int n, int m) {
        ArrayList<TNode> network = new ArrayList<>(n * m);

        for(int i = 0; i < n * m; i++) {
            network.add(generator.generate(i));
            network.get(i).start();
        }

        for(int i = 0; i < n; i++)
            for(int j = 0; j < m; j++){
                if(j + 1 < m){
                    network.get(i*m + j).addNeighbor(network.get(i*m + j + 1));
                    network.get(i*m + j + 1).addNeighbor(network.get(i*m + j));
                }
                if(i + 1 < n){
                    network.get(i*m + j).addNeighbor(network.get((i + 1)*m + j));
                    network.get((i + 1)*m + j).addNeighbor(network.get(i*m + j));
                }
            }

        return network;
    }

    public static <TNode extends Node<TMsg>, TMsg extends Message> ArrayList<TNode> RingNetwork(NodeGenerator<TNode, TMsg> generator, int n) {
        ArrayList<TNode> network = new ArrayList<>(n);
        network.add(generator.generate(0));
        network.get(0).start();

        for(int i = 1; i < n; i++){
            network.add(generator.generate(i));
            network.get(i).start();
            network.get(i - 1).addNeighbor(network.get(i));
            network.get(i).addNeighbor(network.get(i - 1));
        }

        network.get(n - 1).addNeighbor(network.get(0));
        network.get(0).addNeighbor(network.get(n - 1));

        return network;
    }
}
