package lotus;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;


public class PASTR {
	
	public static void broadcastSquad(RobotController rc) throws GameActionException{
		
		int pastrSenseRad = (int) Math.sqrt(RobotType.PASTR.sensorRadiusSquared);
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, pastrSenseRad, rc.getTeam());
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, pastrSenseRad, rc.getTeam().opponent());
		
		//Get squad number from allies nearby
		if(allies.length > 1) {
			int nearestID = allies[0].getID();
			int squad = Channels.assignmentDecoding(rc.readBroadcast(nearestID))[0];
			rc.broadcast(rc.getRobot().getID(), squad);
			
		//if no nearby allies, call for help, assume it's in squad 1
		} else {
			if(rc.readBroadcast(Channels.backupChannel) == 0)
				rc.broadcast(Channels.backupChannel, Channels.backupEncoding(rc.getLocation(), 1, enemies.length));
		}
		
	}

	public static void maintainPasture(RobotController rc) throws GameActionException {
		
		int pastrSenseRad = (int) Math.sqrt(RobotType.PASTR.sensorRadiusSquared);
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, pastrSenseRad, rc.getTeam());
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, pastrSenseRad, rc.getTeam().opponent());
		
		if(allies.length/(double) enemies.length <= 1.5 && rc.readBroadcast(Channels.backupChannel) == 0) {
			int squad = rc.readBroadcast(rc.getRobot().getID());
			rc.broadcast(Channels.backupChannel, Channels.backupEncoding(rc.getLocation(), squad, enemies.length));
		}
		
		
		
	}
}