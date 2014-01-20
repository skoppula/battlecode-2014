package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PASTR {

	public static void runPastureCreator(RobotController rc, MapLocation pstrLoc) throws GameActionException {
		// TODO Auto-generated method stub
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = Mapping.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = Mapping.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		} else {
			if(rc.getLocation().distanceSquaredTo(pstrLoc)>rc.getType().attackRadiusMaxSquared)
				Move.moveTo(rc, pstrLoc);
		}
		System.out.println("PASTURES STILL EXISTS");
		//Consider building a pasture
		MapLocation currLoc = rc.getLocation();
		if (surroundingCows(rc, currLoc) > 0&&rc.isActive()) {
			rc.construct(RobotType.PASTR);
		} else {
			//tell HQ that this location is BAD
		}
		
	}

	private static int surroundingCows(RobotController rc, MapLocation currLoc) throws GameActionException {
		// TODO Auto-generated method stub
		MapLocation[] surrLocs = MapLocation.getAllMapLocationsWithinRadiusSq(currLoc, 20);
		int count = 0;
		for (MapLocation i:surrLocs) {
			if (rc.canSenseSquare(i)) {
				count += rc.senseCowsAtLocation(i);
			}
		}
		return count;
	}

	public static void maintainPasture(RobotController rc) {
		// TODO Auto-generated method stub
		
	}

}
