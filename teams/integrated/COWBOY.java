package integrated;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
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
		int squad = Util.getSquad(assignment);
		int role = Util.getRole(assignment);
		
		//if low on health send distress
		if(rc.getHealth()<rc.getType().maxHealth*0.1){
			int in = rc.readBroadcast(1);
			int len = (int) (Math.log10(in)+1)/3;
			System.out.println("Sending distress signal! ID: " + id + " Squad: " + squad + " Role: " + role);
			rc.broadcast(1, in+ (int) Math.pow(10, len)*(10*squad+role));
		}
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct

		switch(role){                              
	        case 0: COWBOY.runDefender(rc, squad, role); break;
	        case 1: COWBOY.runAttacker(rc, squad, role); break;
	        case 2: PASTR.runPastureCreator(rc, squad, role); break;
	        case 3: NOISE.runNoiseCreator(rc, squad, role);
		}
	}
		
		static void runAttacker(RoboController rc, int squad, int role) throws GameActionException {
			
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam().opponent());
			Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());//was 
			
			if(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
				MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
				if(enemyRobotLocations.length==0){//only HQ is in view
					//navigateByPath(alliedRobots);
					Util.moveTo(rc, rc.senseEnemyHQLocation());
				} else {//shootable robots are in view
					MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
					
					if((alliedRobots.length+1)>=enemyRobots.length){//attack when you have superior numbers
						attackClosest(closestEnemyLoc);
					}else{//otherwise regroup
						regroup(enemyRobots,alliedRobots,closestEnemyLoc);
					}

				}
				
			} else {//NAVIGATION BY DOWNLOADED PATH
				MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
				if(enemyPastrs.length>0&&alliedRobots.length>7){
					System.out.println("Move to " + targetedPastr);
					Util.moveTo(rc, targetedPastr);
				}
			}
		}
}
