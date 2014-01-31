package lotus;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//TODO enemy population based jobs

//TODO face oncoming enemy side when defending
//TODO offense camp and attack? retreat when outnumbered?
//TODO introduce rally points into offensive team

public class RobotPlayer{
	
	public static void run(RobotController rc) throws GameActionException{
		
		int id = rc.getRobot().getID();
		int assignment = rc.readBroadcast(Channels.spawnChannel);
    	RobotType type = rc.getType();

    	
    	if(type == RobotType.HQ) {
    		//Spawn the scout: now a trailblazing, rush attacker
    		//rc.broadcast(0, Channels.assignmentEncoding(Channels.scoutChannel, 2));
    		rc.broadcast(0, Channels.assignmentEncoding(11, 1));
    		MapLocation teamHQ = rc.senseHQLocation();
    		MapLocation enemyHQ =  rc.senseEnemyHQLocation();
    		int xDiff = enemyHQ.x - teamHQ.x, yDiff = enemyHQ.y - teamHQ.y;
    		int y = teamHQ.x + (int) (0.67*xDiff), x = teamHQ.y + (int) (0.67*yDiff);
    		MapLocation rallyPoint = new MapLocation(x, y);
    		rc.broadcast(11, Conversion.mapLocationToInt(enemyHQ));
        	//rc.broadcast(Channels.scoutChannel, Channels.scoutEncoding(Clock.getRoundNum(), rallyPoint, 0));
    		HQ.tryToSpawn(rc);
    		
    	} else if (type == RobotType.SOLDIER) {
    		//Place the squad and type assignment into the robot's ID channel
    		rc.broadcast(id, assignment);
    	
    	} else if (type == RobotType.PASTR){
    		//Find the squad of this PASTR
    		PASTR.broadcastSquad(rc);
    		
    	} else if (type == RobotType.NOISETOWER){
    		NOISE.broadcastSquad(rc);
    	}
    	
		try {
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        			
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else
        			COWBOY.runCowboy(rc, assignment);
        		
        		rc.yield();
        	}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
}
