package lotus;

import java.util.ArrayList;
import java.util.Arrays;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class HQ {
	
	static RobotController hq;
	static boolean initializerRun = false;
    static double cowDensMap[][];
    static int mapY, mapX;
    static  Team team;
    static Team enemy;
    
    static MapLocation[] desiredPASTRs;
    static boolean[] safe;
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
		
		updateJobQueu(rc);
		
		for(Job job:jobQueu)
			job.updateSquadChannel(rc);
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, enemy);
		
		if(enemyRobots.length > 0)
			Attack.indivShootNearby(rc, enemyRobots);
		else
			spawnRobot(rc);
		
		rc.yield();
	}
	
	static void initializeGameVars(RobotController rc) throws GameActionException{
    	
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
    	safe = new boolean[desiredPASTRs.length];
    	
    	for(int i = 0; i < safe.length; i++)
    		safe[i] = true;
    	
    	initRush = startRush(rc);
    	
    	initializerRun = true;
    }
	
	static void updateJobQueu(RobotController rc) throws GameActionException{
		
		MapLocation[] ourPASTRs = rc.sensePastrLocations(team);
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		
		//Remove ongoing Jobs that need deleting
		for(int i = 0; i < jobQueu.size(); i++){
			
			Job job = jobQueu.get(i);
			boolean maxedOutTime = Clock.getRoundNum() > jobQueu.get(i).maxJobLength + jobQueu.get(i).startRound;
			
			//[If an ongoing PASTR reported, but none exists -> assume PASTR died] or [defense job without a PASTR took too long -> assume hit obstacle]
			if((job.ongoingPASTR && !Arrays.asList(ourPASTRs).contains(job.target)) 
					|| (job.type == 0 && !job.ongoingPASTR && maxedOutTime)) {
				job.prepareForRemoval(rc);
				safe[job.desiredPASTRs_index] = false; //mark that position as unsafe
				
				//Add replacement PASTR in safer position
				for(int j = 0; j < safe.length; j++)
					if(safe[j] && !jobAlreadyTaken(desiredPASTRs[j]))						
						jobQueu.add(new Job(j, desiredPASTRs[j], 5, getAvailableSquadNum("defense"), 400));
				
				//Remove from queu after adding new Job, so that same squad number is not assigned
				jobQueu.remove(i);
				
			//else if job is offense and been taking too long to get attack
			} else if(job.type == 1 && maxedOutTime) {
				job.prepareForRemoval(rc);
				jobQueu.remove(i);
				
			}
		}
		
		//Check if jobs have established an ongoing PASTR; if so, mark it.
		for(int i = 0; i < jobQueu.size(); i++) {
			Job job = jobQueu.get(i);
			if(Arrays.asList(ourPASTRs).contains(job.target))
				job.ongoingPASTR = true;
		}
		
		//Create new jobs in offense, rush, and defense/PASTR creation
		if(enemyPASTRs.length > ourPASTRs.length - 1) {
			for(MapLocation enemyPASTR:enemyPASTRs) {
				if(!jobAlreadyTaken(enemyPASTR))
					if(teamHQ.distanceSquaredTo(enemyPASTR) < 900) //If enemy PASTR is close by, add it to the front of the queu
						jobQueu.add(0, new Job(enemyPASTR, 6, getAvailableSquadNum("offense"), 450));
					else
						jobQueu.add(new Job(enemyPASTR, 6, getAvailableSquadNum("offense"), 450));
			}
		}

		if(initRush) {
			jobQueu.add(0, new Job(enemyHQ, 7, getAvailableSquadNum("offense"), 500));
			initRush = false;
			
		} else {
			while(numDefenseJobs() < idealNumPastures) {
				for(int j = 0; j < safe.length; j++)
					if(safe[j] && !jobAlreadyTaken(desiredPASTRs[j]))
						if(teamHQ.distanceSquaredTo(enemyHQ) > 900 && mapX > 30 && mapY > 30) //If enemy is far and map is big, add it to the front of the queu 
							jobQueu.add(0, new Job(j, desiredPASTRs[j], 4, getAvailableSquadNum("defense"), 350));
						else
							jobQueu.add(new Job(j, desiredPASTRs[j], 4, getAvailableSquadNum("defense"), 350));
							
			}
		}
	}
	
	static int numDefenseJobs() {
		int count = 0;
		for(Job job:jobQueu)
			if(job.type == 0)
				count++;
		
		
		return count;
	}
	
	static boolean jobAlreadyTaken(MapLocation m) {
		for(Job job:jobQueu)
			if(job.target.equals(m))
				return true;
	
		return false;
	}

	private static int getAvailableSquadNum(String type) {
		
		ArrayList<Integer> usedSquadNums = new ArrayList<Integer>();
		
		for(Job job:jobQueu) {
			usedSquadNums.add(job.squadNum);
			usedSquadNums.add(job.NTPASTRchannel);
		}
		
		int start = type.equals("defense") ? Channels.firstDefenseChannel : Channels.firstOffenseChannel;
		for(int i = start; start < Channels.lastOffenseChannel; start++)
			if(!usedSquadNums.contains(i))
				return i;
		
		return Channels.firstDefenseChannel;
	}
	
	static void spawnRobot(RobotController rc) throws GameActionException{

		for(Job job:jobQueu) {
			
			if(job.numRobotsAssigned < job.numRobotsNeeded) {
				
				boolean spawnSuccess = false;
				int squad = job.squadNum;
				
				spawnSuccess = tryToSpawn(rc);
				if(spawnSuccess) {
					int assignment = squad < Channels.firstOffenseChannel ? Channels.assignmentEncoding(squad, 0) : Channels.assignmentEncoding(squad, 1);
					rc.broadcast(Channels.spawnChannel, assignment);
					
					job.addRobotAssigned(1);
					
					String type = squad < Channels.firstOffenseChannel ? "attacker" : "defender";
					System.out.println("Spawned a " + type + " with assignment " + assignment);
					return;
				}
			}
		}
		
	}

	static boolean tryToSpawn(RobotController rc) throws GameActionException {
		
		if(rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			Direction dir = Util.findDirToMove(rc);
			if(dir != null) {
				rc.spawn(dir);
				return true;
			}
		}
		
		return false;
	}

	static boolean startRush(RobotController rc) throws GameActionException{
		
		int[] scoutResults = Channels.scoutDecoding(rc.readBroadcast(Channels.scoutChannel));
		
		if((scoutResults[2] == 1 && scoutResults[0] < 50) || (enemyHQ.distanceSquaredTo(teamHQ) < 1000 && getDiagonalDensity() <.1)) {
			System.out.println("Deciding on rush ...");
			return true;
			
		} else {
			System.out.println("Deciding economy development ...");
			return false;
		}
	}

	private static double getDiagonalDensity() {
		
		double normal = 0, wall = 0;
		
		int Ystart = teamHQ.y < enemyHQ.y ? teamHQ.y : enemyHQ.y;
		int Yend = teamHQ.y < enemyHQ.y ? enemyHQ.y : teamHQ.y;
		int Xstart = teamHQ.x < enemyHQ.x ? teamHQ.x : enemyHQ.x;
		int Xend = teamHQ.x < enemyHQ.x ? enemyHQ.x : teamHQ.x;
		
		int Xdiff = (Xend-Xstart), Ydiff = (Yend-Ystart);
		boolean Xlarger = Xdiff > Ydiff;
		int stepSize = Xlarger ? Xdiff/Ydiff : Ydiff/Xdiff;
		
		//Traverse along the diagonal
		if(Xlarger) {
			for(int x = Xstart, y = Ystart; x < Xend; x += stepSize, y++)
				for(int j = 0; j < stepSize; j++){
					if(terrainMap[x+j][y] == NORMAL || terrainMap[x+j][y] == ROAD)
						normal++;
					else
						wall++;
				}
			
		} else {
			for(int x = Xstart, y = Ystart; y < Yend; y += stepSize, x++)
				for(int j = 0; j < stepSize; j++){
					if(terrainMap[x][y+j] == NORMAL || terrainMap[x][y+j] == ROAD)
						normal++;
					else
						wall++;
				}
		}

		return (wall)/(normal+wall);
		
	}
	
	static MapLocation[] findPastureLocs() throws GameActionException {
		
		int numSafetyIntervals = 5;
		MapLocation[] desiredPASTRs = new MapLocation[numSafetyIntervals];
				
		int[] squares = new int[mapX*mapY];
		double[] productivity = new double[mapX*mapY];
		double[] safetyRatios = new double[mapX*mapY];
		int index = 0;
		
		for(int i = 0; i < mapY-3; i+=3){
			for(int j = 0; j < mapX-3; j+=3){
				
				if(cowDensMap[i+1][j+1] == 0)
					continue;
				
				double ratio = (Math.pow((HQ.enemyHQ.x-(j+1)), 2) + Math.pow((HQ.enemyHQ.y-(i+1)), 2))
								/(Math.pow((HQ.teamHQ.x-(j+1)), 2) + Math.pow((HQ.teamHQ.y-(i+1)), 2)); 
				
				if(ratio > 1)
					continue;
						
				double sum = (cowDensMap[i][j] + cowDensMap[i+1][j] + cowDensMap[i+2][j] 
							+ cowDensMap[i][j+1] + cowDensMap[i+1][j+1] + cowDensMap[i+2][j+1]
							+ cowDensMap[i][j+2] + cowDensMap[i+1][j+2] + cowDensMap[i+2][j+2]);
				
				squares[index] = Conversion.coordinatesToInt(j+1, i+1);
				productivity[index] = sum;
				safetyRatios[index] = ratio;
				index++;
			}
		}
		
		int[] copySquares = Arrays.copyOf(squares, index);
		double[] copyRatios = Arrays.copyOf(safetyRatios, index);
		double[] copyRatiosSorted = Arrays.copyOf(safetyRatios, index);
		double[] copyProds = Arrays.copyOf(safetyRatios, index);
		
		Arrays.sort(copyRatiosSorted);
		double[] thresholds = new double[numSafetyIntervals-1];
		
		for(double j = 1; j < numSafetyIntervals; j++)
			thresholds[(int)j-1] = copyRatiosSorted[(int) j*copyRatiosSorted.length/numSafetyIntervals];
		
		double bestProd = 0;
		
		for(int i = 0; i < thresholds.length; i++) {
			for(int j = 0; j < copyProds.length; j++){
				if(i == 0 && copyRatios[j] < thresholds[i]) {
					if(bestProd < copyProds[j]) {
						bestProd = copyProds[j];
						desiredPASTRs[i] = Conversion.intToMapLocation(copySquares[j]);
					}		
				} else if (i == thresholds.length && copyRatios[j] > thresholds[i-1]){
					if(bestProd < copyProds[j]) {
						bestProd = copyProds[j];
						desiredPASTRs[i] = Conversion.intToMapLocation(copySquares[j]);
					}
				} else if (copyRatios[j] < thresholds[i] && copyRatios[j] > thresholds[i-1]){
					if(bestProd < copyProds[j]) {
						bestProd = copyProds[j];
						desiredPASTRs[i] = Conversion.intToMapLocation(copySquares[j]);
					}
				}
			}
			
			bestProd = 0;
		}
		
		return desiredPASTRs;
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
