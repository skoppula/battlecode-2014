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
		
		if (assignment==0) 
			assignment = rc.readBroadcast(id);
		
		//Understand the assignment
		int squad = Util.getSquad(assignment);
		int role = Util.getRole(assignment);
		
		//if low on health send distress
		if(rc.getHealth() == rc.getType().maxHealth*0.5){
			int in = rc.readBroadcast(1);
			//int len = (int) (Math.log10(in+1)+1)/3;
			int len = String.valueOf(in).length()/3;
			//System.out.println("Sending distress signal! ID: " + id + " Squad: " + squad + " Role: " + role);
			System.out.println("SQUAD HERE" + squad + " dsfd " + (in+ (int) Math.pow(10, len)*(10*squad+role)));
			rc.broadcast(1, in+ (int) Math.pow(10, len)*(10*squad+role));
			//System.out.println(in+ (int) Math.pow(10, len)*(10*squad+role));
			
			if(role == 0) {
				rc.broadcast(10, squad);
				
			System.out.println("MR.LOLSQUAD " + squad);
			}
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
		MapLocation loc = rc.getLocation();
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		MapLocation target = new MapLocation(targetX, targetY);
		int currX = (squadInfo/10000000), currY = (squadInfo/100000)%100;
		int x = (loc.x+currX)/2, y = (loc.y+currY)/2;
		
		//broadcast the new average
		rc.broadcast(squad, x*10000000+y*100000+squadInfo%100000);
		System.out.println("squad tracker" + squad);
		
		if(t == types.ATTACKER){
			
			rc.setIndicatorString(4, "ATTACKER");
			
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
			
			if(eloc!=null && rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
				
		} else if (t == types.DEFENDER) {

			rc.setIndicatorString(4, "DEFENDER");			

			//PASTR and NT creation
			int in = rc.readBroadcast(2);
			int diff = squad > 10 ? 11 : 3;
			int status = (in/(int) Math.pow(10, squad-diff)) % 10; // should be 0, 1, or 2
			
			int a = rc.readBroadcast(51);
			
			if( (allies.length>4) && status==0 && rc.isActive()) { //6 is the optimal for big maps
				//with 5 we barely fend them off but we get a shit ton more milk
				//with 4 we actually win decisively...wtf!
				rc.construct(RobotType.PASTR);
				int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
				int right = in % (int) Math.pow(10, squad-diff);
				rc.broadcast(2, left + right);
				System.out.println("Constructing a PASTR..." + allies.length + HQ.rush);
				
			} else if (allies.length>2 && (status==1||a > 0) && rc.isActive()) {
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
			
			if(eloc != null && rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
		}

			rc.yield();
		
	}
		
	
}
