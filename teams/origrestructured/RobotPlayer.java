package origrestructured;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;






import java.util.Random;

//GAME NOTES
//Round ends when rc.yield() or 10000 bytecodes for every robot.
//Every robot spawned: run() called once

public class RobotPlayer {
    
    static enum types {DEFENDER, ATTACKER, PASTR, NOISETOWER};
    static Direction[] allDirections = Direction.values();
    
    public static void run(RobotController rc){
    	
    	try {
    		int id = rc.getRobot().getID();
        	RobotType type = rc.getType();
        	
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        		
        		else if (type == RobotType.SOLDIER) {
        			COWBOY.shootNearby(rc);
        			COWBOY.runSoldier(rc);
        			
        			//Util.tryToMove(rc);
                	
        		}  		
                
//            	
//            	if (type == RobotType.NOISETOWER) {
//                	
//                		NOISE.maintainNoiseTower(rc);
//                
//                //If not others, must be a soldier, so only checking type of soldier
//                } else if (HQ.occupations.get(id) == types.PASTR) {
//                	while(true)
//                			PASTR.runPastureCreator(rc);
//                
//                } else if (HQ.occupations.get(id) == types.DEFENDER) {
//                	while(true)
//                    		COWBOY.runDefender(rc);
//                	
//                } else if (HQ.occupations.get(id) == types.ATTACKER) {	
//                	while(true)
//                			COWBOY.runAttacker(rc);
//                		
//                } else {
//                	while(true)
//                			NOISE.runNoiseCreator(rc);
//                }
            
        		
        		rc.yield();
            }
		
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    





