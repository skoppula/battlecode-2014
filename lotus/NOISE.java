package lotus;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NOISE {
	
	static void checkIfBackupNeeded(RobotController rc) throws GameActionException {
		
		int NTSenseRad = (int) RobotType.NOISETOWER.sensorRadiusSquared;
		
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, NTSenseRad, rc.getTeam());
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, NTSenseRad, rc.getTeam().opponent());
		
		//System.out.println(allies.length + " " + enemies.length);
		
		boolean outnumbered = false;
		if(enemies.length == 0)
			outnumbered = false;
		else //if((double) allies.length/enemies.length <= 1.5)
			outnumbered = true;
		
		if(outnumbered && rc.readBroadcast(Channels.backupChannel) == 0) {
			int squad = rc.readBroadcast(rc.getRobot().getID());
			rc.broadcast(Channels.backupChannel, Channels.backupEncoding(rc.getLocation(), squad, enemies.length));
			System.out.println("NT sending help call");
		}
	}
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};

	public static void maintainNoiseTower(RobotController rc) throws GameActionException {
		while(true){
			
			int r = (int) Math.sqrt(rc.getType().attackRadiusMaxSquared);
			int s = (int) (r/Math.sqrt(2));
			//North Pull
			for(int i = 0; i<r; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, r-i));
					rc.yield();
				}
			}
			
			//North_east pull
			for(int i = 0; i<s; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(s-i, s-i));
					rc.yield();
				}
			}
			
			//East pull
			for(int i = 0; i<r; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(r-i, 0));
					rc.yield();
				}
			}
			
			//South_east pull
			for(int i = 0; i<s; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(s-i, -(s-i)));
					rc.yield();
				}
			}
			
			//South pull
			for(int i = 0; i<r; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(0, -(r-i)));
					rc.yield();
				}
			}
			
			//South_west pull
			for(int i = 0; i<s; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(s-i), -(s-i)));
					rc.yield();
				}
			}
			
			//West pull
			for(int i = 0; i<r; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(r-i), 0));
					rc.yield();
				}
			}
			
			//North_west pull
			for(int i = 0; i<s; i++){
				rc.yield();
				checkIfBackupNeeded(rc);
				if(rc.isActive()){
					rc.attackSquare(rc.getLocation().add(-(s-i), s-i));
					rc.yield();
				}
			}
		}
	}

}
