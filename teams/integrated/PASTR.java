package integrated;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class PASTR {
	static int squad = 0;
	static boolean sentBroadcast = false;

	public static void getSquad(RobotController rc) throws GameActionException{
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9);
		if(closeBy.length>1){
			int nearestID = closeBy[0].getID();
			squad = Util.getSquad(rc.readBroadcast(nearestID));
		}
	}
	
	public static void checkHealth(RobotController rc) throws GameActionException{
		int x = 0; //amount to decrement digit in channel 2 according to squad number
		if(rc.getHealth()<=rc.getType().maxHealth*.5){
			sentBroadcast = true;
			System.out.println("LOSING A PASTURE");
			int in = rc.readBroadcast(2);
			System.out.println(in);
			if((int) in/Math.pow(10, squad-3) == 1){
				x = 1;
			}
			else{
				x = 2;
			}
			int left = (int) ((in/Math.pow(10, squad-3)-x)*Math.pow(10, squad-3));
			int right = in % (int) Math.pow(10, squad-3);
			rc.broadcast(2, left + right);
			System.out.println(left+right);
		}
	}

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
		
		if(sentBroadcast == false){
			checkHealth(rc);
		}
	}
}