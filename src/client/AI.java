package client;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import client.model.Node;

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
	int min_distance_list (Node from, ArrayList<Node> list){
		int MIN = this.MY_MAX;
		for( Node e:list )
			if( D[from.getIndex()][e.getIndex()] < MIN )
				MIN = D[from.getIndex()][e.getIndex()];
		return MIN;
	}
	int avg_distance_list (Node from, ArrayList<Node> list){
		int AVG = 0;
		int cnt = 0;
		for( Node e:list )
			if(D[from.getIndex()][e.getIndex()] != this.MY_MAX){
				cnt++;
				AVG += D[from.getIndex()][e.getIndex()];
			}
		return AVG/cnt;
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
	public Node node; // contains index
	public int type; // 0=Resource, 1=FrontLine, 2=Free, 3=Enemy
	public ArrayList<Integer> q; // For resource management reserved
	public boolean underattack;
	
	NODE_LIST(){
		q = new ArrayList<Integer>();
		underattack = false;
	}
	int circulate_queue_poll(){
		int a = -1;
		if( q.size() > 0 ){
			a = q.get(0);
			q.remove(0);
			q.add(a);
		}
		return a; 
	}
	void my_merge(ArrayList<Integer> newF){
		ArrayList<Integer> newQ = new ArrayList<Integer>();
		for(int i=0; i<q.size(); i++)
			if(newF.contains(q.get(i)))
				newQ.add(q.get(i));
		for(Integer e:newF)
			if( !newQ.contains(e))
				newQ.add(e);
		this.q = newQ;
	}
}


public class AI {
// -------------------------------- OUR CONSTANTS
	int DEFAULT_FRONT_MIN = 5;
	int DEFAULT_RESOURCE_MIN = 1;
	int CLOSE_ENEMY_FL_RM_MIN = 15; // How close is FL to the enemy (MIN)
	int CLOSE_ENEMY_FL_RM_AVG = 2; 	// How close is FL to the enemy (AVG)
	int EMPTINESS_FL_RM  = 3; 		// How empty is the FL
	int RESOURCE_THRESHOLD = 1;

	//-------------------- rating attacking nodes
	int VERTEX_DEGREE = 1;	
	int ENEMY_NEIGHBOUR = 6;
	int ENEMY_POWER = 5;	// How is the difference between power of us and destination enemy
	int UNDER_ATTACK = 2;	//	destination is under attack by another monster
	int DISTANCE_TO_OUR_UNIT = 1; // nearness to our units
	int MAX_DISTANCE_TO_FRIEND = 7; // maximum distance to our friends
	double ENEMY_REMAIN = 0.1;	// point based on number of enemy that cannot flee
	double ENEMY_EXISTENCE = 0.02;
	double REMAIN_UNITS = 0.2;	// remain units
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
	
	void get_nodes_index_by_type(int _type, ArrayList<Integer> nodes_index){	// don't run this function before "update_node_list" 		
		for(int i=0; i<NodeList.length; i++)
			if( NodeList[i].type == _type)
				nodes_index.add(i);
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
			if(NodeList[i].type == 1)
				FNodes.add(NodeList[i].node);
			else if(NodeList[i].type == 0)
				RNodes.add(NodeList[i].node);
		}

		for(Node r:RNodes){
			int minF = warshall.min_distance_list(r, FNodes);
			minF = Math.max(minF, RESOURCE_THRESHOLD);
			
			ArrayList<Integer> newQ = new ArrayList<Integer>();
			for(Node f:FNodes)
				if(warshall.D[r.getIndex()][f.getIndex()] <= minF)
					newQ.add(f.getIndex());
			NodeList[r.getIndex()].my_merge(newQ);
		}
	}
	
	boolean is_FL_on_Free_Path (Node fl){
		if( count_options_for_FL(fl) <= 1 ){
			Node[] NGH = fl.getNeighbours();
			for(Node ngh: NGH)
				if( ngh.getOwner() <0 && is_path_free(ngh, my_world.getMyID()) )
					return true;
		}		
		return false;
	}
	
	int FLine_Priority_for_RM(int index, ArrayList<Node> enemy){
		int point = 500;
		Node fl = NodeList[index].node;
		
		if( is_FL_on_Free_Path(fl) )
			point -= 1000;
		
		int avg_dist = warshall.avg_distance_list(fl, enemy);
		int min_dist = warshall.min_distance_list(fl, enemy);
		int req = fl.getArmyCount();
		
		point -= avg_dist * CLOSE_ENEMY_FL_RM_AVG;
		point -= min_dist * CLOSE_ENEMY_FL_RM_MIN;
		point -= req * EMPTINESS_FL_RM; 
		
		return point;
	}
	
	int count_options_for_FL(Node fl){
		Node[] NGH = fl.getNeighbours();
		int cnt = 0;
		for( Node ngh:NGH )
			if( NodeList[ngh.getIndex()].type > 1)
				cnt ++;
		return cnt;
	}
	boolean has_aliance_neighbour( Node sample ){
		Node[] NGH = sample.getNeighbours();
		for(Node ngh: NGH)
			if( ngh.getOwner() == my_world.getMyID() )
				return true;
		return false;
	}
	void sort_by_army_count (ArrayList<Node> list, boolean ascending){//ascending
		int n = list.size();
		boolean flag = true;
		while(flag == true){
			flag = false;
			for(int i=0; i<n-1; i++)
				if( (list.get(i).getArmyCount() < list.get(i+1).getArmyCount()) != ascending){
					Collections.swap(list, i, i+1);
					flag = true;
				}
		}
	}
	int max_min_index(int[] arr, int max){
		int M = arr[0];
		int M_index = 0;
		for(int i=1; i<arr.length; i++)
			if( (max==1 && arr[i] > M) || (max==0 && arr[i] < M) ){		
				M = arr[i];
				M_index = i;
			}
		return M_index;
	}
	int estimate_enemy_count(Node nd){
		int i = nd.getIndex();
		if( NodeList[i].node.getArmyCount() == 0 )
			return my_world.getLowArmyBound();
		else if( NodeList[i].node.getArmyCount() == 1 )
			return my_world.getMediumArmyBound();
		else
			return 100;
	}
	
	boolean is_path_free (Node in, int BlockType){
		boolean [] mark = new boolean[this.size];
		mark[ in.getIndex() ] = true;
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(in.getIndex());
		int head;
			
		if( in.getOwner() >=0 && in.getOwner() != BlockType)
			return false;
		
		while( q.size() > 0 ){
			head = q.poll();
			
			Node[] NGH = NodeList[head].node.getNeighbours();
			for( Node ngh: NGH ){
				if( NodeList[ngh.getIndex()].node.getOwner() == BlockType )
					continue;
				if( NodeList[ngh.getIndex()].node.getOwner() >=0)
					return false;
				else if( !mark[ngh.getIndex()] ){
					mark[ngh.getIndex()] = true;
					q.add(ngh.getIndex());
				}
			}
		}		
		return true;
	}
	
	void Resource_Manager(){
		ArrayList<Integer> rlist = new ArrayList<Integer>();
		get_nodes_index_by_type(0, rlist);
		if(rlist.size() <= 0)
			return;

		ArrayList<Integer> enemy_ix = new ArrayList<Integer>();
		get_nodes_index_by_type(3, enemy_ix);
		ArrayList<Node> enemy = new ArrayList<Node>();
		for(Integer e:enemy_ix)
			enemy.add(NodeList[e].node);		
		
		
		// Send Army: 
		for(Integer r:rlist){
			int D2F = warshall.D[ NodeList[r].q.get(0) ][r];
			if( D2F <= RESOURCE_THRESHOLD ){
				int [] Priority = new int[ NodeList[r].q.size() ];
				for(int k=0; k<Priority.length; k++)
					Priority[k] = FLine_Priority_for_RM( NodeList[r].q.get(k), enemy );
								
				int max_idx = max_min_index(Priority, 1);//max
				int dest = NodeList[r].q.get(max_idx);
				int count_value = NodeList[r].node.getArmyCount();
				my_world.moveArmy(r, warshall.next_hop(NodeList[r].node, NodeList[dest].node), count_value);				
			}else{				
				int count_value = NodeList[r].node.getArmyCount();
				int dest = NodeList[r].circulate_queue_poll();
				if( is_FL_on_Free_Path(NodeList[dest].node) )
					dest = NodeList[r].circulate_queue_poll();
				my_world.moveArmy(r, warshall.next_hop(NodeList[r].node, NodeList[dest].node), count_value);
			}
		}
	}
	
	int Get_Distance_To_Friend(Node src, Node in){
		if(in.getOwner() == my_world.getMyID())
			return 0;
		
		int min_dist = warshall.MY_MAX;
		Node[] _mynodes = my_world.getMyNodes();
		for( int i=0; i<_mynodes.length; i++ )
			if( warshall.D[in.getIndex()][_mynodes[i].getIndex()] < min_dist )
				if( _mynodes[i].getIndex() != src.getIndex() )
					min_dist = warshall.D[in.getIndex()][_mynodes[i].getIndex()];
		return min_dist;			
	}

	int GetScore(Node src, Node des){
		int _score = 0;
		Node[] neighbours = des.getNeighbours();
		Node[] src_neighbours = src.getNeighbours();
		_score += neighbours.length * VERTEX_DEGREE;	// Degree point
				
		//------------------------- priority base on our power and enemy power
		int my_power;
		if( src.getArmyCount() <= 10)
			my_power = 0;
		else if ( src.getArmyCount() <= 30)
			my_power = 1;
		else
			my_power = 2;
		
		if( des.getOwner() != my_world.getMyID() && des.getOwner() >= 0)			
			_score += (my_power - des.getArmyCount() + 1) *  ENEMY_POWER;
		
		//----------------------- distance to our units priority		
		boolean is_friend = false;
		for(Node tmp:src_neighbours){
			if (NodeList[tmp.getIndex()].type <= 1)
				is_friend = true;
		}
		if(!is_friend){
			if(MAX_DISTANCE_TO_FRIEND > Get_Distance_To_Friend(src, des) )
				_score += (MAX_DISTANCE_TO_FRIEND - Get_Distance_To_Friend(src, des)) * DISTANCE_TO_OUR_UNIT;
		}
				
		//-------------------- number of enemy who can flee		
		if(NodeList[des.getIndex()].type == 3){
			int enemy_counter = 0;
			for(Node tmp: neighbours)
				if(tmp.getOwner()>=0 && tmp.getOwner() != my_world.getMyID())
					enemy_counter++;
					
			if( src.getArmyCount() > estimate_enemy_count(des))
				_score += (estimate_enemy_count(des) - enemy_counter * my_world.getEscapeConstant()) * ENEMY_REMAIN;  
		}
		//-------------------- under attack priority
		if( NodeList[des.getIndex()].underattack)
			_score -= UNDER_ATTACK;
		
		return _score;
	}	
	
	void Frontier_Manager(){
		ArrayList<Integer> frontiers = new ArrayList<Integer>();
		get_nodes_index_by_type(1, frontiers);
		for(Integer i:frontiers){		
			Node source = NodeList[i].node;
            Node[] neighbours = source.getNeighbours();
            Node final_des = null;
            int max = 0;
            int has_enemy = 0;
            //----------------------- Get Score of each node
            for(Node des:neighbours){
            	if(NodeList[des.getIndex()].type > 1){
            		if ( max < GetScore(source, des)){
            			max = GetScore(source, des);
            			final_des = des;
            		}
            	}
            	if(NodeList[des.getIndex()].type == 3)
            		has_enemy++;
            }
            
            if( final_des == null)	// no appropriate destination found
            	continue;
            
            int armycount = 0;
            //-------------------- decrease movement power to free path
            if( is_path_free(final_des, my_world.getMyID()) )
            	armycount = 1;
            else{
            	armycount = source.getArmyCount();
            	//----- decrease army count based on enemy count
            	armycount -= has_enemy *  armycount * ENEMY_EXISTENCE;
            	
            	//------ decrease army count based on our power 
            	if( armycount > 50)
            		armycount -= armycount * REMAIN_UNITS;
            }            
            
            my_world.moveArmy(source, final_des, armycount);
            NodeList[final_des.getIndex()].underattack = true;
		}
	}
	
	public void doTurn(World world) {    	
	try{
		my_world = world;
		if( world.getTurnNumber() <= 0 )
			initialize(); // Initialize Global Variables, run one time		
		update_node_list(); // Run each cycle

        Resource_Manager();
        Frontier_Manager();
        System.out.println(world.getMyNodes().length);
        
	}catch(Exception e){}
	}
}
