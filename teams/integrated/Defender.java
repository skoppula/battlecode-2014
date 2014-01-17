package integrated;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Defender {
	

	static RobotController rc;
	static boolean constructNT = false;
	static Random randall;
	
	public static void runDefender(RobotController rc, int squad, int role) throws GameActionException {
		
		//1. move toward assigned pasture
		//2. if near pasture -> randomMove()
		//3. shootNearby()
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());//was
		int pstrint = rc.readBroadcast(squad);
		MapLocation target = Comms.intToLoc(pstrint);
		System.out.println("Desired pastures are " + target );
		System.out.println(squad + "running defender as " + role);
		
		
		if(rc.isActive()) {
			//defense first
			Util.shootNearby(rc);
			
			//try to move toward desired pasture
			MapLocation loc = rc.getLocation();
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			Direction betterDefense = loc.directionTo(enemyHQ);
			if(rc.getLocation().distanceSquaredTo(target) < 25) {
				//consider building a pasture
				//DON'T MOVE IT TAKES ACTIONDELAY
				//System.out.println("defender standing by");
				
				//if it's safe, build a pasture
				considerBuildingPastr(alliedRobots);
				
				
				
				//if a pasture is build, build a noisetower
				
				
			}
			else
				Util.moveTo(rc, target);
		}
		
	}
	
	private static void considerBuildingPastr(Robot[] alliedRobots) throws GameActionException {
		if(alliedRobots.length>4){//there must be allies nearby for defense
			MapLocation[] alliedPastrs =rc.sensePastrLocations(rc.getTeam());
			if(alliedPastrs.length<5&&(rc.readBroadcast(9)+60<Clock.getRoundNum())){//no allied robot can be building a pastr at the same time
				MapLocation loc = rc.getLocation();
				double numberOfCows = rc.senseCowsAtLocation(loc);
				if(numberOfCows>10){//there must be at least some cows there
					if(alliedPastrs.length==0){//there must not be another pastr nearby
						buildPastr(loc);
					}
//					else{
//						MapLocation closestAlliedPastr = VectorFunctions.findClosest(alliedPastrs, checkLoc);
//						if(closestAlliedPastr.distanceSquaredTo(checkLoc)>GameConstants.PASTR_RANGE*5){
//							buildPastr(loc);
//						}
//					}
				}
			}
		}
	}

	private static void buildPastr(MapLocation checkLoc) throws GameActionException {
		rc.broadcast(9, Clock.getRoundNum());
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
	
}
