package integrated;

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
	        case 0: COWBOY.runDefender(rc, squad); break;
	        case 1: COWBOY.runAttacker(rc, squad); break;
	    //    case 2: PASTR.runPastureCreator(rc, squad, role); break;
	        //double numberOfCows = rc.senseCowsAtLocation(checkLoc);
	    //    case 3: NOISE.runNoiseCreator(rc, squad, role);
		}
	}
	
	static void runDefender(RobotController rc, int squad) throws GameActionException {
		
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
		
	static void runAttacker(RobotController rc, int squad) throws GameActionException {
		
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
			
		} else
			Util.moveTo(rc, new MapLocation(targetX, targetY));

	}
	
}
