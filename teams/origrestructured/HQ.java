package origrestructured;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQ {
	
    public static volatile boolean initializerRun = false;
    public static volatile double cowDensMap[][];
    public static volatile int mapY, mapX;
    public static volatile MapLocation desiredPASTRs[];
    public static volatile boolean currPASTRs[];
    public static volatile Team team;
    public static volatile Team enemy;
    public static volatile int idealNumPastures;
    
    static volatile int[][] terrainMap;
	final static int	NORMAL = 10;
	final static int	ROAD = 3;
	final static int	WALL = 1000;
	final static int	OFFMAP = 99999;
    
    public static volatile ArrayList<MapLocation> enemyPASTRs = new ArrayList<MapLocation>();
	
	static RobotController hq;
	static Random rand;
	
    //Saves only spawned robots, not including HQ
    static enum types {DEFENDER, ATTACKER, PASTR, NOISETOWER};
	public static volatile HashMap<Integer, types> occupations = new HashMap<Integer, types>();
    public static volatile types tempSpawnedType;
    
    static int[] robotTypeCount = {0,0,0,0};
	
	//	HQ Behavior Paradigm:
	//		Run initializers
	//		Keep track robots: store robot ID and occupation; and robot goals
	//		Manage communication and manage robots (HOW TO?)
    
	public static void initializeGameVars(RobotController rc) throws GameActionException{
    	
    	hq = rc;
    	
    	team =  hq.getTeam();
    	enemy = team.opponent();
    	cowDensMap = hq.senseCowGrowth();
    	mapY = cowDensMap.length;
    	mapX = cowDensMap[0].length;
    	idealNumPastures = computeNumPastures();
    	
    	desiredPASTRs = findPastureLocs();
    	
    	System.out.println(Arrays.deepToString(desiredPASTRs));
    	
    	currPASTRs = new boolean[idealNumPastures];
    	createTerrainMap();
    	initializerRun = true;
    	
    	rand = new Random(17);
    }
    
	public static void runHeadquarters(RobotController rc) throws GameActionException {
		
		//This costs 8 rounds and tons of bytecodes - don't do it at first
		if(!initializerRun && Clock.getRoundNum() < 10)
			initializeGameVars(rc);
		
		spawnRobot(rc);

		senseEnemyPASTRs(rc);
		updateRobotDistro(rc);
		updateAssignments();
		
		rc.yield();
	}


	private static void updateAssignments() {
		
		// update robots assigned to:
    	// 1. creating own pastrs
    	// 2. defending own pastrs
    	// 3. attacking enemy pastrs
    	
    	//update corresponding hashmaps
		
		for(int id:occupations.keySet()){
			
			if(PASTR.roboPSTRsAssignment.containsKey(id) || COWBOY.defendPSTRsAssignment.containsKey(id) || COWBOY.roboEnemyAssignment.containsKey(id))
				continue;
			
			types type = occupations.get(id);
			
			if(type==types.PASTR){
				for(int i = 0; i < desiredPASTRs.length; i++){
					if(!currPASTRs[i]) {
						PASTR.roboPSTRsAssignment.put(id, i);
						break;
					}
				} continue;

			} else if (type==types.DEFENDER) {
				
				int[] counts = new int[desiredPASTRs.length];
				for(int pastr:COWBOY.defendPSTRsAssignment.values())
					counts[pastr]++;
				
				COWBOY.defendPSTRsAssignment.put(id,Util.indexOfMin(counts));
			
			} else if (type==types.ATTACKER) {
				
				int[] counts = new int[enemyPASTRs.size()];
				for(int pastr:COWBOY.roboEnemyAssignment.values())
					counts[pastr]++;
				
				COWBOY.roboEnemyAssignment.put(id,Util.indexOfMin(counts));
				
			}
		}
	}
	
	static void spawnRobot(RobotController rc) throws GameActionException{
		
		//1. decide what type of robot needs to be spawned. Type of robot spawned depends on:
	    //		Current distribution of robots
	    //		Time or rounds
	    //		Size of map and distance to enemy HQ
		//2. decide direction to spawn in
		//3. spawn
		//4. update id-occupation hashtable (occupations) and appropriate assignment tables (PASTR.roboPSTRsAssignment)
		
		//System.out.println(rc.senseRobotCount());
		//System.out.println(Arrays.toString(robotTypeCount));
		
		if(Util.sumArray(robotTypeCount)<GameConstants.MAX_ROBOTS && rc.isActive()){
			
			if(robotTypeCount[2] < desiredPASTRs.length)
				PASTR.spawnPASTR(rc);
			
			else if (robotTypeCount[0] < 1.5*desiredPASTRs.length)
				COWBOY.spawnCOWBOY(rc, types.DEFENDER);
			
			else if (robotTypeCount[1] < 5)
				COWBOY.spawnCOWBOY(rc, types.ATTACKER);
			
			else {
				if(rand.nextDouble()<0.5)
					COWBOY.spawnCOWBOY(rc, types.DEFENDER);
				else
					COWBOY.spawnCOWBOY(rc, types.ATTACKER);
			}
		}
	}
	
	static void senseEnemyPASTRs(RobotController rc) throws GameActionException{

		//fill in enemyPASTRs arraylist
		MapLocation[] currEnemyLocs = rc.sensePastrLocations(team.opponent());
		List<MapLocation> currEnemyList = Arrays.asList(currEnemyLocs); 
		
		//add in new locations
		for(MapLocation m:currEnemyLocs){
			if(!enemyPASTRs.contains(m))
				enemyPASTRs.add(m);
		}
		
		//remove old locations and remove assigned robots
		for(int i = 0; i < enemyPASTRs.size(); i++){
			if(!currEnemyList.contains(enemyPASTRs.get(i))){
				enemyPASTRs.set(i, currEnemyLocs[0]);
			}
		}
		
		//broadcast locations to channel? 10 to broadcast 2 to read, max of 5 or so pastrs...
		//channel 505505 (SOS!)
		for (int i=0;i<enemyPASTRs.size();i++) {
			int x = enemyPASTRs.get(i).x;
			int y = enemyPASTRs.get(i).y;
			rc.broadcast(505+i, x*100+y);
		}
		
	}
	
	//TO DO: compute ideal number of pastures
	static int computeNumPastures(){
		return 4;
	}
	
	//TO DO: improve with position weighting
	static MapLocation[] findPastureLocs() throws GameActionException {
		
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
				
				//More weight = farther away from HQ = bad
				double weight = hq.getLocation().distanceSquaredTo(new MapLocation(j,i));
				
				for(int k = 0; k < idealNumPastures; k++){
					
					//Balancing profit in pasture productivity vs. distance: (sum-weight/10)
					if((sum-weight/10)>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						
						//broadcast these locations to channel 168
						int pstrlocint = Util.locToInt(pstrLocs[k]);
						hq.broadcast(168 + k, pstrlocint);
						
						pstrCowDens[k] = sum;
						break;
					}
				}
			}
		}
		return pstrLocs;
	}
	
	//TO DO
	static void updateRobotDistro(RobotController rc){
		//update currPASTRs and robotTypeCount
	}
	
	public static void createTerrainMap(){

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
