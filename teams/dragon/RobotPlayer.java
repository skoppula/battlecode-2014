package dragon;

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
		rc=rcIn;
		Attacker.rc = rcIn;
		Defender.rc = rcIn;
		randall.setSeed(rc.getRobot().getID());
		enemyHQ = rc.senseEnemyHQLocation();
		
		int id = rc.getRobot().getID();
    	RobotType type = rc.getType();
    	
		//read from channel 0: get squad and role
    	int assignment = rc.readBroadcast(0);

    	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
    	if(type != RobotType.HQ)
    		rc.broadcast(id, assignment);
    	else {
    		//spawns a robot on the first round and makes sure that it's a defender on squad 3
    		Move.tryToSpawn(rc);
    		rc.broadcast(0, 300);
    	}
		
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        			//runHQ();
        		
        		else if (type == RobotType.PASTR)
        			//PASTR.maintainPasture(rc);
        			System.out.println("we have a pastr!");
        		
        		else if(type == RobotType.NOISETOWER)
        			//NOISE.maintainNoiseTower(rc);
        			System.out.println("we have a noisetower!");
        		
        		else {
        			COWBOY.runCowboy(rc, assignment);
        			
        		}
        		
        		rc.yield();
        	}
		}catch (Exception e){
				e.printStackTrace();
		}
			
	}
	
	
	private static void runHQ() throws GameActionException {
		//TODO consider updating the rally point to an allied pastr 

		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000000,rc.getTeam());
		
		
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		}
		
		//consider spawning
		tryToSpawn();
		rc.broadcast(0, 300);
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
				Move.moveTo(rc, rc.senseEnemyHQLocation());
			}else{//shootable robots are in view
				MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
				boolean closeEnoughToShoot = closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared;
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
				Move.moveTo(rc, targetedPastr);
			}
		}
	}

	private static void considerBuildingPastr(Robot[] alliedRobots) throws GameActionException {
		if(alliedRobots.length>4){//there must be allies nearby for defense
			MapLocation[] alliedPastrs =rc.sensePastrLocations(rc.getTeam());
			if(alliedPastrs.length<5&&(rc.readBroadcast(50)+60<Clock.getRoundNum())){//no allied robot can be building a pastr at the same time
				for(int i=0;i<20;i++){
					MapLocation checkLoc = VectorFunctions.mladd(rc.getLocation(),new MapLocation(randall.nextInt(8)-4,randall.nextInt(8)-4));
					if(rc.canSenseSquare(checkLoc)){
						double numberOfCows = rc.senseCowsAtLocation(checkLoc);
						if(numberOfCows>1000){//there must be a lot of cows there
							if(alliedPastrs.length==0){//there must not be another pastr nearby
								buildPastr(checkLoc);
							}else{
								MapLocation closestAlliedPastr = VectorFunctions.findClosest(alliedPastrs, checkLoc);
								if(closestAlliedPastr.distanceSquaredTo(checkLoc)>GameConstants.PASTR_RANGE*5){
									buildPastr(checkLoc);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void buildPastr(MapLocation checkLoc) throws GameActionException {
		rc.broadcast(50, Clock.getRoundNum());
		for(int i=0;i<100;i++){//for 100 rounds, try to build a pastr
			if(rc.isActive()){
				if(rc.getLocation().equals(checkLoc)){
					rc.construct(RobotType.PASTR);
					constructNT = true;
				}else{
					Direction towardCows = rc.getLocation().directionTo(checkLoc);
					BasicPathing.tryToMove(towardCows, true,true, true);
				}
			}
			rc.yield();
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

	private static MapLocation getRandomLocation() {
		return new MapLocation(randall.nextInt(rc.getMapWidth()),randall.nextInt(rc.getMapHeight()));
	}

	private static void simpleMove(Direction chosenDirection) throws GameActionException{
		if(rc.isActive()){
			for(int directionalOffset:directionalLooks){
				int forwardInt = chosenDirection.ordinal();
				Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
				if(rc.canMove(trialDir)){
					rc.move(trialDir);
					break;
				}
			}
		}
	}
	
}