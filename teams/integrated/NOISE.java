package integrated;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class NOISE {
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};

	public static void maintainNoiseTower(RobotController rc) throws GameActionException {
		while(true){

			//North Pull
			for(int i = 0; i<20; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, 20-i));
					rc.yield();
				}
			}
			
			//North_east pull
			for(int i = 0; i<14; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(14-i, 14-i));
					rc.yield();
				}
			}
			
			//East pull
			for(int i = 0; i<20; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(20-i, 0));
					rc.yield();
				}
			}
			
			//South_east pull
			for(int i = 0; i<14; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(14-i, -(14-i)));
					rc.yield();
				}
			}
			
			//South pull
			for(int i = 0; i<20; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, -(20-i)));
					rc.yield();
				}
			}
			
			//South_west pull
			for(int i = 0; i<14; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(14-i), -(14-i)));
					rc.yield();
				}
			}
			
			//West pull
			for(int i = 0; i<20; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(20-i), 0));
					rc.yield();
				}
			}
			
			//North_west pull
			for(int i = 0; i<14; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(14-i), 14-i));
					rc.yield();
				}
			}
		}
	}

}
