package integrated;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class COWBOY {
	
	enum types {ATTACKER, DEFENDER}
	
	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
		
		int id = rc.getRobot().getID(); 
		
		if (assignment==0) {
			assignment = rc.readBroadcast(id);
			System.out.println(assignment);
		}
		
		//Understand the assignment
		int squad = Util.getSquad(assignment);
		int role = Util.getRole(assignment);
		
		//if low on health send distress
		if(rc.getHealth() == rc.getType().maxHealth*0.5){
			int in = rc.readBroadcast(1);
			//int len = (int) (Math.log10(in+1)+1)/3;
			int len = String.valueOf(in).length()/3;
			//System.out.println("Sending distress signal! ID: " + id + " Squad: " + squad + " Role: " + role);
			rc.broadcast(1, in+ (int) Math.pow(10, len)*(10*squad+role));
			//System.out.println(in+ (int) Math.pow(10, len)*(10*squad+role));
			
			if(squad == 0)
				rc.broadcast(10, 1);
		}
		
		//ATTACKERS - attack enemy pastrs in a swarm, regroup if necessary
		//DEFENDERS - stationary near pastr, senses enemy approaching, attacks in groups
		//PASTR precursor - goes straight to desired location, checks if it's a good area, constructs
		//NOISETOWER precursor - goes right next to PASTR, checks if PASTR doesn't have noisetower, construct

		switch(role){                              
	        case 0: COWBOY.runSoldier (rc, squad, types.DEFENDER); break;
	        case 1: COWBOY.runSoldier(rc, squad, types.ATTACKER); break;
		}
	}
	
	static void runSoldier (RobotController rc, int squad, types t) throws GameActionException {
		
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, team);
<<<<<<< HEAD
		MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
=======
>>>>>>> 5edfce9a656fcf1a1ac896adfe5f31dbe9695000
		MapLocation loc = rc.getLocation();
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		MapLocation target = new MapLocation(targetX, targetY);
		int currX = (squadInfo/10000000), currY = (squadInfo/100000)%100;
		int x = (loc.x+currX)/2, y = (loc.y+currY)/2;
		
		//broadcast the new average
		rc.broadcast(squad, x*10000000+y*100000+squadInfo%100000);
		
<<<<<<< HEAD
		//PASTR and NT creation
		int in = rc.readBroadcast(2);
		int diff = squad > 10 ? 11 : 3;
		
		int status = (in/(int) Math.pow(10, squad-diff)) % 10; // should be 0, 1, or 2
//		System.out.println(in + " " + squad + " " + status + " " + diff);
		//System.out.println(squadInfo + " " + targetX + " " + targetY + " ");
		
		MapLocation[] enemyPstrs = rc.sensePastrLocations(rc.getTeam().opponent()); // there is a way for the HQ to communicate this to the robot
		//otherwise they're heading straight to enemy HQ

		if(enemyRobots.length>0){ //for both cases
			Util.toDoWhileMoving(rc);
		} else if(t==types.DEFENDER&&loc.distanceSquaredTo(target) > 25) {
			Util.moveTo(rc, new MapLocation(targetX, targetY));
		} else if (t==types.ATTACKER && enemyPstrs.length > 0 ) {
			Util.moveTo(rc, enemyPstrs[0]);
		} else if (t==types.ATTACKER&& loc.distanceSquaredTo(rc.senseHQLocation()) < 25) {
			Direction away = rc.senseHQLocation().directionTo(loc);
			Util.tryToMove(rc);
		}
		
		//hot fix until skanda can explain status method and how to change it
		int a = rc.readBroadcast(51); //if a is more than 0, the noisetower has been killed, make a new one
		int safeArea = rc.readBroadcast(52); //52 is the areasafe channel
		if(t==types.DEFENDER && loc.distanceSquaredTo(new MapLocation(targetX, targetY))<25){
	
=======
		if(t == types.ATTACKER){
			
			//If attacking HQ, have new HQ target outside HQ attack range
			if(rc.senseEnemyHQLocation().equals(new MapLocation(targetX, targetY))) {
				targetX -= RobotType.HQ.attackRadiusMaxSquared/Math.sqrt(2);
				targetY -= RobotType.HQ.attackRadiusMaxSquared/Math.sqrt(2);
			}
			
			//Move away from home HQ
			if(loc.distanceSquaredTo(rc.senseHQLocation()) < 25)
				Util.tryToMove(rc);
			
			//AND THEN GO TO RIGHT PLACE
			if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) > 2)
				Util.moveTo(rc, new MapLocation(targetX, targetY));
			
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
			
			if(rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
				
		} else if (t == types.DEFENDER) {
			
			//PASTR and NT creation
			int in = rc.readBroadcast(2);
			int diff = squad > 10 ? 11 : 3;
			int status = (in/(int) Math.pow(10, squad-diff)) % 10; // should be 0, 1, or 2
			
			int a = rc.readBroadcast(51);
			
>>>>>>> 5edfce9a656fcf1a1ac896adfe5f31dbe9695000
			if( (allies.length>4) && status==0 && rc.isActive()) { //6 is the optimal for big maps
				//with 5 we barely fend them off but we get a shit ton more milk
				//with 4 we actually win decisively...wtf!
				rc.construct(RobotType.PASTR);
				int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
				int right = in % (int) Math.pow(10, squad-diff);
				rc.broadcast(2, left + right);
				System.out.println("Constructing a PASTR..." + allies.length + HQ.rush);
<<<<<<< HEAD
			}
			else if (safeArea > 0){ //if the area is defended //NOTE THIS IS UNFINISHED DON'T PAY ATTENTION
				//System.out.println(safeArea);
//				//Defender End game, build tons of pastures
//				//build desired pastrs
				MapLocation safeLoc = Util.intToLoc(safeArea);
				MapLocation[] desiredPASTRsLocations;
				
				int id = rc.getRobot().getID();
				int ah = rc.readBroadcast(squad);
				int oh = rc.readBroadcast(squad + 1);
				//System.out.println(ah + "is the old one and " + oh + "is the new one");
				rc.broadcast(squad, oh);
//				for (MapLocation i:HQ.desiredPASTRs) {
//					if (i.distanceSquaredTo(safeLoc) < 100) {
//						int id = rc.getRobot().getID();
//						//rc.broadcast(id, 1);
//						System.out.println(i + "is the next ideal location");
//					}
//				}
			}
			else if (allies.length>2 && (status==1||a > 0) && rc.isActive()) {
=======
				
			} else if (allies.length>2 && (status==1||a > 0) && rc.isActive()) {
>>>>>>> 5edfce9a656fcf1a1ac896adfe5f31dbe9695000
				rc.construct(RobotType.NOISETOWER);
				int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
				int right = in % (int) Math.pow(10, squad-diff);
				rc.broadcast(2, left + right);
				System.out.println("Constructing a NT...");
				
				//Communicates to the pastr that a NT was created, so if it is destroyed, pastr can tell
				rc.broadcast(50, Clock.getRoundNum());
				rc.broadcast(51, 0);
			}
			
			//THEN GO TO RIGHT PLACE
			if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) > 4)
				Util.moveTo(rc, new MapLocation(targetX, targetY));
			
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
			
			if(rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
		}

			rc.yield();
		
	}
		
	
}
