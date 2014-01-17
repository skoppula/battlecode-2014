package integrated;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class COWBOY {
	
	static MapLocation rallyPoint;
	static MapLocation targetedPastr;
	static boolean coastIsClear = true;
	static boolean buildNT = false;

	static Random rand = new Random();

	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
		
		int id = rc.getRobot().getID(); //ID will be unique to each soldier
		
		if (assignment==0) {
			assignment = rc.readBroadcast(id);
			System.out.println(assignment);
		}
		
		//Understand the assignment
		int squad = Comms.getSquad(assignment);
		int role = Comms.getRole(assignment);
		
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct
		MapLocation[] enemyPSTR = rc.sensePastrLocations(rc.getTeam().opponent());
		if (squad>10&&enemyPSTR.length > 0) {
			Attacker.runAttacker(enemyPSTR[0]);
		} else {
			//Defender.runDefender(rc, squad, role);
			Attacker.runAttacker(rc.senseEnemyHQLocation());
			
		}
//		switch(role){                              
//	        case 0: COWBOY.runDefender(rc, locTarget, attackTarget); break;
//	        case 1: COWBOY.runAttacker(rc, locTarget, attackTarget); break;
//	        case 2: PASTR.runPastureCreator(rc, locTarget); break;
//	        case 3: NOISE.runNoiseCreator(rc);
//		}
	}
}
