package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Attack {
	//Shoots any *all* nearby robots: does not coordinate shooting with other robots
	static void indivShootNearby(RobotController rc, Robot[] enemyRobots) throws GameActionException {
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		for(Robot enemy:enemyRobots){
			if(rc.isActive()) {
				RobotInfo info = rc.senseRobotInfo(enemy);
				
				if(info.location.equals(enemyHQ))
					continue;
				
				if(info.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared && Clock.getBytecodeNum()<2000){
					rc.attackSquare(info.location);
				}
			}
		}
	}
	

	public static MapLocation nearestEnemyLoc(RobotController rc, Robot[] enemyRobots, MapLocation loc) throws GameActionException {
		
		int minDist = 100000;
		MapLocation bestLoc = null;
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		for(Robot robot:enemyRobots){
			RobotInfo info = rc.senseRobotInfo(robot);
			
			if(info.location.equals(enemyHQ))
				continue;
			
			if(!rc.canSenseObject(robot)){
				continue;
			} else {
				MapLocation m = info.location;
				int dist = m.distanceSquaredTo(loc);
				if(minDist > dist){
					minDist = dist;
					bestLoc = m;

				}
			}
		}
		
		return bestLoc;
	}
}
