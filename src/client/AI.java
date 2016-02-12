package client;
import client.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class BFS_NODE{
	int distance, parent; 
	public BFS_NODE(){
		distance = Integer.MAX_VALUE;
	}
}




public class AI {
// -------------------------------- GlOBAL VARIABLES HERE
	World my_world; // Local World
	
// -------------------------------------------------------	
	
	boolean BFS(Node start, Node dest, ArrayList<Node> path){
		// Note: START is not included in the path
		
		// initialize: 
		Node[] AllNodes = my_world.getMap().getNodes();
		int NSize = AllNodes.length;
		Queue<Integer> q = new LinkedList<Integer>();
		
		BFS_NODE[] nodes = new BFS_NODE[ NSize ];		
		for(int i=0; i<NSize; i++)
			nodes [i] = new BFS_NODE();
		
		// add root to the queue:
		nodes[ dest.getIndex() ].distance = 0;
		nodes[ dest.getIndex() ].parent = dest.getIndex();
		q.add( dest.getIndex() );
		
		// bfs-core:
		Node from;
		boolean found = false;
		
		while (!q.isEmpty()){
			int frontier = q.poll();
			from = AllNodes[ frontier ];
			
			Node[] neighbours = from.getNeighbours();			
			for( Node to:neighbours ){
				// check if should not pass in some conditions <!!!!!!!>
				if( nodes[to.getIndex()].distance != Integer.MAX_VALUE )	continue;
				if( start.getIndex() == to.getIndex() )						found = true;
				
				nodes[to.getIndex()].distance = nodes[from.getIndex()].distance + 1;
				nodes[to.getIndex()].parent   = from.getIndex();
				q.add(to.getIndex());
			}			
			if(found == true)	break;
		}		
		if(found == false)		return false;
		
		from = start;
		do{
			path.add( AllNodes[ nodes[ from.getIndex() ].parent ]);
			from = path.get( path.size()-1 );
		}while( dest.getIndex() != from.getIndex() );		
		return true;
	}
	
	
	public void doTurn(World world) {    	
	try{
		my_world = world;
	
        Node[] myNodes = world.getMyNodes();
        Node dest = world.getMap().getNode(13);
        
        for (Node source : myNodes) {
        	ArrayList< Node > path = new ArrayList< Node >();
        	if( BFS(source,dest, path) == true ){
        		world.moveArmy(source, path.get(0), source.getArmyCount()/2);
        	}
        	/*
            Node[] neighbours = source.getNeighbours();
            if (neighbours.length > 0) {
                // select a random neighbour
                Node destination = neighbours[(int) (neighbours.length * Math.random())];
                // move half of the node's army to the neighbor node
                world.moveArmy(source, destination, source.getArmyCount()/2);
            }
            */
        }
    		
	}catch(Exception e){}
	}

}
