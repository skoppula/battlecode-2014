package pheonix;

import battlecode.common.MapLocation;

public class Comm {
	
	public static int assignmentToInt(int squad, int role) {
		
		return 0;
	}
	
	public static int getType(int memory) {
		return memory/1000;
	}

	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
	public static int getSquad(int i) {
		return i/100;
	}

	public static int getRole(int i) {
		return i%100;
	}

	public static MapLocation getTargetLocation(int memory) {
		// TODO Auto-generated method stub
		// if location==9999
		//read channel associated with team, get location integer
		
		return new MapLocation(0, 0);
	}

}
