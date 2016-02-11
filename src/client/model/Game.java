package client.model;

import client.World;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.model.Event;
import common.network.data.Message;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Model contains data which describes current state of the game.
 * You do not need to read this class, it's internal implementation.
 * See World interface for more information.
 * Do not change this class.
 */
public class Game implements World {
    private int totalTurns;
    private long turnTimeout;
    private int escape;
    private int nodeBonus;
    private int edgeBonus;
    private int firstlvl;
    private int secondlvl;
    private double lossRate1;
    private double lossRate2;
    private long turnStartTime;
    private Consumer<Message> sender;

    private int myID;
    private int turn;
    private Graph map;

    private Node[][] nodes = new Node[3][]; // free nodes, player1's nodes, player2's nodes

    private class NodeArrayList extends ArrayList<Node> {
    } // solving generic array creation!

    public Game(Consumer<Message> sender) {
        this.sender = sender;
    }

    public void handleInitMessage(Message msg) {
        JsonObject constants = msg.args.get(0).getAsJsonObject();
        totalTurns = constants.getAsJsonPrimitive("turns").getAsInt();
        turnTimeout = constants.getAsJsonPrimitive("turnTimeout").getAsInt();
        escape = constants.getAsJsonPrimitive("escape").getAsInt();
        nodeBonus = constants.getAsJsonPrimitive("nodeBonus").getAsInt();
        edgeBonus = constants.getAsJsonPrimitive("edgeBonus").getAsInt();
        firstlvl = constants.getAsJsonPrimitive("firstlvl").getAsInt();
        secondlvl = constants.getAsJsonPrimitive("secondlvl").getAsInt();
        lossRate1 = constants.getAsJsonPrimitive("lossRate1").getAsInt();
        lossRate2 = constants.getAsJsonPrimitive("lossRate2").getAsInt();

        myID = msg.args.get(1).getAsInt();

        // graph deserialization
        JsonArray adjListInt = msg.args.get(2).getAsJsonArray();

        Node[] nodes = new Node[adjListInt.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(i);
        }

        for (int i = 0; i < adjListInt.size(); i++) {
            JsonArray neighboursInt = adjListInt.get(i).getAsJsonArray();
            Node[] neighbours = new Node[adjListInt.get(i).getAsJsonArray().size()];
            for (int j = 0; j < neighbours.length; j++) {
                neighbours[j] = nodes[neighboursInt.get(j).getAsInt()];
            }
            nodes[i].setNeighbours(neighbours);
        }

        JsonArray graphDiff = msg.args.get(3).getAsJsonArray();
        for (int i = 0; i < graphDiff.size(); i++) {
            JsonArray nodeDiff = graphDiff.get(i).getAsJsonArray();
            int node = nodeDiff.get(0).getAsInt();
            int owner = nodeDiff.get(1).getAsInt();
            int armyCount = nodeDiff.get(2).getAsInt();
            nodes[node].setOwner(owner);
            nodes[node].setArmyCount(armyCount);
        }

        map = new Graph(nodes);

        updateNodesList();
    }

    public void handleTurnMessage(Message msg) {
        turnStartTime = System.currentTimeMillis();
        turn = msg.args.get(0).getAsInt();

        JsonArray graphDiff = msg.args.get(1).getAsJsonArray();
        for (int i = 0; i < graphDiff.size(); i++) {
            JsonArray nodeDiff = graphDiff.get(i).getAsJsonArray();
            int nodeIndex = nodeDiff.get(0).getAsInt();
            map.getNode(nodeIndex).setOwner(nodeDiff.get(1).getAsInt());
            map.getNode(nodeIndex).setArmyCount(nodeDiff.get(2).getAsInt());
        }

        updateNodesList();
    }

    private void updateNodesList() {
        NodeArrayList[] nodesList = new NodeArrayList[]{new NodeArrayList(), new NodeArrayList(), new NodeArrayList()};
        for (Node n : map.getNodes()) {
            nodesList[n.getOwner() + 1].add(n);
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = nodesList[i].toArray(new Node[nodesList[i].size()]);
        }
    }

    public long getTurnTimePassed() {
        return System.currentTimeMillis() - turnStartTime;
    }

    public long getTurnRemainingTime() {
        return turnTimeout - getTurnTimePassed();
    }

    @Override
    public int getMyID() {
        return myID;
    }

    @Override
    public Graph getMap() {
        return map;
    }

    @Override
    public Node[] getMyNodes() {
        return nodes[myID + 1];
    }

    @Override
    public Node[] getOpponentNodes() {
        return nodes[2 - myID];
    }

    @Override
    public Node[] getFreeNodes() {
        return nodes[0];
    }

    @Override
    public int getTotalTurns() {
        return totalTurns;
    }

    @Override
    public int getTurnNumber() {
        return turn;
    }

    @Override
    public long getTotalTurnTime() {
        return turnTimeout;
    }

    @Override
    public void moveArmy(Node src, Node dst, int count) {
        moveArmy(src.getIndex(), dst.getIndex(), count);
    }

    @Override
    public void moveArmy(int src, int dst, int count) {
        sender.accept(new Message(Event.EVENT, new Event("m", new Object[]{src, dst, count})));
    }

    @Override
    public int getEscapeConstant() {
        return escape;
    }

    @Override
    public int getNodeBonusConstant() {
        return nodeBonus;
    }

    @Override
    public int getEdgeBonusConstant() {
        return edgeBonus;
    }

    @Override
    public int getLowArmyBound() {
        return firstlvl;
    }

    @Override
    public int getMediumArmyBound() {
        return secondlvl;
    }

    @Override
    public double getMediumCasualtyCoefficient() {
        return lossRate1;
    }

    @Override
    public double getLowCasualtyCoefficient() {
        return lossRate2;
    }

}
