package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class COWBOY {

	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
		
		int id = rc.getRobot().getID(); //ID will be unique to each soldier
		
		if (assignment==0) {
			assignment = rc.readBroadcast(id);
			System.out.println(assignment);
		}
		
		//Understand the assignment
		int team = Comm.getSquad(assignment);
		int role = Comm.getRole(assignment);
		MapLocation locTarget = Comm.getTargetLocation(rc, team);
		MapLocation attackTarget = Comm.getTargetLocation(rc, team);
		
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct
		
		switch(role){                              
	        case 0: COWBOY.runDefender(rc, locTarget, attackTarget); break;
	        case 1: COWBOY.runAttacker(rc, locTarget, attackTarget); break;
	        case 2: PASTR.runPastureCreator(rc, locTarget); break;
	        case 3: NOISE.runNoiseCreator(rc);
		}
	}

	private static void runAttacker(RobotController rc, MapLocation locTarget, MapLocation attackTarget) throws GameActionException {
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = Mapping.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = Mapping.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		} else {
			if(rc.getLocation().distanceSquaredTo(locTarget)>rc.getType().attackRadiusMaxSquared)
				Move.moveTo(rc, locTarget);
		}
	}

	private static void runDefender(RobotController rc, MapLocation locTarget, MapLocation attackTarget) throws GameActionException {
		//consider attacking
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(rc.isActive()&&enemyRobots.length>0){
			MapLocation[] enemyRobotLocations = Mapping.robotsToLocations(enemyRobots, rc, true);
			MapLocation closestEnemyLoc = Mapping.findClosest(enemyRobotLocations, rc.getLocation());
			if(rc.canAttackSquare(closestEnemyLoc))
				rc.attackSquare(closestEnemyLoc);
		} else {
			if(rc.getLocation().distanceSquaredTo(locTarget)>rc.getType().attackRadiusMaxSquared)
				Move.moveTo(rc, locTarget); //Move continuously to a rally point
		}
	}

}
