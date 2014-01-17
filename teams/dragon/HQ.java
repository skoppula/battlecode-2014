package dragon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQ {
	
	static boolean initializerRun = false;
    static double cowDensMap[][];
    static int mapY, mapX;
    static MapLocation desiredPASTRs[];
    static  Team team;
    static Team enemy;
    static int idealNumPastures;
    static boolean constructNT = false;
    
    static int[] squads = new int[20];
    
    static int[][] terrainMap;
	final static int	NORMAL = 10;
	final static int	ROAD = 3;
	final static int	WALL = 1000;
	final static int	OFFMAP = 99999;
    
    public static volatile ArrayList<MapLocation> enemyPASTRs = new ArrayList<MapLocation>();
	
	static RobotController hq;
	static Random rand;

    static int[] robotTypeCount = {0,0,0,0};

	public static void runHeadquarters(RobotController rc) throws GameActionException {
		
		//This costs 8 rounds and tons of bytecodes - don't do it at first
		if(!initializerRun)
			initializeGameVars(rc);
		
		updateSquads(rc);
		//updateRobotDistro(rc);
		
		//Move.shootNearby(rc);
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		} else {
			spawnRobot(rc);
		}
		
		rc.yield();
	}
	
	//Put into channels correct pasture locations and enemy locations
	static void updateSquads(RobotController rc) throws GameActionException{
		for(int i = 0; i < desiredPASTRs.length; i++)
			rc.broadcast(i+3, Comms.locToInt(desiredPASTRs[i]));
		
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		
		for(int i = 0; i < enemyPASTRs.length; i++)
			rc.broadcast(i+11, Comms.locToInt(enemyPASTRs[i]));
	}
	
	static void spawnRobot(RobotController rc) throws GameActionException{

		if(rc.senseRobotCount()<GameConstants.MAX_ROBOTS && rc.isActive()){
			int squad = nextSquadNum();
			//int squad = 11;
//			Move.tryToSpawn(rc);
//			rc.broadcast(0, Comm.assignmentToInt(squad, 1));
//			System.out.println("Attacker : " + Comm.assignmentToInt(squad, 1));
//			HQ.robotTypeCount[1]++;
//			System.out.println("Spawned a attacker");
			
			if(constructNT){
				Move.tryToSpawn(rc);
				HQ.robotTypeCount[3]++;
				rc.broadcast(0, Comms.assignmentToInt(squad, 3));
				System.out.println("Spawned a noise tower precursor");
				constructNT = false;
			 
			} else if (robotTypeCount[0] < 2*desiredPASTRs.length){
				Move.tryToSpawn(rc);
				rc.broadcast(0, Comms.assignmentToInt(squad, 0));
				HQ.robotTypeCount[0]++;
				System.out.println("Spawned a defender");
			 
			} else if(robotTypeCount[2] < desiredPASTRs.length) {
				Move.tryToSpawn(rc);
				HQ.robotTypeCount[2]++;
				rc.broadcast(0, Comms.assignmentToInt(squad, 2));
				constructNT = true;
				System.out.println("Spawned a pasture precursor");
			
			} else {
				Move.tryToSpawn(rc);
				rc.broadcast(0, Comms.assignmentToInt(squad, 1));
				System.out.println("Attacker : " + Comms.assignmentToInt(squad, 1));
				HQ.robotTypeCount[1]++;
				System.out.println("Spawned a attacker");
			}
		}
	}

	private static int nextSquadNum() throws GameActionException {
		// decides offensive or defensive strategy
		int ally = hq.sensePastrLocations(team).length;
		int enemy = hq.sensePastrLocations(hq.getTeam().opponent()).length;
		
		//If there are more ally pastures than enemy ones, keep on building pastures.
		//Otherwise, start attacking enemy pastures
		return ally>=enemy?ally+3:enemy+10;
	}

	public static void initializeGameVars(RobotController rc) throws GameActionException{
    	hq = rc;
    	
    	team =  hq.getTeam();
    	enemy = team.opponent();
    	cowDensMap = hq.senseCowGrowth();
    	mapY = cowDensMap.length;
    	mapX = cowDensMap[0].length;
    	idealNumPastures = computeNumPastures();
    	
    	desiredPASTRs = findPastureLocs();
    	System.out.println("Desired pastures : " +Arrays.deepToString(desiredPASTRs));
    	
    	createTerrainMap();
    	initializerRun = true;
    	
    	rand = new Random(17);
    }
	
	private static int computeNumPastures() {
		// TODO Auto-generated method stub
		return 2;
	}

	static MapLocation[] findPastureLocs() throws GameActionException {
		//returns a MapLocation array with the best pastures
		
		MapLocation pstrLocs[] = new MapLocation[idealNumPastures];
		double pstrCowDens[] = new double[idealNumPastures];
		
		//Fill default
		for (int i = 0; i < idealNumPastures; i++) {
			pstrLocs[i] = new MapLocation(mapX/2, mapY/2);
		}
		
		//The first pasture will be right next to the HQ
		PstrLocs[0] = findHQpstr();
		
		//The next pastures are decided based on cow density
		//Slides a 3x3 window across the entire map, intervals of three and returns windows with highest 
		for(int i = 0; i < mapY-3; i+=4){
			for(int j = 0; j < mapX-3; j+=4){
				
				double sum = (cowDensMap[i][j] + cowDensMap[i+1][j] + cowDensMap[i+2][j] 
							+ cowDensMap[i][j+1] + cowDensMap[i+1][j+1] + cowDensMap[i+2][j+1]
							+ cowDensMap[i][j+2] + cowDensMap[i+1][j+2] + cowDensMap[i+2][j+2]);
				
				//More weight = farther away from HQ = bad
				double weight = hq.getLocation().distanceSquaredTo(new MapLocation(j,i));
				double weight1 = hq.senseEnemyHQLocation().distanceSquaredTo(new MapLocation(j,i));
				
				for(int k = 1; k < idealNumPastures; k++){
					
					//Balancing profit in pasture productivity vs. distance: (sum-weight/10)
					if((sum-weight/weight1)>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						
						pstrCowDens[k] = (sum-weight/weight1);
						break;
					}
				}
			}
		}
		Comms.PSTRLocsToComm(hq, pstrLocs);
		System.out.println(pstrLocs);
		return pstrLocs;
	}
		
	private static MapLocation findHQpstr() {
		// returns the first pstr location, close to the HQ so it can be defended well
		MapLocation HQ = hq.senseHQLocation();
		MapLocation enemyHQ = hq.senseEnemyHQLocation();
		Direction away_from_enemy = enemyHQ.directionTo(HQ);
		MapLocation HQpstr = null;
		
		if (hq.canMove(away_from_enemy)) { //check to see if that spot exists
			HQpstr = HQ.add(away_from_enemy);
		} else { //that spot is probably in a wall, which would be weird, but possible
			for (Direction i:Move.allDirections) {
				if (hq.canMove(i)) {
					return HQ.add(away_from_enemy);
				}
			}
		}
		return HQpstr;
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
