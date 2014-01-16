package pheonix;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//GAME NOTES
//Round ends when rc.yield() or 10000 bytecodes for every robot.
//Every robot spawned: run() called once

public class RobotPlayer {
    
	/*
	 * A value of (-1) implies no writing to channels has occurred.
	 * Channel 0: Type of robot a spawned robot should be
	 * Channel ID: [occupation #] + destination: AABB
	 */
	
	 public static void run(RobotController rc){
    	
    	try {
    		int id = rc.getRobot().getID();
        	RobotType type = rc.getType();
        	
			//read from channel 0, get squad and role
        	int memory = rc.readBroadcast(0);
        	//broadcast to channel ID, squad[1-5]type[0-3]
        	if(type != RobotType.HQ)
        		rc.broadcast(id, Comm.assignmentToInt(squad, role));
        	
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
        			COWBOY.runSoldier(rc);
        			
        		}
        		
        		rc.yield();
        	}
        	
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    





