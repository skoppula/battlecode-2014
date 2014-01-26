package lotus;

import java.util.ArrayList;
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
	
	static RobotController hq;
	static boolean initializerRun = false;
    static double cowDensMap[][];
    static int mapY, mapX;
    static  Team team;
    static Team enemy;
    
    static ArrayList<MapLocation> desiredPASTRs;
    static int idealNumPastures = 2;

	static MapLocation enemyHQ;
	static MapLocation teamHQ;
	static boolean initRush = false;
	static boolean attackedEnemy = false;
    
    static int[] squads = new int[20];
    
    static int[][] terrainMap;
	final static int	NORMAL = 10;
	final static int	ROAD = 3;
	final static int	WALL = 1000;
	final static int	OFFMAP = 99999;
	
	static ArrayList<Job> jobQueu = new ArrayList<Job>();

	public static void runHeadquarters(RobotController rc) throws GameActionException {
		
		if(!initializerRun)
			initializeGameVars(rc);
		
		updateJobQueu();
		updateSquadLocs(rc);
		updateRobotDistro(rc);
		
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
    	
    	enemyHQ = rc.senseEnemyHQLocation();
    	teamHQ = rc.senseHQLocation();
    	terrainMap = createTerrainMap();
    	desiredPASTRs = findPastureLocs();
    	initRush = startRush(rc);
    	
    	initializerRun = true;
    }
	
	//Put team and enemy team pasture, squad, and role info into channels
	static void updateSquadLocs(RobotController rc) throws GameActionException{
		//DEFENDER CHANNELS - 3 to about 8
		//format: [N][XXYY] where N is robot count in the squad and XXYY are coordinates
		
		//RUSH CHANNEL - 11
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		MapLocation rallyPoint = determineRallyPoint(rc);
		
		//TODO surround enemy HQ - rush ENDGAME :)
		if (rc.readBroadcast(Util.rushSuccess) > 0){
			rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(HQ.enemyHQ));
		}
		
		else if(rush && Clock.getRoundNum() < 1000){
			//System.out.println("rush and under 1000");
			if(enemyPASTRs.length>0){
				rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(enemyPASTRs[0]));
				attackedEnemy = true;
			}
			else if (attackedEnemy && enemyPASTRs.length == 0){ //shut down headquarters and endgame
				rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(rc.senseEnemyHQLocation()));
			}
			else
				rc.broadcast(11, (rc.readBroadcast(11)/10000)*10000 + Util.locToInt(rallyPoint));
		}
		
		for(int i = 0; i < enemyPASTRs.length; i++) {
			if (enemyPASTRs[i].distanceSquaredTo(rc.senseEnemyHQLocation()) < 36) {
				//the pastr is untouchable
				rc.broadcast(i+12, (rc.readBroadcast(i+12)/10000)*10000 + Util.locToInt(rallyPoint));
			} else {
				rc.broadcast(i+12, (rc.readBroadcast(i+12)/10000)*10000 + Util.locToInt(enemyPASTRs[i]));
			}	
		}
		
		for(int i = 0; i < desiredPASTRs.length; i++) {
			rc.broadcast(i+3, (rc.readBroadcast(i+3)/10000)*10000 + Util.locToInt(desiredPASTRs[i]));
			//System.out.println("SQUAD TRACKER " + (i+3));
		}
	}

	static boolean startRush(RobotController rc) throws GameActionException{
		// TODO How hard is it to rush the map? Can the enemy reach us in less than 30 turns?
		double mapDensity = findMapDensity();
		if(enemyHQ.distanceSquaredTo(teamHQ) < 900 || mapDensity <.1){
			System.out.println("START-OF-GAME RUSHING THE OTHER TEAM");
			return true;
		}
		else {
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
			Yend = teamHQ.y;
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
		int in  = rc.readBroadcast(Util.distress);
		//System.out.println("DISTRESS BROADCASTS: " + in);
		int numRobots = ("0" + String.valueOf(in)).length()/3; //Must append a 0 to front of string to process so that numRobots works out correctly
		//System.out.println(numRobots + "this is the casualty num");
		for(int i = 0; i < numRobots; i++){ //so this never gets iterated through...
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
		rc.broadcast(Util.distress, 0);
		
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
					rc.broadcast(Util.spawnchannel, j);
					System.out.println("Spawned an attacker: " + j);
				}
			
			} else if (squad < 11) {
				spawnSuccess = tryToSpawn(rc, 0);
				if(spawnSuccess){
					int j = Util.assignmentToInt(squad, 0);
					rc.broadcast(Util.spawnchannel, j);
					System.out.println("Spawned a defender: " + j);
				}
			}
			
			//Increase the squad member count by one
			if(spawnSuccess){
				rc.broadcast(squad, rc.readBroadcast(squad)+10000);
			}
		}
	}
	
	//Determines squad of robot to by spawned next 
	private static int nextSquadNum(RobotController rc) throws GameActionException {
		//If it reads that defensive robots are dying from channel 10
		int squad = rc.readBroadcast(Util.spawnNext);
		if(squad!=0 && squad < 11 && !rush){
			rc.broadcast(Util.spawnNext, 0); //reset value
			System.out.println("spawning a replacement for defender" + squad);
			return squad;
		}
		
		//If starting out a rush, spawn enough attacking squads.
		boolean rushFailed = false; //temporary hot fix - later this should be a checkpoint
		int rushRetreat = computeRushRetreat(rc); 
		if(rush && Clock.getRoundNum() < rushRetreat && !rushFailed) { //TODO decide when to stop rush
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
		
		//include scout!!!
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

	static ArrayList<MapLocation> findPastureLocs() throws GameActionException {
		
		int numSafetyIntervals = 5;
		int[] desiredPASTRs = new int[numSafetyIntervals];
				
		MapLocation[] squares = new MapLocation[mapX*mapY/2];
		int[] 
 
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
					if (sum==0){
						pstrCowDens[k] = 0;
					}
					else if((sum-weight/weight1)>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						
						pstrCowDens[k] = (sum-weight/weight1);
						break;
					}
				}
			}
		}

		return pstrLocs;
	}
		
	public static int[][] createTerrainMap(){

		//Initialize terrain map array
		int[][] terrainMap = new int[mapY][mapX]; 

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
		
		return terrainMap;
	}

}
