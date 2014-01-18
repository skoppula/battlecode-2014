package integrated;

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
	        case 0: COWBOY.runSoldier (rc, squad, types.DEFENDER); break;
	        case 1: COWBOY.runSoldier(rc, squad, types.ATTACKER); break;
		}
	}
	
	static void runSoldier (RobotController rc, int squad, types t) throws GameActionException {
		
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 10000, enemy);
		
		MapLocation loc = rc.getLocation();
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		MapLocation target = new MapLocation(targetX, targetY);
		int currX = (squadInfo/10000000), currY = (squadInfo/100000)%100;
		int x = (loc.x+currX)/2, y = (loc.y+currY)/2;
		
		//broadcast the new average
		rc.broadcast(squad, x*10000000+y*100000+squadInfo%100000);
		
		//PASTR and NT creation
		int in = rc.readBroadcast(2);
		int diff = squad > 10 ? 11 : 3;
		
		int status = (in/(int) Math.pow(10, squad-diff)) % 10; // should be 0, 1, or 2
		System.out.println(in + " " + squad + " " + status + " " + diff);
		System.out.println(squadInfo + " " + targetX + " " + targetY + " ");
		
		if(t==types.DEFENDER && loc.distanceSquaredTo(new MapLocation(targetX, targetY))<2){
			System.out.println("running defender!");
				Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 7, team);
				
				if(allies.length>2 && status==0 && rc.isActive()) {
					rc.construct(RobotType.PASTR);
					int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
					int right = in % (int) Math.pow(10, squad-diff);
//					System.out.println("LEFT LEFT LEFT: " + left + " " + right);
					rc.broadcast(2, left + right);
					System.out.println("Constructing a PASTR...");
					
				} else if (allies.length>2 && status==1 && rc.isActive()) {
					rc.construct(RobotType.NOISETOWER);
					int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
					int right = in % (int) Math.pow(10, squad-diff);
					rc.broadcast(2, left + right);
					System.out.println("Constructing a NT...");
				}
		}
		System.out.println((loc.x-targetX)+(loc.y-targetY));
		
		if(enemyRobots.length>0){
			
//			if(Math.random()>0.05)
//				Util.moveToward(rc, new MapLocation(x, y)); //Regroup
//			else
//				Util.moveToward(rc, new MapLocation(targetX, targetY));
			
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, loc); //SHOULD NOT OUTPUT AN HQ LOCATION
			
			int maxAttackRad = rc.getType().attackRadiusMaxSquared;
			
			if(rc.isActive() && eloc.distanceSquaredTo(rc.getLocation())<=maxAttackRad)
				rc.attackSquare(eloc);
			else if(rc.isActive() && rc.canMove(eloc.directionTo(loc)))
				rc.move(eloc.directionTo(loc));
			
		} else if(loc.distanceSquaredTo(target) > 9)
			Util.moveTo(rc, new MapLocation(targetX, targetY));
		
		
	//	else
			rc.yield();
		
	}
		
	
}
