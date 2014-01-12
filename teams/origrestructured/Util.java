package origrestructured;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Util {
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};
    static Random rand = new Random();
	
    public static Direction dirToMove(MapLocation start, MapLocation goal, int[][] terrainMap){
    	return null;
    }
    
    public static int indexOfMin(int... arr) {
        int idx = -1;
        int p = Integer.MAX_VALUE;
        for(int i = 0; i < arr.length; i++)
            if(arr[i] < p) {
                p = arr[i];
                idx = i;
            }
        return idx;
    }
    
    
    //the A* directions algorithm goes here, preferably implemented with subclasses
    //public static Direction[] directionsTo(MapLocation start, MapLocation goal)
	
	static void cornerMove(RobotController rc) throws GameActionException {
		//For some reason this shows preference for corners, and random twitching
		for (int i = 0;i<7;i++) {
			Random rand = new Random(rc.getRobot().getID());
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	@SuppressWarnings("incomplete-switch")
	public static Direction[] tryDirections(Direction toDest){ //this method basically just returns a list of directions i think it should try when stuck. just logic'ed it out here.
		Direction[] res = new Direction[]{toDest.opposite()};
		switch(toDest){
			case NORTH:
				res = new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.SOUTH_EAST};
			case NORTH_EAST:
				res = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
			case EAST:
				res = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST};
			case SOUTH_EAST:
				res = new Direction[]{Direction.EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.WEST, Direction.NORTH};
			case SOUTH:
				res = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH_WEST, Direction.SOUTH_EAST, Direction.NORTH, Direction.NORTH_WEST, Direction.NORTH_EAST};
			case SOUTH_WEST:
				res = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.NORTH_EAST, Direction.NORTH, Direction.EAST};
			case WEST:
				res = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.EAST, Direction.NORTH_EAST, Direction.SOUTH_EAST};
			case NORTH_WEST:
				res = new Direction[]{Direction.NORTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.EAST};
		}
		return res;
	}
	
	public static void unstick(RobotController rc, Direction toDest, MapLocation dest) throws GameActionException{
		System.out.println("Trying to move " + toDest);
		Direction[] attempts = tryDirections(toDest);
		for (int i=0;i< attempts.length;i++) {
			int j = 6 - rand.nextInt(7);
			System.out.println(j);
			Direction tryDir = attempts[j]; 
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
				if(rc.isActive()){
					System.out.println("Moving " + tryDir);
					rc.move(tryDir);
				}
				rc.yield();
			}
			if(rc.canMove(tryDir) == false){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
				System.out.println("Can't move " + tryDir);
				continue;
			}
			if(rc.canMove(toDest)){
				//System.out.println("found hole");
				if(rc.isActive()){
					rc.move(toDest);
					System.out.print("Moving toDest");
				}
				else{
					rc.yield();
					if(rc.isActive()&&rc.canMove(toDest)){
						rc.move(toDest);
					}
				}
				break;
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
		// TODO Auto-generated method stub
		
		System.out.println("Destinatino is " + dest.x + "and " + dest.y);

    	Direction toDest = rc.getLocation().directionTo(dest);
    	if(rc.getLocation().equals(dest) == false){
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				//System.out.println("UNSTICKING");
    				unstick(rc, toDest, dest); //unstick it
    			}
    		}
    	}//until rc.getLocation.equals(dest)
    	
	}
	

	static void randomMove(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
		for (int i = 0;i<7;i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	static MapLocation intToLoc(int i){
		return new MapLocation(i/100,i%100);
	}
	
	static int locToInt(MapLocation m){
		return (m.x*100 + m.y);
	}

	static int sumArray(int[] arr){
		int sum = 0;

		for (int i : arr)
		    sum += i;
		
		return sum;
	}
	
	static void printHashMap(HashMap map){

		Iterator iterator = map.keySet().iterator();  
		   
		while (iterator.hasNext()) {  
		   String key = "" + iterator.next();  
		   String value = "" + map.get(key);
		 
		   System.out.println("KEY:" + key + " VALUE:" + value);  
		}
	}
}
