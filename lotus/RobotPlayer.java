package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//TODO check if target exists or is HQ: if not, camp and attack?
//TODO face oncoming enemy side when defending
//TODO introduce rally points into moveTo
//TODO if cows = 0 at pastr location, switch to attacker
//TODO economy based endgame, triggered when a pastr has been untouched for 130 rounds
//TODO if pastr dies, don't eliminate the entire Job
//TODO why/when use Util.channelMove(rc)? and Util.avoidEnemyHQ(rc)?
//TODO ask the HQ for reinforcements? retreat too
//TODO in moveto, route robots around HQ, if possible, otherwise just pass through

//TODO retreat if outnumbered
//TODO moveto fails on almsman?

public class RobotPlayer{
	
	public static void run(RobotController rc) throws GameActionException{
		
		int id = rc.getRobot().getID();
		int assignment = rc.readBroadcast(Channels.spawnChannel);
    	RobotType type = rc.getType();

    	
    	if(type == RobotType.HQ) {
    		//Spawn the scout
    		rc.broadcast(0, Channels.assignmentEncoding(Channels.scoutChannel, 2));
        	rc.broadcast(Channels.scoutChannel, Channels.scoutEncoding(Clock.getRoundNum(), rc.senseEnemyHQLocation(), 0));
    		HQ.tryToSpawn(rc);
    		
    	} else if (type == RobotType.SOLDIER) {
    		//Place the squad and type assignment into the robot's ID channel
    		rc.broadcast(id, assignment);
    	
    	} else if (type==RobotType.PASTR){
    		//Find the squad of this PASTR
    		PASTR.broadcastSquad(rc);
    	}
    	
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        			
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else
        			COWBOY.runCowboy(rc, assignment);
        		
        		rc.yield();
        	}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
}
