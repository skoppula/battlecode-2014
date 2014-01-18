package integrated;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Comms {
	
	static MapLocation intToLoc(int i){
		return new MapLocation(i/100,i%100);
	}
	
	static int sumArray(int[] arr){
		int sum = 0;

		for (int i : arr)
		    sum += i;
		
		return sum;
	}
	


	public static int idRoundToInt(int id, int roundNum) {
		return id*10000+roundNum;
	}
	
	




	//In team's channel: AABB|CCDD: (AA,BB) = attack target, (CC, DD) = location to move to
	
	public static MapLocation getTargetLocation(RobotController rc, int i) throws GameActionException {
		int j = 0;
		if (i<10) {
			j = rc.readBroadcast(i+3);
		} else {
			j = rc.readBroadcast(i);
		}
		
		return new MapLocation((j/100)%100, j%100);
	}
	
	public static MapLocation getAttackLocation(RobotController rc, int i) throws GameActionException {
		int j  = rc.readBroadcast(i);
		return new MapLocation(j/10000,(j/1000)%100);
	}

}
