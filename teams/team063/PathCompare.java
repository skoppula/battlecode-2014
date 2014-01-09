package team063;

import java.util.Comparator;
import battlecode.common.*;
class PathCompare implements Comparator<searchNode>{
	MapLocation goal;
	public PathCompare(MapLocation goal){
		super();
		this.goal = goal;
	}
		
	public int compare(searchNode o1, searchNode o2){
		return (Integer)(o1.cost + o1.state.distanceSquaredTo(this.goal)) - (Integer)(o2.cost + o2.state.distanceSquaredTo(this.goal));
	}
}
