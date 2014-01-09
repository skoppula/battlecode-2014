package restructured;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

//GAME NOTES
//Round ends when rc.yield() for every robot.
//New rounds starts -> run() is run again

public class RobotPlayer {
    
    static enum types {DEFENDER, ATTACKER, PASTR, NOISETOWER};
    
    public static void run(RobotController rc){
    	
    	int id = rc.getRobot().getID();
    	RobotType type = rc.getType();
    	
        if(type == RobotType.HQ){
        	while(true)
            	HQ.runHeadquarters(rc);
        	
        } else if (type == RobotType.PASTR) {
        	while(true)
        		PASTR.maintainPasture(rc);
        	
        } else if (type == RobotType.NOISETOWER) {
        	while(true)
        		NOISE.maintainNoiseTower(rc);
        
        //If not others, must be a soldier, so only checking type of soldier
        } else if (HQ.occupations.get(id) == types.PASTR) {
        	while(true)
        			PASTR.runPastureCreator(rc);
        
        } else if (HQ.occupations.get(id) == types.DEFENDER) {
        	while(true)
            		COWBOY.runDefender(rc);
        	
        } else if (HQ.occupations.get(id) == types.ATTACKER) {	
        	while(true)
        			COWBOY.runAttacker(rc);
        		
        } else {
        	while(true)
        			NOISE.runNoiseCreator(rc);
        }
        
	}

}
    





