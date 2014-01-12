package origrestructured;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PASTR {


    public static void spawnPASTR(RobotController rc){
		
		for (Direction i:Util.allDirections) {
			if(rc.canMove(i)) {
				try {
					rc.spawn(i);
//					HQ.tempSpawnedType = types.PASTR;
					rc.broadcast(0, 2);
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
		
		System.out.println("RUNNING PASTURE CREATOR");
		
		MapLocation[] desiredPASTRs = Util.commToPSTRLocs(rc);
		
		try {
			if(rc.isActive()) {
				int id = rc.getRobot().getID();
				
				int idx = -1;
				for(int i = 26; i < 51; i++){
					int val = rc.readBroadcast(i);
					if((val-val%100)/100==id)
						idx = val%100;
				}
					
				
				if(idx > -1) {
						MapLocation target = desiredPASTRs[idx];

						if(rc.getLocation().x == target.x  &&  rc.getLocation().y == target.y) {
							rc.construct(RobotType.PASTR);
							System.out.println("Converted PASTR");
						}
						else
							Util.moveTo(rc, target);
						
				} else
					Util.moveTo(rc, desiredPASTRs[0]);
				
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
