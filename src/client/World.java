package client;

import client.model.Graph;
import client.model.Node;

/**
 * Game Interface
 * At each turn, you are given an instance of the World, and you can call any of
 * the following methods to get information from the game, or do action on the
 * game. Please read documentation of any method you have problem with that.
 * Please do not change this class.
 */
public interface World {
    /**
     * get ID of your team. it will be useful when you want to check if a node is yours or not.
     *
     * @return ID of your team
     */
    int getMyID();

    /**
     * get map of the game.
     *
     * @return a graph that represents the map of the game.
     */
    Graph getMap();

    /**
     * get nodes that you are owner of them.
     *
     * @return your nodes
     */
    Node[] getMyNodes();

    /**
     * get nodes that your opponent is owner of them.
     *
     * @return opponent's nodes
     */
    Node[] getOpponentNodes();

    /**
     * get nodes of the game that none of the two players are owner of them.
     *
     * @return free (without owner) nodes
     */
    Node[] getFreeNodes();

    /**
     * total (maximum) turns of the game.
     *
     * @return total turns
     */
    int getTotalTurns();

    /**
     * number of turns that passed as long as game started.
     *
     * @return turn number
     */
    int getTurnNumber();

    /**
     * get time limit of each turn.
     *
     * @return total turn time (ms)
     */
    long getTotalTurnTime();

    /**
     * get time passed from when the last turn is started.
     *
     * @return turn time passed (ms)
     */
    long getTurnTimePassed();

    /**
     * get the remaining time of the current turn.
     *
     * @return turn remaining time (ms)
     */
    long getTurnRemainingTime();

    /**
     * The move is the only action in this game. You can request a move by
     * calling this method and by passing with source, destination, and number
     * of armies you want to move from source to destination.
     *
     * @param src   source node
     * @param dst   destination node
     * @param count number of armies you want to move from source to destination
     */
    void moveArmy(Node src, Node dst, int count);

    /**
     * The move is the only action in this game. You can request a move by
     * calling this method and by passing with source, destination, and number
     * of armies you want to move from source to destination.
     *
     * @param src   source node's index, you can get index of a node by calling node.getIndex().
     * @param dst   destination node's index, you can get index of a node by calling node.getIndex().
     * @param count number of armies you want to move from source to destination.
     */
    void moveArmy(int src, int dst, int count);

    /**
     * Maximum number of armies that can escape from a battle.
     *
     * @return escape constant
     */
    int getEscapeConstant();

    /**
     * Amount of armies awarded for getting ownership of a node.
     *
     * @return node bonus constant
     */
    int getNodeBonusConstant();

    /**
     * Amount of armies added to a node for each adjacent node with the same ownership.
     *
     * @return edge bonus constant
     */
    int getEdgeBonusConstant();

    /**
     * Maximum amount of army that indicated as low.
     *
     * @return
     */
    int getLowArmyBound();

    /**
     * Maximum amount of army that indicated as medium.
     *
     * @return medium army bound
     */
    int getMediumArmyBound();

    /**
     * The winner of the battle losses some of his armies. The coefficient of loss
     * is a function of the army count of both sides of the battle (in terms of
     * low, medium, and high). When the level difference of the winner and loser is
     * at most 1 (e.g. winner is high and loser is medium, or winner is low and
     * medium is low), the loss coefficient can be determined by calling this function.
     *
     * @return loss coefficient
     */
    double getMediumCasualtyCoefficient();

    /**
     * The winner of the battle losses some of his armies. The coefficient of loss
     * is a function of the army count of both sides of the battle (in terms of
     * low, medium, and high). When the level difference of the winner and loser
     * is 2 (i.e. winner is high and loser is low), the loss coefficient can be
     * determined by calling this function.
     *
     * @return loss coefficient
     */
    double getLowCasualtyCoefficient();
}
