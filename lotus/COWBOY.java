package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class COWBOY {
	
	//TODO check if target exists or is HQ: if not, camp and attack?
	//TODO face oncoming enemy side when defending
	//TODO introduce rally points into moveTo
	//TODO if cows = 0 at pastr location, switch to attacker
	//TODO economy based endgame, triggered when a pastr has been untouched for 130 rounds
	//TODO update NT status
	//TODO if pastr dies, don't eliminate the entire Job
	//TODO why/when use Util.channelMove(rc)? and Util.avoidEnemyHQ(rc)?
	//TODO ask the HQ for reinforcements?
	//TODO in moveto, route robots around HQ, if possible, otherwise just pass through
	
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
	        case 1: COWBOY.runAttacker (rc, squad); break;
	        case 2: COWBOY.runScout (rc, squad); break;
		}
		
		rc.yield();
	}
	
	static void kamikaze(RobotController rc) throws GameActionException {

		Team enemy = rc.getTeam().opponent();
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		
		if(enemyPASTRs.length > 0)
			Util.moveTo(rc, enemyPASTRs[0]);
		else
			Util.moveTo(rc, rc.senseEnemyHQLocation());
		
	}
	
	static void runScout(RobotController rc, int squad) throws GameActionException {
		int squadInfo = rc.readBroadcast(squad);
		MapLocation target = Conversion.intToMapLocation(Channels.scoutDecoding(squadInfo)[1]);
		MapLocation curr = rc.getLocation();
		
		if(curr.distanceSquaredTo(target) > 50)
			Util.moveTo(rc, target);
		
		else {
			int start = Channels.scoutDecoding(squadInfo)[0];
			rc.broadcast(Channels.scoutChannel, Channels.scoutEncoding((Clock.getRoundNum()-start), target, 1));
		}
		
	}
	
	static void runDefender(RobotController rc, int squad) throws GameActionException {
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		
		int squadInfo = rc.readBroadcast(squad);
		MapLocation target = Conversion.intToMapLocation(squadInfo);
		MapLocation curr = rc.getLocation();
		
		int status = rc.readBroadcast(squad+1);
		int PASTRstatus = Channels.NTPASTRDecoding(status)[0];
		int NTstatus = Channels.NTPASTRDecoding(status)[1];
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, team);
		
		//Create a PASTR/NT if not already there
		if(allies.length > 3 && curr.distanceSquaredTo(target) < 16 && rc.isActive()) {
			if(PASTRstatus == 0) {
				rc.construct(RobotType.PASTR);
				rc.broadcast(squad + 1, Channels.NTPASTREncoding(NTstatus, 1));
				System.out.println("Constructing a PASTR...");
				
			} else if (NTstatus == 0) {
				rc.construct(RobotType.NOISETOWER);
				rc.broadcast(squad + 1, Channels.NTPASTREncoding(1, PASTRstatus));
				System.out.println("Constructing a NT...");
				
			}
		}
		
		//Then go to right place
		if(curr.distanceSquaredTo(target) > 8)
			Util.moveTo(rc, target);
		
		//Then attack!
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
		MapLocation eloc = Attack.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
		
		if(eloc != null && rc.isActive() && rc.canAttackSquare(eloc)) {
			Util.moveToward(rc, eloc);
			rc.attackSquare(eloc);
		}
	}
	
	static void runAttacker (RobotController rc, int squad) throws GameActionException {
		
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		
		int squadInfo = rc.readBroadcast(squad);
		MapLocation target = Conversion.intToMapLocation(squadInfo);
		MapLocation curr = rc.getLocation();
		
		//First steps away from home HQ
		if(curr.distanceSquaredTo(rc.senseHQLocation()) < 25)
			Util.tryToMove(rc);
		
		//Go to right place
		if(curr.distanceSquaredTo(target) > 7)
			Util.moveTo(rc, target);
			
		//Then attack!
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
		MapLocation eloc = Attack.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
		
		if(eloc != null && rc.isActive() && rc.canAttackSquare(eloc)) {
			Util.moveToward(rc, eloc);
			rc.attackSquare(eloc);
		}
		
	}
		
	
}
