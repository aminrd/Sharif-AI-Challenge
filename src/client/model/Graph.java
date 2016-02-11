package client.model;

/**
 * Graph class.
 * Map of the game is graph consisting of Nodes.
 * Please do not change this class.
 */
public class Graph {
    private Node[] nodes;

    Graph(Node[] nodes) {
        this.nodes = nodes;
    }

    /**
     * get all nodes of the map.
     *
     * @return array of nodes
     */
    public Node[] getNodes() {
        return this.nodes;
    }

    /**
     * get node by its index.
     *
     * @param index index of the node
     * @return node at specified index
     */
    public Node getNode(int index) {
        return nodes[index];
    }
}
