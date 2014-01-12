package origrestructured;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//GAME NOTES
//Round ends when rc.yield() or 10000 bytecodes for every robot.
//Every robot spawned: run() called once

public class RobotPlayer {
    
	public static volatile boolean firstRun = true;
	
    public static void run(RobotController rc){
    	
    	try {
    		int id = rc.getRobot().getID();
        	RobotType type = rc.getType();
        	
        	if(firstRun){
        		firstRun = false;
        	} else {
        		HQ.occupations.put(id, HQ.tempSpawnedType);
        		Util.printHashMap(HQ.occupations);
        	}
        	
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else if(HQ.occupations.get(id) == HQ.types.PASTR)
        			PASTR.runPastureCreator(rc);
        		
        		else if(HQ.occupations.get(id) == HQ.types.DEFENDER)
        			COWBOY.runDefender(rc);
        		
        		else if(HQ.occupations.get(id) == HQ.types.ATTACKER)
        			COWBOY.runAttacker(rc);
        		
        		else
        			NOISE.runNoiseCreator(rc);
	
        		rc.yield();
            }
		
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    





