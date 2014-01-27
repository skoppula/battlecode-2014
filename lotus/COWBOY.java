package lotus;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class COWBOY {
	
	enum types {ATTACKER, DEFENDER, SCOUT}
	
	//check if target exists: if not, camp and attack?
	//corners around PASTR
	//introduce rally points into moveTo
	//if cows = 0 at pastr location, switch to attacker
	//economy based endgame, triggered when a pastr has been untouched for 130 rounds
	//
	
	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
	
		if (assignment == 0)
			assignment = rc.readBroadcast(rc.getRobot().getID());
			
		int squad = Channels.assignmentDecoding(assignment)[0];
		int role = Channels.assignmentDecoding(assignment)[1];
		
		if(rc.readBroadcast(squad) == 0) {
			System.out.println("My time has elapsed. I must die in battle with honor for my squad " + squad + "!");
			kamikaze(rc);
		}
		
		switch(role){                              
	        case 0: COWBOY.runDefender (rc, squad); break;
	        case 1: COWBOY.runAttacker (rc, squad, types.ATTACKER); break;
	        case 2: COWBOY.runScout (rc, squad, types.SCOUT); break;
		}
	}
	
	static void kamikaze(RobotController rc) throws GameActionException {

		Team enemy = rc.getTeam().opponent();
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		
		if(enemyPASTRs.length > 0)
			Util.moveTo(rc, enemyPASTRs[0]);
		else
			Util.moveTo(rc, rc.senseEnemyHQLocation());
		
	}
	
	static void runDefender(RobotController rc, int squad) throws GameActionException {
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		
		int squadInfo = rc.readBroadcast(squad);
		int targetX = Conversion.intToMapLocation(squadInfo).x;
		int targetY = Conversion.intToMapLocation(squadInfo).y;
		
		int PASTRstatus = rc.readBroadcast(squad+1);
		int NTstatus = rc.readBroadcast(squad+2);
		
		MapLocation curr = rc.getLocation();
		MapLocation target = new MapLocation(targetX, targetY);
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, team);

		if (allies.length > 3 && PASTRstatus == 0 && curr.distanceSquaredTo(target) < 25 && rc.isActive()) {
				rc.construct(RobotType.PASTR);
				rc.broadcast(squad + 1, 1);
				System.out.println("Constructing a PASTR...");
				
		} else if (allies.length>4 && (status==1) && rc.isActive()) {
			if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) < 16){
				rc.construct(RobotType.NOISETOWER);
				int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
				int right = in % (int) Math.pow(10, squad-diff);
				rc.broadcast(Util.pastrChannel, left + right);
				System.out.println("Constructing a NT...");
				
				//Communicates to the pastr that a NT was created, so if it is destroyed, pastr can tell
				rc.broadcast(Util.lastNTChannel, Clock.getRoundNum()); //channel 50: last noisetower constructed birthstamp
				rc.broadcast(Util.NTexistenceChannel, 0);
			}
		}
		
		//THEN GO TO RIGHT PLACE
		if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) > 4)
			Util.moveTo(rc, new MapLocation(targetX, targetY));
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
		MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
		
		if(eloc != null && rc.isActive() && rc.canAttackSquare(eloc))
			rc.attackSquare(eloc);

	}
	
	static void runSoldier (RobotController rc, int squad, types t) throws GameActionException {
		

		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared, team);
		MapLocation loc = rc.getLocation();
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		MapLocation target = new MapLocation(targetX, targetY);
		
		
		if(t == types.ATTACKER){
			
			rc.setIndicatorString(4, "ATTACKER");
			
			//If attacking HQ, have new HQ target outside HQ attack range
			if(rc.senseEnemyHQLocation().equals(new MapLocation(targetX, targetY))) {
				targetX -= RobotType.HQ.attackRadiusMaxSquared/Math.sqrt(2);
				targetY -= RobotType.HQ.attackRadiusMaxSquared/Math.sqrt(2); //this sometimes returns negative values
			}
			
			//Move away from home HQ
			if(loc.distanceSquaredTo(rc.senseHQLocation()) < 25)
				Util.tryToMove(rc);
			
			//Gather at the rally point
			System.out.println("attacker moving to" + targetX + "," + targetY + ", " + squadInfo);
			if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) > 2)
				//Util.moveTo(rc, new MapLocation(targetX, targetY));
				Util.channelMove(rc);
			
			//Attack nearby enemy soldiers
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
			
			if(eloc!=null && rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
				
		} else if (t == types.DEFENDER) {

			rc.setIndicatorString(4, "DEFENDER");			

			//PASTR and NT creation
			int in = rc.readBroadcast(Util.pastrChannel);
			int diff = squad > 10 ? 11 : 3;
			int status = (in/(int) Math.pow(10, squad-diff)) % 10; // should be 0, 1, or 2
			
			//communication
			int NTexistence = rc.readBroadcast(Util.NTexistenceChannel);
			int areaSafe = rc.readBroadcast(Util.areaSafeChannel);
			
			MapLocation[] allyPstrs = rc.sensePastrLocations(rc.getTeam());
			
			if (Clock.getRoundNum() > 1000 && Clock.getRoundNum() < 1500){
				System.out.println(status + "status" + in + "channel 2");
			}
			

			if(allies.length>3 && status==0 &&loc.distanceSquaredTo(target) < 25&&rc.isActive() ) {
				//check to make sure there are cows there
				double cows = rc.senseCowsAtLocation(loc);
				if (cows > 10) {
					rc.construct(RobotType.PASTR);
					int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
					int right = in % (int) Math.pow(10, squad-diff);
					rc.broadcast(Util.pastrChannel, left + right);
					System.out.println("Constructing a PASTR..." + allies.length + HQ.rush);
				}else {
					rc.broadcast(Util.failedPastr, 1);
					int id = rc.getRobot().getID();
					
					rc.broadcast(id, 1101);
					//signals that the robot has switched occupations
					rc.broadcast(id+1, 1);
					System.out.println("transformed into attacker" + id);
				}
			} else if (rc.readBroadcast(Util.strategyChannel) > 0&&rc.isActive()&&status==0) {
				double cows = rc.senseCowsAtLocation(loc);
				if (cows > 10) {
					rc.construct(RobotType.PASTR);
					int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
					int right = in % (int) Math.pow(10, squad-diff);
					rc.broadcast(Util.pastrChannel, left + right);
					System.out.println("Constructing a PASTR..." + allies.length + HQ.rush);
				}
			}
			else if (areaSafe > 0 &&rc.senseCowsAtLocation(loc) > 300&&rc.isActive()){
				//economy based endgame, triggered when a pastr has been untouched for 130 rounds
				//triggered in PASTR.java line 65
				rc.construct(RobotType.PASTR);
				//set status to 1 for the entire squad
				int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
				int right = in % (int) Math.pow(10, squad-diff);
				rc.broadcast(Util.pastrChannel, left + right);
				//don't need defenders
				rc.broadcast(squad, 900000);
				System.out.println("Constructing a PASTR..." + allies.length + HQ.rush);
			}
			else if (allies.length>4 && (status==1) && rc.isActive()) {
				if(Math.pow(loc.x-target.x,2) + Math.pow(loc.y-target.y, 2) < 16){
					rc.construct(RobotType.NOISETOWER);
					int left = (int) ((in/Math.pow(10, squad-diff)+1)*Math.pow(10, squad-diff));
					int right = in % (int) Math.pow(10, squad-diff);
					rc.broadcast(Util.pastrChannel, left + right);
					System.out.println("Constructing a NT...");
					
					//Communicates to the pastr that a NT was created, so if it is destroyed, pastr can tell
					rc.broadcast(Util.lastNTChannel, Clock.getRoundNum()); //channel 50: last noisetower constructed birthstamp
					rc.broadcast(Util.NTexistenceChannel, 0);
				}
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
