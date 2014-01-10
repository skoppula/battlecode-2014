package team063;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//this is the code from the first Battlecode 2014 lecture
//paste this text into RobotPlayer.java in a package called bob
//this code is badly organized. We'll fix it in later lectures.
//you can use this as a reference for how to use certain methods.

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
					terrainMap[i][j] = NORMAL;
				else if(t==TerrainTile.valueOf("ROAD"))
					terrainMap[i][j] = ROAD;
				else if(t==TerrainTile.valueOf("VOID"))
					terrainMap[i][j] = WALL;
				else
					terrainMap[i][j] = OFFMAP;
			}
		}
	}
	
	public static void moveTo(RobotController rc, MapLocation goal) throws GameActionException{
		Direction[] directions = Navigation.directionsTo(rc.getLocation(), goal);
		while(directions.length > 0){
			Direction toGoal = directions[0];
			directions.remove(0);
			if(rc.isActive()&&rc.canMove(toGoal)){
				rc.move(toGoal);
			}
		}
	}
	
//	public static void run(RobotController rc) throws GameActionException{
//		Direction[] allDirections = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
//		createTerrainMap(rc); //stores a terrain map in RobotPlayer.terrainMap
//		Direction[] path;
//		
//		while(true){
//			
//			if(rc.getType()==RobotType.HQ){//if I'm a headquarters
//				Direction spawnDir = Direction.NORTH;
//				if(rc.senseRobotCount() == 0 && rc.isActive()){
//					rc.spawn(Direction.NORTH);
//				}
//			}
//			if(rc.getType()==RobotType.SOLDIER){
//				MapLocation destination = new MapLocation(5,5);
//				moveTo(rc, destination);
//				rc.yield();
//				
//				//Direction[] directions = Navigation.directionsTo(rc.getLocation(), destination);
//				
//			}
//		}
//	}
//}
	public static void run(RobotController rcin){
        RobotController rc = rcin;
		Direction[] allDirections = Direction.values();
        Direction[] kevinDir = Navigation.directionsTo(rcin.getLocation(), new MapLocation(5,5));
        int round = 0;
        createTerrainMap(rc);
        //robotID + how much it has gone through the list
        //put zeroes in the places that it has already gone through
        
        while(true){
            try{
                    if(rc.getType()==RobotType.HQ){//if I'm a headquarters
                    	Direction spawnDir = Direction.NORTH;
        				if(rc.senseRobotCount() == 0 && rc.isActive()){
        					rc.spawn(Direction.NORTH);
        				}
                    }else if(rc.getType()==RobotType.SOLDIER){
                    	
                    	//First I want to get the robot out of the headquarters a little
                    	if(Clock.getRoundNum() < 10 && rc.isActive()&&rc.canMove(Direction.NORTH)){
                    		rc.move(Direction.NORTH);
                    	} else {
                    		//Now it will go in a clockwise circle following the array
                    		if (rc.isActive()&&rc.canMove(kevinDir[round])) {
                    			rc.move(kevinDir[round]);
                    			round +=1;
                    		}
                    		
                    		
                    		
                    	}
                    }
                    
                    rc.yield();
            } catch (Exception e) {
            	e.printStackTrace();
            }
            }
    }
}