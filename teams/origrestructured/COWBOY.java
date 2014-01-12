package origrestructured;

import java.util.HashMap;

import origrestructured.HQ.types;
import battlecode.common.Direction;
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
		
		try {
			if(rc.isActive()) {
				int id = rc.getRobot().getID();
				shootNearby(rc);
				
				if(defendPSTRsAssignment.containsKey(id)) {
						MapLocation target = HQ.desiredPASTRs[defendPSTRsAssignment.get(id)];
				
						if(rc.getLocation().distanceSquaredTo(target) < 3)
							Util.randomMove(rc);
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, HQ.desiredPASTRs[0]);
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void runAttacker(RobotController rc) {
		
		//move toward assigned pasture
		//if near pasture -> shoot pasture
		//else shoot anything nearby
		
		try {
			if(rc.isActive()){
				int id = rc.getRobot().getID();
				shootNearby(rc);
				
				if(roboEnemyAssignment.containsKey(id)) {
						MapLocation target = HQ.enemyPASTRs.get(roboEnemyAssignment.get(id));
				
						if(rc.getLocation().distanceSquaredTo(target) < 3)
							shootNearby(rc);
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, HQ.enemyPASTRs.get(0));
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
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
	
    public static void spawnCOWBOY(RobotController rc, HQ.types type){
			
		for (Direction i:Util.allDirections) {
			if(rc.canMove(i)) {
				try {
					rc.spawn(i);
					if(type==HQ.types.DEFENDER) {
			    		HQ.tempSpawnedType = types.DEFENDER;
						HQ.robotTypeCount[0]++;
						System.out.println("Spawned defender");
						
			    	} else {
			    		HQ.tempSpawnedType = types.ATTACKER;
						HQ.robotTypeCount[1]++;
						System.out.println("Spawned attacker");
			    	}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				break;
			}
		}
    }

	
	
}
