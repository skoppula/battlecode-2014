package team063;

import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

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
		Direction[] directions = new Direction[]{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		int dir = Arrays.asList(directions).indexOf(toDest);
		System.out.println(dir);
		switch(dir){
			case 0:
				res = new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH};
				break;
			case 1:
				res = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
				break;
			case 2:
				res = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST};
				break;
			case 3:
				res = new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
				break;
			case 4:
				res = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH};
				break;
			case 5:
				res = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
				break;
			case 6:
				res = new Direction[]{Direction.SOUTH, Direction.NORTH, Direction.EAST};
				break;
			case 7:
				res = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
				break;
				
		}
		return res;
	}
	
	public static void unstick(RobotController rc, Direction toDest, MapLocation dest) throws GameActionException{
		System.out.println("Trying to move " + toDest + " towards (" + dest.x + ", " + dest.y + ")");
		Direction[] directions = tryDirections(toDest);
		for(Direction tryDir: directions){ //think of ways that would make sense to try, ordered by likelihood of finding opening
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
				System.out.println("found hole");
				if(rc.isActive()){
					rc.move(toDest);
					System.out.println("Moving toDest");
				}
				else{
					rc.yield();
					if (rc.isActive()){
						rc.move(toDest);
					}
					System.out.println("Took a nap and then moved toDest");
					break;
				}
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
		// TODO Auto-generated method stub

    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				System.out.println("UNSTICKING");
    				unstick(rc, toDest, dest); //unstick it
    				toDest = rc.getLocation().directionTo(dest);
    			}
    		rc.yield();
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
	
    static MapLocation[] commToPSTRLocs(RobotController rc){
    	
    	int channel = 51;
    	ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
    	
    	
    	try {
    		int val = rc.readBroadcast(channel);
    		while(val!=-1){
    			locs.add(Util.intToLoc(val));
    			channel++;
    			val = rc.readBroadcast(channel);
    		}
    	} catch (GameActionException e){
    		e.printStackTrace();
    	}
    	
    	
    	return locs.toArray(new MapLocation[locs.size()]);
    }

    static MapLocation[] commToEnemyPSTRLocs(RobotController rc){
    	
    	
    	int channel = 71;
    	ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
    	
    	try {
    		int val = rc.readBroadcast(channel);
    		while(val!=-1){
    			locs.add(Util.intToLoc(val));
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
	
	static void shootNearby(RobotController rc) throws GameActionException {
		//shooting
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//if there are enemies
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(anEnemyInfo.location);
				}
			}
		}
	}
	
//	static void printHashMap(HashMap map){
//
//		Iterator iterator = map.keySet().iterator();  
//		   
//		while (iterator.hasNext()) {  
//		   String key = "" + iterator.next();  
//		   String value = "" + map.get(key);
//		 
//		   System.out.println("KEY:" + key + " VALUE:" + value);  
//		}
//	}
}
