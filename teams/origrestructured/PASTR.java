package origrestructured;

import java.util.HashMap;
import battlecode.common.RobotController;

public class PASTR {

    static HashMap<Integer, Integer> roboPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
	public static void runPastureCreator(RobotController rc) {
		
		int id = rc.getRobot().getID();
		
		/*
		
		if(rc.isActive()){
			
			if(in correct pasture location){
				rc.construct(RobotType.PASTR);
				update currPASTRs[]
			} else {
				if(rc.canMove) -> move robot toward goal, as per roboPSTRsAssignment
			}
		
		}
		*/
		
		rc.yield();
	}

	public static void maintainPasture(RobotController rc) {
		
		
		//IF health is low and rc.isActive()
		//suicide
		//update currPASTRs[] and roboPSTRsAssignment
		
		rc.yield();
	}

}
