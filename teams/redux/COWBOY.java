package redux;

import redux.HQ.types;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class COWBOY {
	
	

	public static void runDefender(RobotController rc) {
		
		//1. move toward assigned pasture
		//2. if near pasture -> randomMove()
		//3. shootNearby()
		
		try {
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		try {
			MapLocation[] desiredPASTRs = Util.commToPSTRLocs(rc);
			
			if(rc.isActive()) {
				int id = rc.getRobot().getID();
				Util.shootNearby(rc);
				
				int idx = -1;
				for(int i = 26; i < 51; i++){
					int val = rc.readBroadcast(i);
					if((val-val%100)/100==id)
						idx = val%100;
				}
				
				if(idx>-1) {
						MapLocation target = desiredPASTRs[idx];
						rc.setIndicatorString(1, target.toString());
						
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
		
		try {
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		MapLocation[] enemyPASTRs = Util.commToEnemyPSTRLocs(rc);
		
		try {
			if(rc.isActive()){
				int id = rc.getRobot().getID();
				Util.shootNearby(rc);
				
				int idx = -1;
				for(int i = 26; i < 51; i++){
					int val = rc.readBroadcast(i);
					if((val-val%100)/100==id)
						idx = val%100;
				}
				
				if(idx > -1) {
						MapLocation target = enemyPASTRs[idx];
						rc.setIndicatorString(1, target.toString());
						
						if(target.x==-1)
							rc.selfDestruct();
				
						if(rc.getLocation().distanceSquaredTo(target) < 3)
							Util.shootNearby(rc);
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, enemyPASTRs[0]);
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
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
						rc.setIndicatorString(0, "Defender");
						
			    	} else {
			    		HQ.tempSpawnedType = types.ATTACKER;
			    		rc.broadcast(0, 1);
						HQ.robotTypeCount[1]++;
						System.out.println("Spawned attacker");
						rc.setIndicatorString(0, "Attacker");
			    	}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		try {
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
    }

	
	
}
