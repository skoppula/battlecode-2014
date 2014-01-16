package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
    
	/*
	 * A value of (-1) implies no writing to channels has occurred.
	 * Channel 0: Type of robot a spawned robot should be
	 * Channel 1: Distress channel!
	 * Channel ID: [occupation #] + destination: AABB
	 */
	
	 public static void run(RobotController rc){
    	
    	try {
    		int id = rc.getRobot().getID();
        	RobotType type = rc.getType();
        	
			//read from channel 0: get squad and role
        	int assignment = rc.readBroadcast(0);
        	System.out.println("Spawn : " + assignment);

        	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
        	if(type != RobotType.HQ)
        		rc.broadcast(id, assignment);
        	
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
        	
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    