package createTerrainMap;

import battlecode.common.*;


//This will RobotPlayer package implements creating the 2D map array containing the values

public class RobotPlayer {

	public static void run(RobotController rc){
		
		//Establish directions
		Direction allDirections[] = Direction.values();
		for (int i = 0; i < allDirections.length; i ++) {
			System.out.println(allDirections[i]);
		}
		
		//Get cow density field and map dimensions
		double cowDensMap[][] = rc.senseCowGrowth();
		int mapY = cowDensMap.length, mapX = cowDensMap[0].length;
		
		//Initialize terrain map array
		int terrainMap[][] = new int[mapY][mapX]; 
		
		//Scan over map to identify types of terrain at each location
		const int	NORMAL = 10;
		const int	ROAD = 0;
		const int	WALL = 1000;
		const int	OFFMAP = -1;
		
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
}
