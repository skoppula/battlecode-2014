package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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

	//In team's channel: AABB|CCDD: (AA,BB) = attack target, (CC, DD) = location to move to
	
	public static MapLocation getTargetLocation(RobotController rc, int i) throws GameActionException {
		int j = rc.readBroadcast(i);
		return new MapLocation((j/100)%100, j%100);
	}
	
	public static MapLocation getAttackLocation(RobotController rc, int i) throws GameActionException {
		int j  = rc.readBroadcast(i);
		return new MapLocation(j/10000,(j/1000)%100);
	}

}
