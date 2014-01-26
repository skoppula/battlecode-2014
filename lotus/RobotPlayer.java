package lotus;

/*
 * Channel 0: spawning signal: squad*100+type
 * Channel 1: distress: [SS][T][SS][T]...SS=squad, and T = type of distressed robots
 * Channel 2: current pastures and noise towers: [D][D][D]...where D=0 indicates no PASTR/NT, D=1 indicates PASTR, D=2 PASTR & NT set up 
 * Channel 3-9: defensive squad locations & corresponding PASTR/NT locations: [A][XX][YY], A = count robots in squad, (XX,YY) = target
 * Channel 10: if experiencing easyToRush 1; else 0
 * Channel 11-20: offensive squad locations & corresponding PASTR/NT locations
 * Channel 21: scout counter [#moves][XXYY][1 or 0], 1 = journey done, 0 = still en-route
 * 
 * Channel [ID]: assignment
 * Channel [ID+1] for PASTRS: round number it was spawned
 */

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc) throws GameActionException{
		
		int id = rc.getRobot().getID();
    	RobotType type = rc.getType();

    	
    	if(type == RobotType.HQ) {
    		//Spawn the scout
    		rc.broadcast(0, 2102);
        	rc.broadcast(21, Util.locToInt(rc.senseEnemyHQLocation()));
    		HQ.tryToSpawn(rc, 2);
    		
    	} else if (type == RobotType.SOLDIER) {
    		//Place the squad and type assignment into the robot's ID channel
    		int assignment = rc.readBroadcast(Util.spawnchannel);
    		rc.broadcast(id, assignment);
    	
    	} else if (type==RobotType.PASTR){
    		//Find the squad of this PASTR
    		PASTR.broadcastSquad(rc);
    		//TODO See how long it lives: if >100 rounds, area is safe, and set up more PASTRs around it
    		rc.broadcast(id+1, Clock.getRoundNum()); 
    	}
    	
    	//General run commands that loop
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        			
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
        			int assignment = rc.readBroadcast(Util.spawnchannel);
        			COWBOY.runCowboy(rc, assignment);
        		}
        		
        		rc.yield();
        	}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
}
