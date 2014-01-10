package team063;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Navigation {
	public static Direction direction(MapLocation a, MapLocation b){
		int dx = a.x - b.x;
		int dy = a.y - b.y;
		switch(dy){
			case 1:
				switch(dx){
					case 1:
						return Direction.NORTH_WEST;
					case 0:
						return Direction.NORTH;
					case -1:
						return Direction.NORTH_EAST;
				}
			case 0:
				switch(dx){
					case 1:
						return Direction.WEST;
					case 0:
						return Direction.OMNI;
					case -1:
						return Direction.EAST;
				}
			case -1:
				switch(dx){
					case 1:
						return Direction.SOUTH_WEST;
					case 0:
						return Direction.SOUTH;
					case -1:
						return Direction.SOUTH_EAST;
				}
		}
		return Direction.NORTH;
	}

	
	public static Direction[] directionsTo(MapLocation start, MapLocation goal){
		MapLocation[] nodes = locationsTo(start, goal);
		
		ArrayList<Direction> directions = new ArrayList<Direction>();
		for(int i = 0; i<nodes.length-1; i++){
			Direction direction = direction(nodes[i], nodes[i+1]);
			directions.add(direction);
		}
		return (Direction[]) directions.toArray(new Direction[directions.size()]);
	}
	
	/*
	 * @returns a list of MapLocations from a start location to a goal. Use this to find the directions that the robot has to go (eventually a "goto" method)
	 */
	public static MapLocation[] locationsTo(MapLocation start, MapLocation goal){ //basically implements a* search
		searchNode current = null;
		ArrayList<MapLocation> visited = new ArrayList<MapLocation>();
		searchNode startNode = new searchNode(start, null);
		if(start.equals(goal)){
			MapLocation[] res = new MapLocation[]{start};
			return res;
		}
		List<searchNode> agenda = new ArrayList<searchNode>();
		for(searchNode child:searchNode.getChildren(startNode)){
			if(child.state.equals(goal)){
				System.out.print("Coordinates x: " + child.state.x + " y: " + child.state.y);
				return (MapLocation[]) child.getPath();
			}
			else{
					agenda.add(child);
					visited.add(child.state);
				}
			}
			while(agenda.size() != 0){
				Collections.sort(agenda, new PathCompare(goal)); //ordering in increasing cost
				current = agenda.get(0);
				agenda.remove(0);
				//System.out.println("Expanding coordinates x: " + current.state.x + " y: " + current.state.y);
				for(searchNode child:searchNode.getChildren(current)){
					if(child.state.equals(goal)){
						return (MapLocation[]) child.getPath();
					}else{
						if(visited.contains(child.state)){
							continue;
						}
						else{
							agenda.add(child);
							visited.add(child.state);
						}
					}
				}
			}
		return null;	
		}
	
	public static void main(String[] args){
		RobotPlayer.terrainMap = new int[][]{{10,10,10,3,3,3,3,3,10,10,10,10,3,3,3,9999,3,3,3,3}, {10,10,10,3,3,3,3,3,10,10,10,10,3,3,3,9999,3,3,3,3},{10,10,10,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3},{10,10,3,3,3,3,3,3,10,10,10,10,3,3,3,3,3,3,3,3}};
		
//		MapLocation a = new MapLocation(0,0);
//		MapLocation b = new MapLocation(0,1);
//		Direction[] directions = directionsTo(a,b);
//		System.out.println("From a: ("+a.x+", "+a.y+") to b:("+b.x+", "+b.y+"): " + Arrays.toString(directions));
		
		for(int i = 0; i<10; i++){
			for(int j = 0; j<10; j++){
				for(int k = 0; k<10; k++){
					for(int l = 0; l<10; l++){
						MapLocation a = new MapLocation(i,j);
						MapLocation b = new MapLocation(k,l);
						Direction[] directions = directionsTo(a,b);
						System.out.println("From a: ("+a.x+", "+a.y+") to b:("+b.x+", "+b.y+"): " + Arrays.toString(directions));
					}
				}
			}
		}

		
//		ArrayList<searchNode> list = new ArrayList<searchNode>();
//		list.add(c);
//		list.add(d);
//		searchNode[] res = (searchNode[]) list.toArray(new searchNode[list.size()]);
//		System.out.print(res);

	}
}