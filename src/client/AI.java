package client;
import client.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

class WARSHALL{
	int size, MY_MAX;
	Node[] NODES;
	public int [][] graph;
	public int [][] D, P;
	
	/*====================================================================*/
	WARSHALL(World _world){ // O(n^3)				
		NODES = _world.getMap().getNodes();
		this.size = NODES.length;
		this.MY_MAX = size + 100;
		
		this.graph  = new int[size][size];
		this.D 	= new int[size][size];
		this.P 	= new int[size][size];
		
		for(int i=0; i<size; i++){
			graph[i][i] = 1;
			Node[] NGH = NODES[i].getNeighbours();			
			for(Node ngh:NGH)
				graph[i][ngh.getIndex()] = 1;
		}
		for(int i=0; i<size; i++)
			for(int j=0; j<size; j++)
				if( i==j )
					P[i][i] = MY_MAX;
				else if (graph[i][j] == 1){
					D[i][j] = 1; // Or other distance value 
					P[i][j] = j;					
				}else
					D[i][j] = P[i][j] = MY_MAX;
	
		for(int k=0; k<size; k++)
			for(int i=0; i<size; i++)
				for(int j=0; j<size; j++)
					if( D[i][k]+D[k][j] < D[i][j] ){
						D[i][j] = D[i][k]+D[k][j];
						P[i][j] = P[i][k];
					}
	}
	boolean path_exist(Node start, Node dest){
		if(D[start.getIndex()][dest.getIndex()] == MY_MAX)
			return false;
		return true;
	}
	boolean short_path(Node start, Node dest, ArrayList<Node> path){ // O(n)
		// Note: START is not included in the path
		int s = start.getIndex();
		int d = dest.getIndex();
		if(!path_exist(start, dest))	return false;
		while(s != d){
			s = P[s][d];
			path.add( NODES[s] );
		}				
		return true;
	}
	int next_hop(Node start, Node dest){
		if(!path_exist(start, dest))	return -1;
		return 
				this.P[start.getIndex()][dest.getIndex()];
	}
}

class BFS_NODE{
	int distance, parent; 
	public BFS_NODE(){
		distance = Integer.MAX_VALUE;
	}
}


class NODE_LIST{
	Node node; // contains index
	public int type; // 0=Resource, 1=FrontLine, 2=Free, 3=Enemy
	public int min_value;
	public Queue<Integer> q; // For resource management reserved
	
	NODE_LIST(){
		this.min_value = 1;
		q = new LinkedList<Integer>();
	}
	int circulate_queue_poll(){
		int a = -1;
		if( q.size() > 0 ){
			a = q.poll();
			q.add(a);
		}
		return a; 
	}
	void my_merge(ArrayList<Integer> newF){
		Queue<Integer> newQ = new LinkedList<Integer>();
		int p;
		while( q.size() > 0 ){
			p = q.poll();
			if( newF.contains(p) )
				newQ.add(p);
		}
		for(Integer e:newF)
			if( !newQ.contains(e))
				newQ.add(e);
		q = newQ;
		while( q.size() > 0 ){
			p = q.poll();
			System.out.print(p + " ");
		}
		System.out.println();
	}
}


public class AI {
// -------------------------------- OUR CONSTANTS
	int DEFAULT_FRONT_MIN = 5;
	int DEFAULT_RESOURCE_MIN = 1;
// -------------------------------- GlOBAL VARIABLES HERE
	World my_world; // Local World	
	WARSHALL warshall;
	NODE_LIST [] NodeList;
	int size; // or N = number of nodes
// -------------------------------------------------------	
	void initialize(){
		// Run one time, initialize Global Variables here
		this.warshall = new WARSHALL(this.my_world);
		size = my_world.getMap().getNodes().length;
		NodeList = new NODE_LIST[size];
		for(int i=0; i<size; i++)
			NodeList[i]= new NODE_LIST();
	}
	
	boolean BFS(Node start, Node dest, ArrayList<Node> path){// O(V.E) or O(n^2)
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
	
	void update_node_list(){
		ArrayList<Node> FNodes = new ArrayList<Node>();
		ArrayList<Node> RNodes = new ArrayList<Node>();
		
		Node[] allNodes = my_world.getMap().getNodes();
		for(int j=0; j<allNodes.length; j++){
			int i = allNodes[j].getIndex();
			NodeList[i].node = my_world.getMap().getNode(i);
			
			if( allNodes[j].getOwner() < 0 )
				NodeList[i].type = 2; // FREE
			else if( allNodes[j].getOwner() != my_world.getMyID() )
				NodeList[i].type = 3; // ENEMY
			else{
				boolean isResource = true;
				Node[] NGH = allNodes[j].getNeighbours();
				for( Node ngh:NGH )
					if( ngh.getOwner() != my_world.getMyID() ){			
						isResource = false;
						break;
					}
				if( isResource == true)
					NodeList[i].type = 0; // RESOURCE
				else
					NodeList[i].type = 1; // FRONT LINE
			}
			if(NodeList[i].type == 1){
				NodeList[i].min_value = Math.max(NodeList[i].min_value, DEFAULT_FRONT_MIN);
				FNodes.add(NodeList[i].node);
			}else if(NodeList[i].type == 0){
				NodeList[i].min_value = Math.max(NodeList[i].min_value, DEFAULT_RESOURCE_MIN);
				RNodes.add(NodeList[i].node);
			}
		}
	}
	
	public void doTurn(World world) {    	
	try{
		my_world = world;
		if( world.getTurnNumber() <= 0 )
			initialize(); // Initialize Global Variables, run one time		
		update_node_list(); // Run each cycle
		
		
        Node[] myNodes = world.getMyNodes();
        Node dest = world.getMap().getNode(13);
        
        for (Node source : myNodes) {
        	ArrayList< Node > path = new ArrayList< Node >();
        	//if( BFS(source,dest, path) == true ){
        	if( warshall.short_path(source, dest, path) == true ){
        		world.moveArmy(source, path.get(0), source.getArmyCount() - 1);
        	}
        	/**
            Node[] neighbours = source.getNeighbours();
            if (neighbours.length > 0) {
                // select a random neighbour
                Node destination = neighbours[(int) (neighbours.length * Math.random())];
                // move half of the node's army to the neighbor node
                world.moveArmy(source, destination, source.getArmyCount()/2);
            }
            /**/
        }
	}catch(Exception e){}
	}
}
