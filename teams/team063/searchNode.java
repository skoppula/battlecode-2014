package team063;

import java.util.ArrayList;
import java.util.Collections;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class searchNode{
	MapLocation state;
	searchNode parent;
	int cost;
	
	public searchNode(MapLocation state, searchNode parent){ //constructor that defaults cost to 0
		this.state = state;
		this.parent = parent;
		this.cost = 0;
	}

	public searchNode(MapLocation state, searchNode parent, int cost){ //constructor that takes cost
		this.state = state;
		this.parent = parent;
		this.cost = cost;
	}
	
	public static searchNode[] getChildren(searchNode s){
		ArrayList<searchNode> children = new ArrayList<searchNode>();
		Direction[] directions = new Direction[]{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		for(Direction direction: directions){
			MapLocation child = s.state.add(direction); //tries every cardinal direction
			if(child.x<RobotPlayer.terrainMap.length && child.x>0 && child.y<RobotPlayer.terrainMap[0].length && child.y>0){
				if(RobotPlayer.terrainMap[child.x][child.y] == 3  || RobotPlayer.terrainMap[child.x][child.y] == 10 ){ //tests if terrain on child space is ROAD or NORMAL
					searchNode addchild = new searchNode(child, s, s.cost + RobotPlayer.terrainMap[child.x][child.y]); //adds cost of traveling on ROAD (3) or NORMAL (10)
					children.add((searchNode)addchild);
				}
			}
		}
		searchNode[] res = (searchNode[]) children.toArray(new searchNode[children.size()]);
		return res;
	}	
	
	/*
	 * getPath() returns an array that describes the path from that node back to the start node backwards.
	 * 
	 */
	public MapLocation[] getPath(){
		ArrayList<MapLocation> path = new ArrayList<MapLocation>();
		path.add(this.state);
		searchNode s = this;
		while(s.parent != null){
			s = s.parent;
			path.add(s.state);
		}
		Collections.reverse(path);
		return (MapLocation[]) path.toArray(new MapLocation[path.size()]);
	}
}