package origrestructured;

import java.util.HashMap;

import origrestructured.HQ.types;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PASTR {

    static HashMap<Integer, Integer> roboPSTRsAssignment = new HashMap<Integer, Integer>();
    //Integer [Robot ID]:Integer [index of a MapLocation in desiredPASTRLocs[] ]
	
    public static void spawnPASTR(RobotController rc){
		
		for (Direction i:Util.allDirections) {
			if(rc.canMove(i)) {
				try {
					rc.spawn(i);
					HQ.tempSpawnedType = types.PASTR;
					HQ.robotTypeCount[2]++;
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		System.out.println("Spawned PASTR precursor");
    }
    
	public static void runPastureCreator(RobotController rc) {
		
		try {
			if(rc.isActive()) {
				int id = rc.getRobot().getID();
				
				if(roboPSTRsAssignment.containsKey(id)) {
						MapLocation target = HQ.desiredPASTRs[roboPSTRsAssignment.get(id)];
				
						if(rc.getLocation().x == target.x  &&  rc.getLocation().y == target.y) {
							rc.construct(RobotType.PASTR);
							System.out.println("Converted PASTR");
						}
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, HQ.desiredPASTRs[0]);
				
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}

	public static void maintainPasture(RobotController rc) {
		
		
		//IF health is low and rc.isActive()
		//suicide
		//update currPASTRs[] and roboPSTRsAssignment
		
		
		try {
			if(rc.getHealth()<3 && rc.isActive())
				rc.selfDestruct();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}

}
