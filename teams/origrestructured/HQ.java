package origrestructured;

import java.util.ArrayList;
import java.util.HashMap;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQ {
	
    public static boolean initializerRun = false;
    public static double cowDensMap[][];
    public static int mapY, mapX;
    public static MapLocation desiredPASTRs[];
    public static boolean currPASTRs[];
    public static Team team;
    public static Team enemy;
    public static int idealNumPastures;
    
    static int[][] terrainMap;
	final static int	NORMAL = 10;
	final static int	ROAD = 3;
	final static int	WALL = 1000;
	final static int	OFFMAP = 99999;
    
    //public static ArrayList<MapLocation> enemyPASTRs = new ArrayList<MapLocation>();
	public static MapLocation[] enemyPASTRs;
	
	static RobotController hq;
	
    //Saves only spawned robots, not including HQ
    static enum types {DEFENDER, ATTACKER, PASTR, NOISETOWER};
	static HashMap occupations = new HashMap();
    
    static int[] robotTypeCount = new int[4];
	
	//	HQ Behavior Paradigm:
	//		Run initializers
	//		Keep track robots: store robot ID and occupation; and robot goals
	//		Manage communication and manage robots (HOW TO?)
	public static void runHeadquarters(RobotController rc) throws GameActionException{
		
		//This costs 8 rounds and tons of bytecodes - don't do it at first
		if(!initializerRun&&Clock.getRoundNum()>5) {
			initializeGameVars(rc);
			initializerRun = true;
		}
		
		senseEnemyPASTRs(rc);
		//updateRobotDistro(rc);
		//updateAssignments();
		
		
		//Continue spawning so that 25 robots always on field
		
		for (Direction i:Util.allDirections) {
			if(rc.isActive() && rc.canMove(i)&& totalRobots()<GameConstants.MAX_ROBOTS) {
				//spawnRobot();
				rc.spawn(i);
			}
		}
		rc.yield();
	}
	

	private static void updateAssignments() {
		// update robots assigned to:
    	// 1. creating own pastrs
    	// 2. defending own pastrs
    	// 3. attacking enemy pastrs
    	
    	//update corresponding hashmaps
	}

	public static void initializeGameVars(RobotController rc){
    	
    	hq = rc;
    	
    	team =  hq.getTeam();
    	enemy = team.opponent();
    	cowDensMap = hq.senseCowGrowth();
    	mapY = cowDensMap.length;
    	mapX = cowDensMap[0].length;
    	idealNumPastures = computeNumPastures();
    	desiredPASTRs = findPastureLocs();
    	currPASTRs = new boolean[idealNumPastures];
    	createTerrainMap();
    
    }
	
	//TO DO
	static void spawnRobot() throws GameActionException{
		//1. decide what type of robot needs to be spawned. Type of robot spawned depends on:
	    //		Current distribution of robots
	    //		Time or rounds
	    //		Size of map and distance to enemy HQ
		//2. decide direction to spawn in
		//3. spawn
		//4. update id-occupation hashtable (occupations) and appropriate assignment tables (PASTR.roboPSTRsAssignment)
		
//		Direction spawnDir = Direction.NORTH;
//		if(hq.isActive()&&hq.canMove(spawnDir)){
//			hq.spawn(spawnDir);
//		}
		System.out.println("Okay!");
	}
	
	static void senseEnemyPASTRs(RobotController rc) throws GameActionException{
		//fill in enemyPASTRs arraylist
		enemyPASTRs = rc.sensePastrLocations(rc.getTeam().opponent());
		
		//broadcast locations to channel? 10 to broadcast 2 to read, max of 5 or so pastrs...
		//channel 505505 (SOS!)
		for (int i=0;i<enemyPASTRs.length;i++) {
			int x = enemyPASTRs[i].x;
			int y = enemyPASTRs[i].y;
			rc.broadcast(5505+i, x*100+y);
		}
		
	}
	
	//TO DO: compute ideal number of pastures
	static int computeNumPastures(){
		return 5;
	}
	
	//TO DO: improve with position weighting
	static MapLocation[] findPastureLocs() {
		
		MapLocation pstrLocs[] = new MapLocation[idealNumPastures];
		int pstrCowDens[] = new int[idealNumPastures];
		
		//Fill default
		for (int i = 0; i < idealNumPastures; i++) {
			pstrLocs[i] = new MapLocation(mapX/2, mapY/2);			
		}
		
		//Slides a 3x3 window across the entire map, intervals of three and returns windows with highest 
		for(int i = 0; i < mapY-3; i+=4){
			for(int j = 0; j < mapX-3; j+=4){
				
				int sum = (int) (cowDensMap[i][j] + cowDensMap[i+1][j] + cowDensMap[i+2][j] 
							+ cowDensMap[i][j+1] + cowDensMap[i+1][j+1] + cowDensMap[i+2][j+1]
							+ cowDensMap[i][j+2] + cowDensMap[i+1][j+2] + cowDensMap[i+2][j+2]);
				
				for(int k = 0; k < idealNumPastures; k++){
					if(sum>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						pstrCowDens[k] = sum;
						break;
					}
				}
				
				
			}
		}
		
		for (int i = 0; i < pstrLocs.length; i++) {
			System.out.println("SKANDA FOUND THESE LOCATIONS: " + pstrLocs[i]);
		}
		
		return pstrLocs;
	}
	
	//TO DO
	static void updateRobotDistro(RobotController rc){
		//update currPASTRs and robotTypeCount
	}
	
	static int totalRobots(){
		return robotTypeCount[0] + robotTypeCount[1] + robotTypeCount[2] + robotTypeCount[3];
	}
	
	public static void createTerrainMap(){
		//Establish directions
		Direction allDirections[] = Direction.values();
		for (int i = 0; i < allDirections.length; i ++) {
			System.out.println(allDirections[i]);
		}

		//Get cow density field and map dimensions
		double cowDensMap[][] = hq.senseCowGrowth();
		int mapY = cowDensMap.length, mapX = cowDensMap[0].length;

		//Initialize terrain map array
		terrainMap = new int[mapY][mapX]; 

		//Scan over map to identify types of terrain at each location
		for(int i = 0; i < mapY; i++){
			for(int j = 0; j < mapX; j++){
				TerrainTile t = hq.senseTerrainTile(new MapLocation(j, i));
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
