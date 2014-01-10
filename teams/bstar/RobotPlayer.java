package bstar;

/*
 * b* allows a robot to calculate on each step what direction it has to move to get to a goal location. includes an unstick method that allows robot to "glide off" walls it hits and
 * find ways out of corners.
 * 
 * the best way to visualize what i've done is to run the robot in the bakedpotato map which has a ton of corners that it is prone to get stuck in. castles is also pretty good.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import battlecode.common.*;

public class RobotPlayer{
	static int[][] terrainMap;
	
	public static void createTerrainMap(RobotController rc){
		//Establish directions
		Direction allDirections[] = Direction.values();
		for (int i = 0; i < allDirections.length; i ++) {
			System.out.println(allDirections[i]);
		}

		//Get cow density field and map dimensions
		double cowDensMap[][] = rc.senseCowGrowth();
		int mapY = cowDensMap.length, mapX = cowDensMap[0].length;

		//Initialize terrain map array
		terrainMap = new int[mapY][mapX]; 

		//Scan over map to identify types of terrain at each location
		final int	NORMAL = 10;
		final int	ROAD = 3;
		final int	WALL = 1000;
		final int	OFFMAP = 99999;

		for(int i = 0; i < mapY; i++){
			for(int j = 0; j < mapX; j++){
				TerrainTile t = rc.senseTerrainTile(new MapLocation(j, i));
				if(t==TerrainTile.valueOf("NORMAL"))
					terrainMap[j][i] = NORMAL;
				else if(t==TerrainTile.valueOf("ROAD"))
					terrainMap[j][i] = ROAD;
				else if(t==TerrainTile.valueOf("VOID"))
					terrainMap[j][i] = WALL;
				else
					terrainMap[j][i] = OFFMAP;
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
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
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
	
	public static void run(RobotController rcin){
        RobotController rc = rcin;
        createTerrainMap(rc);
		Direction[] allDirections = Direction.values();
        
        while(true){
            try{
                    if(rc.getType()==RobotType.HQ){//if I'm a headquarters
                    	Direction spawnDir = Direction.NORTH;
        				if(rc.senseRobotCount() == 0 && rc.isActive()&&rc.canMove(spawnDir)){
        					rc.spawn(Direction.NORTH);
        				}
                    }else if(rc.getType()==RobotType.SOLDIER){
                    	MapLocation dest = new MapLocation(5,5); //testing to go to 5,5 here.
                    	Direction toDest = rc.getLocation().directionTo(dest);
                    	while(rc.getLocation().equals(dest) == false){
                    		if(rc.isActive() && rc.canMove(toDest)){
                    			rc.move(toDest);
                    			toDest = rc.getLocation().directionTo(dest);
                    		}else{ //robot is either inactive or can't move toDest
                    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
                    				System.out.println("UNSTICKING");
                    				unstick(rc, toDest); //unstick it
                    			}
                    		}
                    	}//until rc.getLocation.equals(dest)
                    }
                    rc.yield();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
//	public static void main(String[] args){
//		MapLocation a = new MapLocation(0,0);
//		MapLocation b = new MapLocation (2,3);
//		System.out.print(a.directionTo(b));
//	}
}
