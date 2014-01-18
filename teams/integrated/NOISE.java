package integrated;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NOISE {
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};
    public static int radius = RobotType.NOISETOWER.attackRadiusMaxSquared;
    

	public static void maintainNoiseTower(RobotController rc) throws GameActionException {
		while(true){
			
			int r = 17; //square root of 300
			int r1 = 12; //17 / sqrt(2) for the corner pulls

			//North Pull
			for(int i = 0; i<r; i++){
				if(rc.isActive()){ //should add a check
					rc.attackSquare(rc.getLocation().add(0, r-i));
					rc.yield();
				}
			}
			
			//North_east pull
			for(int i = 0; i<r1; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(r1-i, r1-i));
					rc.yield();
				}
			}
			
			//East pull
			for(int i = 0; i<r; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(r-i, 0));
					rc.yield();
				}
			}
			
			//South_east pull
			for(int i = 0; i<r1; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(r1-i, -(r1-i)));
					rc.yield();
				}
			}
			
			//South pull
			for(int i = 0; i<r; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, -(r-i)));
					rc.yield();
				}
			}
			
			//South_west pull
			for(int i = 0; i<r1; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(r1-i), -(r1-i)));
					rc.yield();
				}
			}
			
			//West pull
			for(int i = 0; i<r; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(r-i), 0));
					rc.yield();
				}
			}
			
			//North_west pull
			for(int i = 0; i<r1; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(r1-i), r1-i));
					rc.yield();
				}
			}
		}
	}

}
