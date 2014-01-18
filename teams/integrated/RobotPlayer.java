package integrated;

//things to do:
//defend pastrs that are under attack, or at least consider defending them
//battlecry when charging into battle -> concerted effort
//something like the opposite of a battlecry, when you're sure you're outnumbered

/*
 * Channel 0: spawning signal: squad*100+type
 * Channel 1: distress: [SS][T][SS][T]...SS=squad, and T = type of distressed robots
 * Channel 2: [null]
 * Channel 3-10: defensive squad locations & corresponding pastr/NT locations: [A][XX][YY], A = count robots in squad
 * Channel 11-20: offensive squad locations & corresponding pasr/NT locations
 * 
 */

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc) throws GameActionException{
		
		int id = rc.getRobot().getID();
    	RobotType type = rc.getType();
    	
		//read from channel 0: get squad and role
    	int assignment = rc.readBroadcast(0);

   
    	if(type != RobotType.HQ)
    		rc.broadcast(id, assignment);
		
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			System.out.println("A PASTR is running...");
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
        			COWBOY.runCowboy(rc, assignment);
        		}
        		
        		rc.yield();
        	}
		} catch (Exception e){
				e.printStackTrace();
		}
			
	}
	
}
