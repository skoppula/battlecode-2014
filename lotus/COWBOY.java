package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class COWBOY {
	
	static int distanceThreshold = 9; //How close the robot can be to target to establish PASTR
	
	static void checkIfBackupNeeded(RobotController rc) throws GameActionException {
		
		int soldierSenseRad = (int) RobotType.SOLDIER.sensorRadiusSquared;
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, soldierSenseRad, rc.getTeam());
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, soldierSenseRad, rc.getTeam().opponent());
		
		//System.out.println(allies.length + " " + enemies.length);
		
		boolean outnumbered = false;
		if(enemies.length == 0)
			outnumbered = false;
		else if((double) allies.length/enemies.length <= 1.5)
			outnumbered = true;
		
		if(outnumbered && rc.readBroadcast(Channels.backupChannel) == 0) {
			int squad = rc.readBroadcast(rc.getRobot().getID());
			rc.broadcast(Channels.backupChannel, Channels.backupEncoding(rc.getLocation(), squad, enemies.length));
			System.out.println("COWBOY sending help call");
		}
	}
	
	public static void runCowboy(RobotController rc, int assignment) throws GameActionException {
	
		if (assignment == 0)
			assignment = rc.readBroadcast(rc.getRobot().getID());
			
		int squad = Channels.assignmentDecoding(assignment)[0];
		int role = Channels.assignmentDecoding(assignment)[1];
		
		if(rc.readBroadcast(squad) == 0) {
			System.out.println("My time has elapsed. I must die in battle with honor for my squad " + squad + "!");
			kamikaze(rc);
		}
		
		//checkIfBackupNeeded(rc);
		
		switch(role){                              
	        case 0: COWBOY.runDefender (rc, squad); break;
	        case 1: COWBOY.runAttacker (rc, squad); break;
	        case 2: COWBOY.runScout (rc, squad); break;
		}
		
		rc.yield();
	}
	
	private static void kamikaze(RobotController rc) throws GameActionException {

		Team enemy = rc.getTeam().opponent();
		MapLocation[] enemyPASTRs = rc.sensePastrLocations(enemy);
		
		if(enemyPASTRs.length > 0)
			Move.moveTo(rc, enemyPASTRs[0]);
		else
			Move.moveTo(rc, rc.senseEnemyHQLocation());
		
	}
	
	static void runScout(RobotController rc, int squad) throws GameActionException {
		int squadInfo = rc.readBroadcast(squad);
		MapLocation target = Conversion.intToMapLocation(Channels.scoutDecoding(squadInfo)[1]);
		MapLocation curr = rc.getLocation();
		
		if(curr.distanceSquaredTo(target) > 50)
			Move.moveTo(rc, target);
		
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
		int PASTRstatus = Channels.NTPASTRDecoding(status)[1];
		int NTstatus = Channels.NTPASTRDecoding(status)[0];
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared*2, team);
		
		//Create a PASTR/NT if not already there
		if(allies.length >= rc.readBroadcast(Channels.numAlliesNeededChannel) && curr.distanceSquaredTo(target) < distanceThreshold && rc.isActive()) {
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
			Move.moveTo(rc, target);
		
		//Then attack!
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
		MapLocation eloc = Attack.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
		
		if(eloc != null) {
			if(rc.isActive())
				Move.moveToward(rc, eloc);
			if(rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
		}
	}
	
	static void runAttacker (RobotController rc, int squad) throws GameActionException {
		
		Team team = rc.getTeam();
		Team enemy = team.opponent();
		
		int squadInfo = rc.readBroadcast(squad);
		MapLocation target = Conversion.intToMapLocation(squadInfo);
		MapLocation curr = rc.getLocation();

		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared*2, team);
		
		//First steps away from home HQ
		if(curr.distanceSquaredTo(rc.senseHQLocation()) < 25)
			Move.tryToMove(rc);
		
		//Go to right place
		if(curr.distanceSquaredTo(target) > 7) {
			//System.out.println(target + " target " + allies.length + "ally length");
			Move.moveTo(rc, target);
		}
		
		//Then attack!
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, enemy);
		MapLocation eloc = Attack.nearestEnemyLoc(rc, enemyRobots, rc.getLocation());
		
		if(eloc != null) {
			if(rc.isActive())
				Move.moveToward(rc, eloc);
			if(rc.isActive() && rc.canAttackSquare(eloc))
				rc.attackSquare(eloc);
		}
		
	}
		
	
}
