package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//TODO backup (nearby target switching)
//TODO enemy population based jobs
//TODO scout fix

//TODO face oncoming enemy side when defending
//TODO offense camp and attack? retreat when outnumbered?
//TODO introduce rally points into moveTo

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
