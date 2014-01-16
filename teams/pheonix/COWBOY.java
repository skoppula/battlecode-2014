package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class COWBOY {

	public static void runSoldier(RobotController rc, int assignment) throws GameActionException {
		// gets memory and executes role
		
		int id = rc.getRobot().getID(); //ID will be unique to each soldier
		
		//Understand the assignment
		int team = Comm.getSquad(assignment);
		int role = Comm.getRole(assignment);
		MapLocation targetLocation = Comm.getTargetLocation(memory);
		
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct
		
		switch(Comm.getType(memory)){                              
	        case 0: COWBOY.runDefender(rc); break;
	        case 1: COWBOY.runAttacker(rc); break;
	        case 2: PASTR.runPastureCreator(rc); break;
	        case 3: NOISE.runNoiseCreator(rc);
		}
	}

	private static void runAttacker(RobotController rc) {
		// TODO Auto-generated method stub
		
	}

	private static void runDefender(RobotController rc) {
		// TODO Auto-generated method stub
		
	}

}
