package lotus;


import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class PASTR {
	static boolean sentBroadcast = false;
	
	public static void broadcastSquad(RobotController rc) throws GameActionException{
		Robot[] closeBy = rc.senseNearbyGameObjects(Robot.class, 9);
		if(closeBy.length>1){
			int nearestID = closeBy[0].getID();
			rc.broadcast(rc.getRobot().getID(), Util.getSquad(rc.readBroadcast(nearestID)));
		}
	}
	
	public static void checkHealth(RobotController rc) throws GameActionException{
		int x = 0; //amount to decrement digit in channel 2 according to squad number
		
		int squad = Math.abs(rc.readBroadcast(rc.getRobot().getID()));
		
		if(rc.getHealth()<=rc.getType().maxHealth*.5){ 
			sentBroadcast = true;
			System.out.println("LOSING A PASTURE"); //if we lose a pasture, we should switch to rush
			
			//TODO
			//communicate to HQ that we could switch to rush OR we should find a better pasture position to defend
			//rc.broadcast(defenseFailedChannel, 1);
			//received by HQ in updateSquadLocs - move desired pastures closer to HQ
			
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
			
			if(rc.readBroadcast(rc.getRobot().getID())>=0) {
				rc.broadcast(2, left + right);
				rc.broadcast(rc.getRobot().getID(), rc.readBroadcast(rc.getRobot().getID())*-1);
			}
			
			System.out.println(left+right);
		}
	}

	public static void maintainPasture(RobotController rc) throws GameActionException {
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, HQ.enemy);
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, HQ.team);
		
		int NTcount = 0;
		int lastNTconstruct = rc.readBroadcast(Util.lastNTChannel);
		
		int spawnRound = rc.readBroadcast(rc.getRobot().getID()+1);
		//if after, say 150 rounds the pastr still exists, then it is well defended and set up
		//we can definitely set up surrounding pastrs, since we assume the area is well defended
		
		//sense if the pastr has survived a rush attack
		boolean underAttack = false;  
		boolean survivedRush = false;
		
		if (enemyRobots.length > 0) {
			//under attack, survived
			underAttack = true;
		}
		if (underAttack) {
			if (enemyRobots.length==0){
				survivedRush = true;
			}
		}
		
		if (Clock.getRoundNum() > spawnRound + 150&&spawnRound > 50&&survivedRush) { //takes 50 rounds to create a pastr
			//broadcast to the HQ that the area is well defended and you can start with the late Economy game
			int area = Util.locToInt(rc.getLocation());
			rc.broadcast(Util.areaSafeChannel, area);
			//System.out.println("SQUAD TRACKER" + areaSafeChannel + "area : " + area);
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
			//System.out.println("NT HAS DIED NOOO");
			rc.broadcast(Util.NTexistenceChannel, 1); //should say squad number
			//Skanda, how to change the status variable back to 1???
		}
		if(sentBroadcast == false){
			checkHealth(rc);
		}
		
	}
}