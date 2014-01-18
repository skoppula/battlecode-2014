package integrated;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class PASTR {


	static void runPastureCreator (RobotController rc, int squad) throws GameActionException {
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
		MapLocation loc = rc.getLocation();
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		int currX = (squadInfo/10000000), currY = (squadInfo/100000)%100;
		int x = (loc.x+currX)/2, y = (loc.y+currY)/2;
		
		if(enemyRobots.length>0){
			
			Util.moveToward(rc, new MapLocation(x, y)); //Regroup
			
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, loc); //SHOULD NOT OUTPUT AN HQ LOCATION
			
			int maxAttackRad = rc.getType().attackRadiusMaxSquared;
			
			if(rc.isActive() && eloc.distanceSquaredTo(rc.getLocation())<=maxAttackRad)
				rc.attackSquare(eloc);
			else if(rc.isActive() && rc.canMove(eloc.directionTo(loc)))
				rc.move(eloc.directionTo(loc));
			
		} else if(!((loc.x-targetX)+(loc.y-targetY)>3))
			Util.moveTo(rc, new MapLocation(targetX, targetY));
		
		else
			rc.yield();
		
	}

	public static void maintainPasture(RobotController rc) {
		

	}
}