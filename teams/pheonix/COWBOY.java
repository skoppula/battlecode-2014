package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class COWBOY {

	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
		
		int id = rc.getRobot().getID(); //ID will be unique to each soldier
		
		//Understand the assignment
		int team = Comm.getSquad(assignment);
		int role = Comm.getRole(assignment);
		MapLocation locTarget = Comm.getTargetLocation(rc, team);
		MapLocation attackTarget = Comm.getTargetLocation(rc, team);
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct
		
		switch(Comm.getType(role)){                              
	        case 0: COWBOY.runDefender(rc, locTarget, attackTarget); break;
	        case 1: COWBOY.runAttacker(rc, locTarget, attackTarget); break;
	        case 2: PASTR.runPastureCreator(rc, locTarget, attackTarget); break;
	        case 3: NOISE.runNoiseCreator(rc, locTarget, attackTarget);
		}
	}

	private static void runAttacker(RobotController rc, MapLocation locTarget, MapLocation attackTarget) throws GameActionException {
		
		if(rc.senseObjectAtLocation(attackTarget))
			
		
			
		if(rc.getLocation().distanceSquaredTo(locTarget)>rc.getType().attackRadiusMaxSquared)
			Move.moveTo(rc, locTarget);
		else
			rc.attackSquare(attackTarget);
	}

	private static void runDefender(RobotController rc, MapLocation locTarget, MapLocation attackTarget) throws GameActionException {
		Move.moveTo(rc, locTarget); //move continuously to a rally point
		
	}

}
