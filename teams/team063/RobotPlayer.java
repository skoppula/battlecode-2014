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
	
	/*
	 * @returns a list of MapLocations from a start location to a goal. Use this to find the directions that the robot has to go (eventually a "goto" method)
	 */
	public static MapLocation[] directionsTo(RobotController rc, MapLocation start, MapLocation goal){ //basically implements a* search
		searchNode current = null;
		searchNode startNode = new searchNode(start, null);
		if(start.equals(goal)){
			MapLocation[] res = new MapLocation[]{start};
			return res;
		}
		List<searchNode> agenda = new ArrayList<searchNode>();
		for(searchNode child:searchNode.getChildren(rc, startNode)){
			if(child.state == goal){
				return (MapLocation[]) child.getPath();
			}
			else{
				agenda.add(child);
			}
		}
			while(agenda.size() != 0){
					Collections.sort(agenda, new PathCompare());
					current = agenda.get(0);
				}
				for(searchNode child:searchNode.getChildren(rc, current)){
					if(child.state == goal){
						return (MapLocation[]) child.getPath();
					}
					else{
						agenda.add(child);
						}
					}
			return null;	
			}
	
	
	public static void run(RobotController rc){
			Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
			
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
		
		while(true){
			if(rc.getType()==RobotType.HQ){//if I'm a headquarters
				Direction spawnDir = Direction.NORTH;
				try {
					if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
						rc.spawn(Direction.NORTH);
					}
				} catch (GameActionException e) {
					// TODO hi contestant who downloaded this.
					e.printStackTrace();
				}
			}else if(rc.getType()==RobotType.SOLDIER){
				//shooting
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
				if(enemyRobots.length>0){//if there are enemies
					Robot anEnemy = enemyRobots[0];
					RobotInfo anEnemyInfo;
					try {
						anEnemyInfo = rc.senseRobotInfo(anEnemy);
						if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
							if(rc.isActive()){
								rc.attackSquare(anEnemyInfo.location);
							}
						}
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{//there are no enemies, so build a tower
					if(Math.random()<0.01){
						if(rc.isActive()){
							try {
								rc.construct(RobotType.PASTR);
							} catch (GameActionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else {
						//Move the robot toward the areas with the greatest cow growth
						double cowmap[][] = rc.senseCowGrowth();
						for (int i = 0; i < cowmap.length; i++) {					
							double cowmapx[] = cowmap[i];
						
							double max = cowmapx[0];
							for (int j = 1; j < cowmapx.length; j++) {
								if (cowmapx[i] > max) {
									max = cowmapx[i];
									//Here we should probably sort them in another array that lists the locations with the highest cow growth
								}
							}
						}
					}
				}
				//movement
				Direction chosenDirection = allDirections[(int)(Math.random()*8)];
				if(rc.isActive()&&rc.canMove(chosenDirection)){
					try {
						rc.move(chosenDirection);
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
			}
			rc.yield();
		}
	}
}