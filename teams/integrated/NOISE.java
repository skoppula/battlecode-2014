package integrated;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NOISE {

	public static void print(String str){
		System.out.println(str);
	}
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};

	public static void runNoiseCreator(RobotController rc) throws GameActionException {
		Util.channelMove(rc);
		rc.yield();
		if(rc.isActive()){
			System.out.print("constructing noise tower...");
			rc.construct(RobotType.NOISETOWER);
		}
		rc.yield();
	}

	public static void maintainNoiseTower(RobotController rc) throws GameActionException {
		while(true){
			int r = rc.getType().attackRadiusMaxSquared;
			int s = (int) (r/Math.sqrt(2));
			//North Pull
			print("NT is pulling cows");
			for(int i = 0; i<r; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, r-i));
					rc.yield();
				}
			}
			
			//North_east pull
			for(int i = 0; i<s; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(s-i, s-i));
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
			for(int i = 0; i<s; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(14-s, -(14-s)));
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
			for(int i = 0; i<s; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(s-i), -(s-i)));
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
			for(int i = 0; i<s; i++){
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(s-i), s-i));
					rc.yield();
				}
			}
		}
	}

}
