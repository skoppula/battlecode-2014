package dragon;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Comms {
	
	static MapLocation intToLoc(int i){
		return new MapLocation(i/100,i%100);
	}
	
	static int locToInt(MapLocation m){
		return (m.x*100 + m.y);
	}
	
	static void PSTRLocsToComm(RobotController rc, MapLocation[] pstr) throws GameActionException {
		for (int i=0;i<pstr.length;i++) {
			int channel = 3;
			rc.broadcast(channel+i, locToInt(pstr[i]));
		}
	}
	
    static MapLocation[] commToPSTRLocs(RobotController rc){
    	
    	int channel = 3;
    	ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
    	
    	
    	try {
    		int val = rc.readBroadcast(channel);
    		while(val!=0){
    			locs.add(Comms.intToLoc(val));
    			channel++;
    			val = rc.readBroadcast(channel);
    		}
    	} catch (GameActionException e){
    		e.printStackTrace();
    	}
    	
    	
    	return locs.toArray(new MapLocation[locs.size()]);
    }

	static int sumArray(int[] arr){
		int sum = 0;

		for (int i : arr)
		    sum += i;
		
		return sum;
	}
	
	static int idAssignToInt(int id, int j){
		return id*100+j;
	}

	public static int idRoundToInt(int id, int roundNum) {
		return id*10000+roundNum;
	}
	
	
	public static int assignmentToInt(int squad, int role) {
		
		return squad*100+role;
	}

	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
	public static int getSquad(int i) {
		return (i/100)%100;
	}

	public static int getRole(int i) {
		return i%100;
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
