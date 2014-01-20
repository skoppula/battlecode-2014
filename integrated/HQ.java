package integrated;

import java.util.Arrays;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQ {
	
	static boolean initializerRun = false;
    static double cowDensMap[][];
    static int mapY, mapX;
    static MapLocation desiredPASTRs[];
    static  Team team;
    static Team enemy;
    static int idealNumPastures = 2;
    static int storedCurrNumPastures = 0;
    static boolean constructNT = false;
	static MapLocation enemyHQ;
	static MapLocation teamHQ;
	static boolean rush = false;
    
    static int[] squads = new int[20];
    
    static int[][] terrainMap;
	final static int	NORMAL = 10;
	final static int	ROAD = 3;
	final static int	WALL = 1000;
	final static int	OFFMAP = 99999;
    
	static RobotController hq;
	static Random rand;

    static int[] robotTypeCount = {0,0};

	public static void runHeadquarters(RobotController rc) throws GameActionException {
		
		if(!initializerRun)
			initializeGameVars(rc);
		
		updateSquadLocs(rc);
		updateRobotDistro(rc);
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, team);
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, enemy);
		if(enemyRobots.length > 0)
			Util.indivShootNearby(rc, enemyRobots);
		else
			spawnRobot(rc);
		
		rc.yield();
	}
	
	public static void initializeGameVars(RobotController rc) throws GameActionException{
    	hq = rc;
    	
    	team =  hq.getTeam();
    	enemy = team.opponent();
    	cowDensMap = hq.senseCowGrowth();
    	mapY = cowDensMap.length;
    	mapX = cowDensMap[0].length;
    	idealNumPastures = computeNumPastures();
    	enemyHQ = rc.senseEnemyHQLocation();
    	teamHQ = rc.senseHQLocation();
    	createTerrainMap();
    	desiredPASTRs = findPastureLocs();
    	System.out.println("Desired pastures : " + Arrays.deepToString(desiredPASTRs));
    	
    	
    	initializerRun = true;
    	
    	rush = startRush(rc);
    	
    	rand = new Random(17);
    }
	
	//Put team and enemy team pasture, squad, and role info into channels
	static void updateSquadLocs(RobotController rc) throws GameActionException{
		//DEFENDER CHANNELS - 3 to about 8
		//format: [N][XXYY] where N is robot count in the squad and XXYY are coordinates
		
		//RUSH CHANNEL - 11
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		MapLocation rallyPoint = new MapLocation ((enemyHQ.x + 2*teamHQ.x)/3, (enemyHQ.y + 2*teamHQ.y)/3);
		if(rush && Clock.getRoundNum() < 1000)
			rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(rallyPoint));
		else if(enemyPASTRs.length>0)
			rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(enemyPASTRs[0]));
		
		for(int i = 0; i < enemyPASTRs.length; i++) {
			rc.broadcast(i+12, (rc.readBroadcast(i+12)/10000)*10000 + Util.locToInt(enemyPASTRs[i]));	
		}
		
		for(int i = 0; i < desiredPASTRs.length; i++)
			rc.broadcast(i+3, (rc.readBroadcast(i+3)/10000)*10000 + Util.locToInt(desiredPASTRs[i]));
		
	}
	
	static boolean startRush(RobotController rc){
		double mapDensity = findMapDensity();
		if(enemyHQ.distanceSquaredTo(teamHQ) < 900 && mapDensity < .5){
			System.out.println("START-OF-GAME RUSHING THE OTHER TEAM");
			return true;
		} else {
			System.out.println("STARTING ECONOMY DEVELOPMENT PREFERRED");
			return false;
		}
	}
	
	private static double findMapDensity() {
		//what's the map density
		//what's the wall count between HQ's?
		//how easy is it to navigate to enemyHQ? (guess? maybe?)
		int normal = 0;
		int wall = 0;
		int road = 0;
		int tileType;
		
		int Ystart = 0, Yend = 0, Xstart = 0, Xend=0;
		//only focus on range between HQ's
		if (teamHQ.y < enemyHQ.y) { //If our team HQ is north of enemy HQ
			Ystart = teamHQ.y;
			Yend = enemyHQ.y;
		} else {
			Ystart = enemyHQ.y;
			Yend = teamHQ.y+3;
		}
		if (teamHQ.x < enemyHQ.x) { //If our team HQ is left of enemy HQ
			Xstart = teamHQ.x;
			Xend = enemyHQ.x;
		} else {
			Xstart = enemyHQ.y;
			Xend = teamHQ.y;
		}
		
		for (int i=Ystart;i<Yend;i++) {
			for (int j=Xstart;j<Xend;j++) {
				tileType = terrainMap[j][i];
				if (tileType==NORMAL) {
					normal+=1;
				} else if (tileType==ROAD) {
					road+=1;
				} else if (tileType==WALL) {
					wall+=1;
				}
			}
		}
		double density = (double) (wall)/(normal+road+wall);
		
		return density;
		
	}

	//Keep track of deaths
	static void updateRobotDistro(RobotController rc) throws GameActionException{
		
		//Channel 1: distress: [SS][T][SS][T]...SS=squad, and T = type of distressed robots
		int in  = rc.readBroadcast(1);
		//int numRobots = (int) (Math.log10(in)+1)/3;
		int numRobots = String.valueOf(in).length()/3;
		
		//System.out.println(numRobots + "this is the casualty num");
		for(int i = 0; i < numRobots; i++){
			int j = (int) (in/Math.pow(1000,i))%1000;
			int type = j%10;
			int squad = j/10;
			//System.out.println(type + "and " + squad);
			
			//subtract from squad count signal and robot type count
			System.out.println("ROBOT DIED from SQUAD: " + squad);
			System.out.println(Arrays.toString(robotTypeCount));
			robotTypeCount[type]--;
			System.out.println(Arrays.toString(robotTypeCount));
			int k = rc.readBroadcast(squad);
			rc.broadcast(squad,(k/10000-1)*10000+k%10000);
		}
		
		//reset the distress channel
		rc.broadcast(1, 0);
		
		//System.out.println(in);
		
	}
	
	static void spawnRobot(RobotController rc) throws GameActionException{

		if(rc.senseRobotCount()<GameConstants.MAX_ROBOTS && rc.isActive()){
			
			int squad = nextSquadNum(rc);
			boolean spawnSuccess = false;
			Robot[] allies = rc.senseNearbyGameObjects(Robot.class, RobotType.SOLDIER.attackRadiusMaxSquared/2, team);
			
			if(squad > 10) {
				spawnSuccess = tryToSpawn(rc, 1);
				if(spawnSuccess) {
					int j = Util.assignmentToInt(squad, 1);
					rc.broadcast(0, j);
					System.out.println("Spawned an attacker: " + j);
				}
			
			} else if (squad < 11) {
				spawnSuccess = tryToSpawn(rc, 0);
				if(spawnSuccess){
					int j = Util.assignmentToInt(squad, 0);
					rc.broadcast(0, j);
					System.out.println("Spawned a defender: " + j);
				}
				
			}
			
			if(spawnSuccess){
				rc.broadcast(squad, rc.readBroadcast(squad)+10000);
			}
		}
	}
	
	//Determines squad of robot to by spawned next 
	private static int nextSquadNum(RobotController rc) throws GameActionException {
		
		//If it reads that defensive robots are dying from channel 10
		int squad = rc.readBroadcast(10);
		if(squad>0){
			rc.broadcast(10, 0); //reset value
			return squad;
		}
		
		//If starting out a rush, spawn enough attacking squads.
		if(rush && Clock.getRoundNum() < 1000) {
			for(int i = 11; i < 12; i++){
				if((rc.readBroadcast(i)/10000)%10<8)
					return i;
			}
		} 
		
		//Else if didn't establish pastures yet, need defensive squads
		for(int i = 3; i < 3+desiredPASTRs.length; i++){
			if((rc.readBroadcast(i)/10000)%10<6)
				return i;
		}
		
		//else spawn attackers
		for(int i = 11; i < 21; i++){
			if((rc.readBroadcast(i)/10000)%10<6)
				return i;
		}
		
		return 3;
	}

	static boolean tryToSpawn(RobotController rc, int type) throws GameActionException {
		if(rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			Direction dir = Util.findDirToMove(rc);
			if(dir != null) {
				rc.spawn(dir);
				robotTypeCount[type]++;
				return true;
			}
		}
		return false;
	}
	
	private static int computeNumPastures() {
		return 3; //If you increase this to 4 bad things happen
	}

	static MapLocation[] findPastureLocs() throws GameActionException {
		//returns a MapLocation array with the best pastures
		
		MapLocation pstrLocs[] = new MapLocation[idealNumPastures];
		double pstrCowDens[] = new double[idealNumPastures];
		
		//Fill default
		for (int i = 0; i < idealNumPastures; i++) {
			pstrLocs[i] = new MapLocation(mapX/2, mapY/2);
		}
		
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
				
				for(int k = 0; k < idealNumPastures; k++){
					
					//Balancing profit in pasture productivity vs. distance: (sum-weight/10)
					if((sum-weight/weight1)>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						
						pstrCowDens[k] = (sum-weight/weight1);
						break;
					}
				}
			}
		}
		//The first pasture will be right next to the HQ
		pstrLocs[0] = findHQpstr(pstrLocs[0]);

		System.out.println(pstrLocs[0] + "and" + pstrLocs[1]);
		
		return pstrLocs;
	}
		
	private static MapLocation findHQpstr(MapLocation origPstr) throws GameActionException {
		// returns the first pstr location, close to the HQ so it can be defended well
		MapLocation HQ = hq.senseHQLocation();
		MapLocation enemyHQ = hq.senseEnemyHQLocation();
		Direction toward_enemy = HQ.directionTo(enemyHQ);
		MapLocation HQpstr = origPstr;
		
		int r = hq.getType().sensorRadiusSquared;
		//System.out.println(r);
		
		for (Direction i:Util.allDirections) {
			MapLocation p = HQ.add(i, 10); //perimeter location
			System.out.println("map" + p.x + "" + p.y);
			if (p.x < 0 || p.x > mapX || p.y < 0 || p.y > mapY||i==toward_enemy)
				continue;
			else {
				int a = terrainMap[p.x][p.y];
				System.out.println(a);
				if (a==NORMAL||a==ROAD) {
					return p;
				}	
			}
			
		}
		
//		if (cowDensMap[test.x, test.y] > 1) { //check to see if that spot exists
//			HQpstr = HQ.add(away_from_enemy);
//		} else { //that spot is probably in a wall, which would be weird, but possible
//			
//		}
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
