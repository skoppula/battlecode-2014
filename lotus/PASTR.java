package lotus;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class PASTR {
	
	public static void broadcastSquad(RobotController rc) throws GameActionException{
		
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9, rc.getTeam());
		
		if(closeBy.length > 1) {
			int nearestID = closeBy[0].getID();
			rc.broadcast(rc.getRobot().getID(), Channels.assignmentDecoding(rc.readBroadcast(nearestID))[0]);
			
		} else
			kamikaze(rc);
		
	}
	
	private static void kamikaze(RobotController rc) throws GameActionException {
	}

	public static void maintainPasture(RobotController rc) throws GameActionException {
		
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9, rc.getTeam());
		int squad = rc.readBroadcast(rc.getRobot().getID());
		
		if(closeBy.length < 2) {
			rc.broadcast(squad, 0);
			kamikaze(rc);
		}
		
		
		
	}
}