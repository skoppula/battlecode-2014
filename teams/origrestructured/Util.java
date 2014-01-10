package origrestructured;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Util {
	
    public static Direction allDirections[] = Direction.values();
    static Random rand = new Random();
	
    public static Direction dirToMove(MapLocation start, MapLocation goal, int[][] terrainMap){
    	return null;
    }
    
    //the A* directions algorithm goes here, preferably implemented with subclasses
    //public static Direction[] directionsTo(MapLocation start, MapLocation goal)
    
	
	
	static void cornerMove(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
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
				res = new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH};
			case NORTH_EAST:
				res = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
			case EAST:
				res = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST};
			case SOUTH_EAST:
				res = new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
			case SOUTH:
				res = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH};
			case SOUTH_WEST:
				res = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
			case WEST:
				res = new Direction[]{Direction.SOUTH, Direction.NORTH, Direction.EAST};
			case NORTH_WEST:
				res = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
				
		}
		return res;
	}
	
	public static void unstick(RobotController rc, Direction toDest) throws GameActionException{
		System.out.println("Trying to move " + toDest);
		for(Direction tryDir: tryDirections(toDest)){ //think of ways that would make sense to try, ordered by likelihood of finding opening
			if(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
				if(rc.isActive()){
					System.out.println("Moving " + tryDir);
					rc.move(tryDir);
				}
			}
			if(rc.canMove(tryDir) == false){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
				System.out.println("Can't move " + tryDir);
				continue;
			}
			if(rc.canMove(toDest)){
				System.out.println("found hole");
				break;
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static void bstarMove(RobotController rc, MapLocation goal) throws GameActionException {
		// TODO Auto-generated method stub
		MapLocation dest = new MapLocation(20, 20); //testing to go to 5,5 here.
		MapLocation loc = rc.getLocation();
		Direction toDest = rc.getLocation().directionTo(dest);
		//System.out.println("bstarMove!!!");
    	if(loc.x != dest.x && loc.y != dest.y){
    		System.out.println("Hi!");
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive()){ //if robot can't move toDest...
    				System.out.println("UNSTICKING");
    				unstick(rc, toDest); //unstick it
    			}
    		rc.yield();
    		}
    	}//until rc.getLocation.equals(dest)
    	else {
    		System.out.println("YAAAY WE'RE IN BUSINESS");
    	}
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

	
}
