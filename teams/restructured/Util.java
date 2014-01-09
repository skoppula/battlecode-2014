package restructured;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Util {
	
    public static Direction allDirections[] = Direction.values();
	
    public static Direction dirToMove(MapLocation start, MapLocation goal, int[][] terrainMap){
    	return null;
    }
    
    //the A* directions algorithm goes here, preferably implemented with subclasses
    //public static Direction[] directionsTo(MapLocation start, MapLocation goal)
    
	public static void randomMove(RobotController rc) throws GameActionException {
		
	    Random rand = new Random(rc.getRobot().getID());
		
		for (int i = 0;i<8;i++) {
    		Direction dir = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive() && rc.canMove(dir)){
            	rc.move(dir);
            	break;
            }
    	}
	}
}
