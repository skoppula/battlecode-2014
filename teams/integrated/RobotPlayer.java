package integrated;

//things to do:
//defend pastrs that are under attack, or at least consider defending them
//battlecry when charging into battle -> concerted effort
//something like the opposite of a battlecry, when you're sure you're outnumbered

import java.util.ArrayList;
import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	public static RobotController rc;
	public static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	public static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
	static int bigBoxSize = 5;
	static MapLocation enemyHQ;
	
	//HQ data:
	static MapLocation rallyPoint;
	static MapLocation targetedPastr;
	static boolean die = false;
	
	//SOLDIER data:
	static int myBand = 100;
	static int pathCreatedRound = -1;
    static boolean constructNT = false;
	
    
    
	public static void run(RobotController rcIn) throws GameActionException{
		
		rc=rcIn; //FLAGGED FOR REMOVAL
		Attacker.rc = rcIn; //FLAGGED FOR REMOVAL
		Defender.rc = rcIn; //FLAGGED FOR REMOVAL
		randall.setSeed(rc.getRobot().getID()); //FLAGGED FOR REMOVAL
		
		int id = rc.getRobot().getID();
    	RobotType type = rc.getType();
    	
		//read from channel 0: get squad and role
    	int assignment = rc.readBroadcast(0);

    	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
    	if(type != RobotType.HQ)
    		rc.broadcast(id, assignment);
		
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			//PASTR.maintainPasture(rc);
        			System.out.println("we have a pastr!");
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
        			COWBOY.runCowboy(rc, assignment);
        		}
        		
        		rc.yield();
        	}
		} catch (Exception e){
				e.printStackTrace();
		}
			
	}
	

	private static MapLocation findAverageAllyLocation(Robot[] alliedRobots) throws GameActionException {
		//find average soldier location
		MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(alliedRobots, rc, true);
		MapLocation startPoint;
		if(alliedRobotLocations.length>0){
			startPoint = VectorFunctions.meanLocation(alliedRobotLocations);
			if(Clock.getRoundNum()%100==0)//update rally point from time to time
				rallyPoint=startPoint;
		}else{
			startPoint = rc.senseHQLocation();
		}
		return startPoint;
	}

	private static MapLocation getNextTargetPastr(MapLocation[] enemyPastrs,MapLocation startPoint) {
		if(enemyPastrs.length==0)
			return null;
		if(targetedPastr!=null){//a targeted pastr already exists
			for(MapLocation m:enemyPastrs){//look for it among the sensed pastrs
				if(m.equals(targetedPastr)){
					return targetedPastr;
				}
			}
		}//if the targeted pastr has been destroyed, then get a new one
		return VectorFunctions.findClosest(enemyPastrs, startPoint);
	}

	public static void tryToSpawn() throws GameActionException {
		if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
	
	private static void runSoldier() throws GameActionException {
		//follow orders from HQ
		
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());//was 
		if(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			if(enemyRobotLocations.length==0){//only HQ is in view
				//navigateByPath(alliedRobots);
				Util.moveTo(rc, rc.senseEnemyHQLocation());
			}else{//shootable robots are in view
				MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
				if((alliedRobots.length+1)>=enemyRobots.length){//attack when you have superior numbers
					attackClosest(closestEnemyLoc);
				}else{//otherwise regroup
					regroup(enemyRobots,alliedRobots,closestEnemyLoc);
				}
			}
		}else{//NAVIGATION BY DOWNLOADED PATH
			//navigateByPath(alliedRobots);
			
			MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
			if(enemyPastrs.length>0){
				MapLocation startPoint = findAverageAllyLocation(alliedRobots);
				targetedPastr = getNextTargetPastr(enemyPastrs,startPoint);
				//broadcast it
				System.out.println("Move to " + targetedPastr);
				Util.moveTo(rc, targetedPastr);
			}
		}
	}

	private static void regroup(Robot[] enemyRobots, Robot[] alliedRobots,MapLocation closestEnemyLoc) throws GameActionException {
		int enemyAttackRangePlusBuffer = (int) Math.pow((Math.sqrt(rc.getType().attackRadiusMaxSquared)+1),2);
		if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=enemyAttackRangePlusBuffer){//if within attack range, back up
			Direction awayFromEnemy = rc.getLocation().directionTo(closestEnemyLoc).opposite();
			BasicPathing.tryToMove(awayFromEnemy, true,true,false);
		}else{//if outside attack range, group up with allied robots
			MapLocation[] alliedRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc,false);
			MapLocation alliedRobotCenter = VectorFunctions.meanLocation(alliedRobotLocations);
			Direction towardAllies = rc.getLocation().directionTo(alliedRobotCenter);
			BasicPathing.tryToMove(towardAllies, true,true, false);
		}
	}

	private static void attackClosest(MapLocation closestEnemyLoc) throws GameActionException {
		//attacks the closest enemy or moves toward it, if it is out of range
		if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared){//close enough to shoot
			if(rc.isActive()){
				rc.attackSquare(closestEnemyLoc);
			}
		}else{//not close enough to shoot, so try to go shoot
			Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
			//simpleMove(towardClosest);
			BasicPathing.tryToMove(towardClosest, true,true, false);
		}
	}

	
}
