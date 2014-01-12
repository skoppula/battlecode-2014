package origrestructured;

import origrestructured.HQ.types;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class COWBOY {
	
	

	public static void runDefender(RobotController rc) {
		
		//1. move toward assigned pasture
		//2. if near pasture -> randomMove()
		//3. shootNearby()
		
		try {
			MapLocation[] desiredPASTRs = Util.commToPSTRLocs(rc);
			
			if(rc.isActive()) {
				int id = rc.getRobot().getID();
				shootNearby(rc);
				
				int idx = -1;
				for(int i = 26; i < 51; i++){
					int val = rc.readBroadcast(i);
					if((val-val%100)/100==id)
						idx = val%100;
				}
				
				if(idx>-1) {
						MapLocation target = desiredPASTRs[idx];
				
						if(rc.getLocation().distanceSquaredTo(target) < 3)
							Util.randomMove(rc);
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, desiredPASTRs[0]);
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void runAttacker(RobotController rc) {
		
		//move toward assigned pasture
		//if near pasture -> shoot pasture
		//else shoot anything nearby
		
		MapLocation[] enemyPASTRs = Util.commToEnemyPSTRLocs(rc);
		
		try {
			if(rc.isActive()){
				int id = rc.getRobot().getID();
				shootNearby(rc);
				
				int idx = -1;
				for(int i = 26; i < 51; i++){
					int val = rc.readBroadcast(i);
					if((val-val%100)/100==id)
						idx = val%100;
				}
				
				if(idx > -1) {
						MapLocation target = enemyPASTRs[idx];
				
						if(rc.getLocation().distanceSquaredTo(target) < 3)
							shootNearby(rc);
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, enemyPASTRs[0]);
				
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
			    		rc.broadcast(0, 0);
						HQ.robotTypeCount[0]++;
						System.out.println("Spawned defender");
						
			    	} else {
			    		HQ.tempSpawnedType = types.ATTACKER;
			    		rc.broadcast(0, 1);
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
