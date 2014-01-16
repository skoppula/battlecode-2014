package pheonix;

import battlecode.common.MapLocation;

public class Comm {
	
	public static int assignmentToInt(int squad, int role) {
		
		return 0;
	}
	
	public static int getType(int memory) {
		return memory/1000;
	}

	public static int getTeam(int memory) {
		// TODO Auto-generated method stub
		return memory/10000;
	}

	public static int getRole(int memory) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static MapLocation getTargetLocation(int memory) {
		// TODO Auto-generated method stub
		// if location==9999
		//read channel associated with team, get location integer
		
		return new MapLocation(0, 0);
	}

}
