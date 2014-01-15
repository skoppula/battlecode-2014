package redux;

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
        	
        	if(type != RobotType.HQ)
        		rc.broadcast(id, rc.readBroadcast(0)*10000+9999);
        	
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
                        switch(rc.readBroadcast(id)/10000){                              
                                case 0: COWBOY.runDefender(rc); break;
                                case 1: COWBOY.runAttacker(rc); break;
                                case 2: PASTR.runPastureCreator(rc); break;
                                case 3: NOISE.runNoiseCreator(rc);
                        }
        		}
        		
        		rc.yield();
        	}
        	
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    





