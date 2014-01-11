package origrestructured;

import java.util.HashMap;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class COWBOY {
	
    static HashMap<Integer, Integer> defendPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
    static HashMap<Integer, Integer> roboEnemyAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in enemyPASTRs[] ]
    
    
    public static void runSoldier(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
    	

    	//System.out.println("SOLDIER SAYS THAT INITIALIZERRUN IS  " + HQ.initializerRun);
    	
    	int num = rc.sensePastrLocations(HQ.team).length;
    	int goalint =rc.readBroadcast(168+num);
    	
		MapLocation target = Util.intToLoc(goalint);
		
		if (goalint != 0) {
    		Util.moveTo(rc, target);
    		System.out.println(goalint);	
			//Util.randomMove(rc);
//    		for (MapLocation i:goals) {
//    			System.out.println("SKANDA FOUND THESE LOCATIONS: " + i);
//    		}
    	}
    	else {
    		Util.randomMove(rc);
    		System.out.println(goalint);		
    	}
	}
    
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
	
	
	static void shootNearby(RobotController rc) throws GameActionException {
		//shooting
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//if there are enemies
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(anEnemyInfo.location);
				}
			}
		}
	}

	
	
}
