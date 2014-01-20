package integrated;


import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class PASTR {

	public static void maintainPasture(RobotController rc) throws GameActionException {
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, HQ.enemy);
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, HQ.team);
		
		int NTcount = 0;
		int lastNTconstruct = rc.readBroadcast(50);
		int NTexistenceChannel = 51;
		
		int spawnRound = rc.readBroadcast(rc.getRobot().getID());
		int areaSafeChannel = 52;
		//if after, say 150 rounds the pastr still exists, then it is well defended and set up
		//we can definitely set up surrounding pastrs, since we assume the area is well defended
		if (Clock.getRoundNum() > spawnRound + 150) {
			//broadcast to the HQ that the area is well defended and you can start with the late Economy game
			int area = Util.locToInt(rc.getLocation());
			rc.broadcast(areaSafeChannel, area);
		}
		
		if (allies.length < enemyRobots.length) {
			//ask the HQ for reinforcements
			//we know that the enemy will rush your pastures
		}
		
		for (Robot i:allies) {
			//sense noisetower
			RobotInfo a = rc.senseRobotInfo(i);
			if (a.type==RobotType.NOISETOWER) {
				//all is ok
				NTcount +=1;
			}
		}
		
		if (NTcount ==0 && Clock.getRoundNum() > lastNTconstruct + 110) {
			//ask HQ to spawn another NT
			System.out.println("NT HAS DIED NOOO");
			rc.broadcast(51, 1); //should say squad number
			//Skanda, how to change the status variable back to 1???
		}
		
		
	}
}