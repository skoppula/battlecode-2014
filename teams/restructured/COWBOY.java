package restructured;

import java.util.HashMap;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class COWBOY {
	
    static HashMap<Integer, Integer> defendPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
    static HashMap<Integer, Integer> roboEnemyAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in enemyPASTRs[] ]
    
	public static void runDefender(RobotController rc) {
		
		//1. move toward assigned pasture
		//2. if near pasture -> randomMove()
		//3. shootNearby()
		
		rc.yield();
	}
	
	public static void runAttacker(RobotController rc) {
		
		MapLocation currLoc = rc.getLocation();
		
		//move toward assigned pasture
		//if near pasture -> shoot pasture
		//else shoot anything nearby
		
		rc.yield();
	}
	
	private static void shootNearby(RobotController rc, MapLocation currLoc) throws GameActionException {
	
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, HQ.enemy);
		
		if(enemyRobots.length>0){
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			
			if(rc.isActive() && anEnemyInfo.location.distanceSquaredTo(currLoc)<rc.getType().attackRadiusMaxSquared)
				rc.attackSquare(anEnemyInfo.location);

		}
		
	}
	
}
