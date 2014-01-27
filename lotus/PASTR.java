package lotus;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class PASTR {
	
	public static void broadcastSquad(RobotController rc) throws GameActionException{
		
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9, rc.getTeam());
		
		if(closeBy.length > 1) {
			int nearestID = closeBy[0].getID();
			rc.broadcast(rc.getRobot().getID(), Channels.assignmentDecoding(rc.readBroadcast(nearestID))[0]);
			
		} else
			COWBOY.kamikaze(rc);
		
	}

	public static void maintainPasture(RobotController rc) throws GameActionException {
		
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9, rc.getTeam());
		int squad = rc.readBroadcast(rc.getRobot().getID());
		
		if(closeBy.length < 2) {
			rc.broadcast(squad, 0);
			COWBOY.kamikaze(rc);
		}
		
		//Check if there is a NT nearby
		boolean NTnearby = false;
		for(Robot r:closeBy) {
			RobotInfo info = rc.senseRobotInfo(r);
			if(info.type == RobotType.NOISETOWER)
				NTnearby = true;
		}
		
		if(!NTnearby) {
			rc.broadcast(squad + 1, Channels.NTPASTREncoding(0, 1));
		} else {
			rc.broadcast(squad + 1, Channels.NTPASTREncoding(1, 1));
		}
		
	}
}