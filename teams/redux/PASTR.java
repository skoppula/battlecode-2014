package redux;

import battlecode.common.Clock;
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
		
		try {
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		System.out.println("Spawned PASTR precursor");
		rc.setIndicatorString(0, "PASTR precursor");
    }
    

    
	public static void runPastureCreator(RobotController rc) {
		
		try {
			Util.shootNearby(rc);
			
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		
		
			MapLocation[] desiredPASTRs = Util.commToPSTRLocs(rc);

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
						
						rc.setIndicatorString(1, target.toString());
						
						if(rc.getLocation().distanceSquaredTo(target)<3) {
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
			for(int i = 101; i < 130; i++){
				if(rc.readBroadcast(i)%10000!=Clock.getRoundNum()){
					rc.broadcast(i, Util.idRoundToInt(rc.getRobot().getID(), Clock.getRoundNum()));
					break;
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		try {
			if(rc.getHealth()<3 && rc.isActive())
				rc.selfDestruct();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
	}

}
