package origrestructured;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
	
    static HashMap<Integer, Integer> roboPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
    static HashMap<Integer, Integer> defendPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
    static HashMap<Integer, Integer> roboEnemyAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in enemyPASTRs[] ]
	
    public static volatile boolean initializerRun = false;
    public static volatile double cowDensMap[][];
    public static volatile int mapY, mapX;
    static volatile MapLocation desiredPASTRs[];
    public static volatile boolean currPASTRs[]; //This is pasture locations that have robots assigned and en route to become pastures there, not 'current' pastures
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
    	System.out.println("Desired pastures : " +Arrays.deepToString( desiredPASTRs));
    	writePSTRLocsToComm(rc, desiredPASTRs);
    	
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
		
    	//update corresponding hashmaps
		commToHashMaps(rc);
		
		senseEnemyPASTRs(rc);
		updateRobotDistro(rc);
		updateAssignments(rc);
		
		rc.yield();
	}

	static void commToHashMaps(RobotController rc){
		//channels 2 - 25 encode robot id:occupation mapping.
		//Encoded as XXXXX0Y where XXXXX is the robot ID and Y is robot occupation
		//Y = 0 DEFENDER, 1 ATTACKER, 2 PASTR, 3 NOISETOWER
		for(int i = 2; i < 26; i++){
			if(i==-1)
				continue;
			
			int val = 0;
			try {
				val = rc.readBroadcast(i);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			
			int job = val%100;
			int id = (val-job)/100;
			
			switch(job) {
				case 0: occupations.put(id, HQ.types.DEFENDER); break;
				case 1: occupations.put(id, HQ.types.ATTACKER); break;
				case 2: occupations.put(id, HQ.types.PASTR); break;
				case 3: occupations.put(id, HQ.types.NOISETOWER); break;
			}
		}
		
		for(int i = 26; i < 51; i++){
			
			int val = 0;
			try {
				val = rc.readBroadcast(i);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			
			if(val==-1)
				continue;
			
			int index = val%100;
			int id = (val-index)/100;
			
			switch(occupations.get(id)) {
				case DEFENDER: defendPSTRsAssignment.put(id, index); break;
				case ATTACKER: roboEnemyAssignment.put(id, index); break;
				case PASTR: roboPSTRsAssignment.put(id, index); break;
				case NOISETOWER: break;
			}
		}
	}
	
	private static void updateAssignments(RobotController rc) {
		
		// update robots assigned to:
    	// 1. creating own pastrs
    	// 2. defending own pastrs
    	// 3. attacking enemy pastrs

		for(int id:occupations.keySet()){
			
			if(roboPSTRsAssignment.containsKey(id) || defendPSTRsAssignment.containsKey(id) || roboEnemyAssignment.containsKey(id))
				continue;
			
			types type = occupations.get(id);
			
			if(type==types.PASTR){
				for(int i = 0; i < desiredPASTRs.length; i++){
					if(!currPASTRs[i]) {
						roboPSTRsAssignment.put(id, i);
						currPASTRs[i]=true;
						break;
					}
				} continue;

			} else if (type==types.DEFENDER) {
				
				int[] counts = new int[desiredPASTRs.length];
				for(int pastr:defendPSTRsAssignment.values())
					counts[pastr]++;
				
				defendPSTRsAssignment.put(id,Util.indexOfMin(counts));
			
			} else if (type==types.ATTACKER) {
				
				int[] counts = new int[enemyPASTRs.size()];
				for(int pastr:roboEnemyAssignment.values())
					counts[pastr]++;
				
				roboEnemyAssignment.put(id,Util.indexOfMin(counts));
				
			}
		}
		
		assignmentsToComm(rc);
	}
	
	static void assignmentsToComm(RobotController rc){
		
		try {
			Iterator<Integer> iterator = roboPSTRsAssignment.keySet().iterator();  
			
			int channel = 26;
			while (iterator.hasNext()) {  
			   int id = iterator.next();  
			   int index = roboPSTRsAssignment.get(id);
			   rc.broadcast(channel, Util.idAssignToInt(id, index));
			   channel++;
			}
			
			iterator = defendPSTRsAssignment.keySet().iterator();
			while (iterator.hasNext()) {  
				   int id = iterator.next();  
				   int index = defendPSTRsAssignment.get(id);
				   rc.broadcast(channel, Util.idAssignToInt(id, index));
				   channel++;
			}
			
			iterator = roboEnemyAssignment.keySet().iterator();
			while (iterator.hasNext()) {  
				   int id = iterator.next();  
				   int index = roboEnemyAssignment.get(id);
				   rc.broadcast(channel, Util.idAssignToInt(id, index));
				   channel++;
			}
		} catch (GameActionException e){
			e.printStackTrace();
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
			
			System.out.println("Types of robots : " + Arrays.toString(robotTypeCount));
			if(robotTypeCount[2] < desiredPASTRs.length)
				PASTR.spawnPASTR(rc);
			
			else if (robotTypeCount[0] < desiredPASTRs.length)
				COWBOY.spawnCOWBOY(rc, types.DEFENDER);
			
			else if (robotTypeCount[1] < 5)
				COWBOY.spawnCOWBOY(rc, types.ATTACKER);
			
			else if(robotTypeCount[0] < desiredPASTRs.length){
				COWBOY.spawnCOWBOY(rc, types.DEFENDER);
			}
			
			else {
				if(rand.nextDouble()<0.5)
					COWBOY.spawnCOWBOY(rc, types.DEFENDER);
				else
					COWBOY.spawnCOWBOY(rc, types.ATTACKER);
			}
		}
	}
	
	static void senseEnemyPASTRs(RobotController rc) {
		
//		System.out.println(enemyPASTRs);
		
		//fill in enemyPASTRs arraylist
		MapLocation[] currEnemyLocs = rc.sensePastrLocations(team.opponent());
		List<MapLocation> currEnemyList = Arrays.asList(currEnemyLocs); 
		
		//add in new locations
		for(MapLocation m:currEnemyLocs){
			if(!enemyPASTRs.contains(m))
				enemyPASTRs.add(m);
		}
		
		//remove old locations and reassign those robots by replacing their assignmed location
		for(int i = 0; i < enemyPASTRs.size(); i++){
			if(!currEnemyList.contains(enemyPASTRs.get(i))){
				if(currEnemyLocs.length==0) //ATTACKER MOVES TO ENEMY HQ IF THERE IS NO OTHER PLACE TO ATTACK.
						enemyPASTRs.set(i, new MapLocation(-1, -1));
				else
					enemyPASTRs.set(i, currEnemyLocs[0]);
			}
		}
		
		writeEnemyLocsToComm(rc);
	}
	
	static void writeEnemyLocsToComm(RobotController rc){
		try {
			for(int i = 0; i < enemyPASTRs.size(); i++){
					rc.broadcast(71+i,Util.locToInt(enemyPASTRs.get(i)));
			}
		} catch (GameActionException e) {
			e.printStackTrace();
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
					if((sum-weight/(mapY))>pstrCowDens[k]){
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
	
	static void writePSTRLocsToComm(RobotController rc, MapLocation[] pstrLocs){
		try {
			for(int i = 0; i < pstrLocs.length; i++){
					rc.broadcast(51+i,Util.locToInt(pstrLocs[i]));
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	//TO DO
	static void updateRobotDistro(RobotController rc){
		
		int clock = Clock.getRoundNum();
		
		try {
			for(int i = 101; i < 131; i++){
				int val = rc.readBroadcast(i);
				
				if(val==-1)
					break;
				
				
				int round = val%10000;
				int id = (val-round)/10000;
				
				if((clock-round)>1 && occupations.containsKey(id)){
					switch(occupations.get(id)) {
						case DEFENDER: { 
							if(clock%11==0)
								robotTypeCount[0]--; 
										defendPSTRsAssignment.remove(id);
										occupations.remove(id);
										break;
						}
						case ATTACKER: {robotTypeCount[1]--; 
										roboEnemyAssignment.remove(id);
										occupations.remove(id);
										break;		
						}
						case PASTR: break;
						case NOISETOWER: break;
					}
				}	
			}
			
			List<MapLocation> currPASTRsLocs = Arrays.asList(rc.sensePastrLocations(team));
			if(clock>200 && currPASTRsLocs.size()<desiredPASTRs.length){
				
				//TO DO: need to keep track of PASTR to-be's: spawning too many PASTRs while other pastrs are en route
				if(clock%11==0)
					robotTypeCount[2]--;
				
				currPASTRs = new boolean[desiredPASTRs.length];
				
				for(int i = 0; i < desiredPASTRs.length; i++){
					
					for(int j = 0; j < currPASTRsLocs.size();j++){
						if(desiredPASTRs[i].distanceSquaredTo(currPASTRsLocs.get(j))<3)
							currPASTRs[i] = true;
					}
				}
			}
			
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		
		
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
