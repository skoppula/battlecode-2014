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

	static boolean testing = false;
	
	static int pastrMaxRoundsAlive = 600;
	static int attackerMaxRoundsAlive = 600;
	static int attackerMaxRoundsAlive_Long = 800;
	static int numDefendersInSquad = 10;
	static int numDefendersInSquad_Redefeding = 10;
	static int numAttackersInSquad = 7;
	
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
    	
    	System.out.println("Desired PASTR positions, in order of increasing safety: " + Arrays.toString(desiredPASTRs));
    	
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
			if((job.ongoingPASTR && !isPASTRNearby(job.target, ourPASTRs)) 
					|| (job.type == 0 && !job.ongoingPASTR && maxedOutTime)) {
				job.prepareForRemoval(rc);
				safe[job.desiredPASTRs_index] = false; //mark that position as unsafe
				System.out.println("PASTR position " + desiredPASTRs[job.desiredPASTRs_index] + " deemed unsafe.");
				
				//Add replacement PASTR in safer position
				for(int j = 0; j < safe.length; j++) {
					if(safe[j] && !jobAlreadyTaken(desiredPASTRs[j])) {
						jobQueu.add(new Job(j, desiredPASTRs[j], numDefendersInSquad_Redefeding, getAvailableSquadNum("defense"), pastrMaxRoundsAlive));
						break;
					}
				}
				//Remove from queu after adding new Job, so that same squad number is not assigned
				jobQueu.remove(i);
				
			//else if job is offense and been taking too long to get attack
			} else if(job.type == 1 && maxedOutTime) {
				job.prepareForRemoval(rc);
				jobQueu.remove(i);
				
			}
		}
		
		//If all locations are marked as unsafe, retry all locations
		if(Util.areAllFalse(safe))
	    	for(int i = 0; i < safe.length; i++)
	    		safe[i] = true;
		
		
		//Check if jobs have established an ongoing PASTR; if so, mark it.
		for(int i = 0; i < jobQueu.size(); i++) {
			Job job = jobQueu.get(i);
			
			if(job.ongoingPASTR == false && isPASTRNearby(job.target, ourPASTRs)) {
				System.out.println(job + " just got marked as ONGOING!");
				job.ongoingPASTR = true;
			}
		}
		
		//Create new jobs in offense, rush, and defense/PASTR creation
		for(MapLocation enemyPASTR:enemyPASTRs) {
			if(!jobAlreadyTaken(enemyPASTR))
				if(enemyPASTRs.length > ourPASTRs.length - 1 || teamHQ.distanceSquaredTo(enemyPASTR) < 900) //If enemy PASTR is close by, add it to the front of the queu
					jobQueu.add(0, new Job(enemyPASTR, numAttackersInSquad, getAvailableSquadNum("offense"), attackerMaxRoundsAlive));
				else
					jobQueu.add(new Job(enemyPASTR, numAttackersInSquad, getAvailableSquadNum("offense"), attackerMaxRoundsAlive_Long));
		}

		if(initRush) {
			jobQueu.add(0, new Job(enemyHQ, 7, getAvailableSquadNum("offense"), attackerMaxRoundsAlive));
			initRush = false;
			
		} else {
			for(int i = 0; i < idealNumPastures; i++) {
				for(int j = 0; j < safe.length; j++) {
					if(safe[j] && !jobAlreadyTaken(desiredPASTRs[j])) {
						if(teamHQ.distanceSquaredTo(enemyHQ) > 900 && mapX > 30 && mapY > 30) { //If enemy is far and map is big, add it to the front of the queu 
							jobQueu.add(0, new Job(j, desiredPASTRs[j], numDefendersInSquad, getAvailableSquadNum("defense"), pastrMaxRoundsAlive));
							break;
					
						} else {
							jobQueu.add(new Job(j, desiredPASTRs[j], numDefendersInSquad, getAvailableSquadNum("defense"), pastrMaxRoundsAlive));
							break;
						}
					}
				}
			}
		}
		
		if(testing)
			for(Job job:jobQueu)
				System.out.print(job + " | ");
	}
	
	static private boolean isPASTRNearby(MapLocation target, MapLocation[] ourPASTRs) {
		for(MapLocation m:ourPASTRs) {
			if(target.distanceSquaredTo(m) < COWBOY.pastrDistCreationThreshold) {
				return true;
			}
		}
		
		return false;
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
		for(int i = start; i < Channels.lastOffenseChannel; i++) {
			if(!usedSquadNums.contains(i)) {
				return i;
			}
		}
	
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
					
					String type = squad < Channels.firstOffenseChannel ? "defender" : "attacker";
					System.out.println("Spawned a " + type + " with assignment " + assignment);
					return;
				}
			}
		}
		
	}

	static boolean tryToSpawn(RobotController rc) throws GameActionException {
		
		if(rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			Direction dir = Move.findDirToMove(rc);
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
			Xdiff = Xdiff == 0 ? 1 : Xdiff;
			Ydiff = Ydiff == 0 ? 1 : Ydiff;
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
		
		int numSafetyIntervals = 8;
	
		int[] squares = new int[mapX*mapY];
		double[] productivity = new double[mapX*mapY];
		double[] safetyRatios = new double[mapX*mapY];
		int index = 0;
		
		int scanResolution = 3;
		if(mapX*mapY < 400) {
			scanResolution = 1;
		} else if (mapX*mapY<900) {
			scanResolution = 2;
		}
		
		for(int i = 0; i < mapY-scanResolution; i+=scanResolution){
			for(int j = 0; j < mapX-scanResolution; j+=scanResolution){
				
				if((scanResolution == 1 || scanResolution == 2) && cowDensMap[i][j] == 0)
					continue;
				else if(scanResolution == 3 && cowDensMap[i+1][j+1] == 0)
					continue;
				
				double ratio = Math.sqrt(Math.pow((HQ.enemyHQ.x-(j+1)), 2) + Math.pow((HQ.enemyHQ.y-(i+1)), 2))
								/Math.sqrt(Math.pow((HQ.teamHQ.x-(j+1)), 2) + Math.pow((HQ.teamHQ.y-(i+1)), 2)); 
				
				//WE WANT THIS RATIO TO BE AS LARGE AS POSSIBLE
				double ratioThreshold = mapX*mapY > 400 ? 2 : 1;
				if(ratio < ratioThreshold)
					continue;
				
				double sum = 0; 
				if(scanResolution == 1) {
					sum = cowDensMap[i][j];
					
				} else if(scanResolution == 2) {
					sum = (cowDensMap[i][j] + cowDensMap[i+1][j] 
							+ cowDensMap[i][j+1] + cowDensMap[i+1][j+1]);
				
				} else {
					sum = (cowDensMap[i][j] + cowDensMap[i+1][j] + cowDensMap[i+2][j] 
							+ cowDensMap[i][j+1] + cowDensMap[i+1][j+1] + cowDensMap[i+2][j+1]
							+ cowDensMap[i][j+2] + cowDensMap[i+1][j+2] + cowDensMap[i+2][j+2]);
				}

				
				squares[index] = scanResolution == 1 ? Conversion.coordinatesToInt(j, i) : Conversion.coordinatesToInt(j+1, i+1);
				productivity[index] = sum;
				safetyRatios[index] = ratio;
				index++;
			}
		}
		
		while(index <= numSafetyIntervals) {
			numSafetyIntervals--;
		}
		
		MapLocation[] desiredPASTRs = new MapLocation[numSafetyIntervals];
		
		int[] copySquares = Arrays.copyOf(squares, index);
		double[] copyRatios = Arrays.copyOf(safetyRatios, index);
		double[] copyRatiosSorted = Arrays.copyOf(safetyRatios, index);
		double[] copyProds = Arrays.copyOf(productivity, index);
		
		Arrays.sort(copyRatiosSorted);
		//ratios are now sorted from least safe to safest
		double[] thresholds = new double[numSafetyIntervals];
		
		if(testing) {
			System.out.println(Arrays.toString(copySquares));
			System.out.println(Arrays.toString(copyRatios));
			System.out.println(Arrays.toString(copyRatiosSorted));
			System.out.println(Arrays.toString(copyProds));
			System.out.println(Arrays.toString(thresholds));
		}
		
		for(double j = 0; j < numSafetyIntervals; j++)
			thresholds[(int) j] = copyRatiosSorted[(int) j*copyRatiosSorted.length/numSafetyIntervals];
		
		double bestProd = 0;
		
		for(int i = thresholds.length - 1; i > -1 ; i--) {
			for(int j = 0; j < copyProds.length; j++){
				if(copyRatios[j] > thresholds[i]) {
					MapLocation m = Conversion.intToMapLocation(copySquares[j]);
					if(bestProd < copyProds[j] && !Arrays.asList(desiredPASTRs).contains(m)) {
						bestProd = copyProds[j];
						desiredPASTRs[i] = m;
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
