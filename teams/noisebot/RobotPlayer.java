package noisebot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

//GAME NOTES
//Round ends when rc.yield() or 10000 bytecodes for every robot.
//Every robot spawned: run() called once

public class RobotPlayer {
    
	/*
	 * A value of (-1) implies no writing to channels has occurred.
	 * Channel 0: Type of robot a spawned robot should be
	 * Channel 1: firstRun for HQ; establishes (-1)'s
	 * Channel 2 - 25: robot id:occupation mapping. Encoded as XXXXX0Y where XXXXX is the robot ID and Y is robot occupation
	 * 					Y = 0 DEFENDER, 1 ATTACKER, 2 PASTR, 3 NOISETOWER
	 * Channel 26-50: robot id:target assignment mapping. Encoded as XXXX0B where XXXX is the robot ID and B is index of the robot target
	 * Channel 51-70: pasture locations
	 * Channel 71-100: enemy locations
	 * Channel 101-130: robot posting the round number, so that hq can see which robots died
	 */
	
	 public static void run(RobotController rc){
    	
    	try {
    		int id = rc.getRobot().getID();
        	RobotType type = rc.getType();
        	
        	if(rc.readBroadcast(1)!=-1){
        		//On first run, set all robot occupations to -1.
        		for(int i = 0; i < 131; i++)
        			rc.broadcast(i, -1);
        		
        	} else {
        		//Put ID and occupation into appropriate channel
        		for(int i = 2; i < 26; i++){
        			if(rc.readBroadcast(i)==-1) {
        				rc.broadcast(i, 100*id+rc.readBroadcast(0));
        				break;
        			}
        		}
        		
        	}
        	
        	while(true) {
        		
        		if(type == RobotType.HQ)
                	HQ.runHeadquarters(rc);
        		
        		else if (type == RobotType.PASTR)
        			PASTR.maintainPasture(rc);
        		
        		else if(type == RobotType.NOISETOWER)
        			NOISE.maintainNoiseTower(rc);
        		
        		else {
        			
        			if (rc.isActive()&& rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) >25) {
        				rc.construct(RobotType.NOISETOWER);
        			} else {
        				
        				if (rc.isActive()&&Clock.getRoundNum() > 210) {
        					rc.construct(RobotType.PASTR);
        				} else {
        					MapLocation loc = rc.getLocation();
        					MapLocation perimeter1 = new MapLocation(loc.x+5, loc.y);
        					rc.senseObjectAtLocation(perimeter1);
        					Util.randomMove(rc);
        					//take up positions
        				}
        			}
//        			for(int i = 2; i < 26; i++){
//        				int val = rc.readBroadcast(i);
//        				if((val-val%100)/100==id){
//        					
//        					switch(val%100){
//        						case 2: PASTR.runPastureCreator(rc); break;
//        						case 1: COWBOY.runAttacker(rc); break;
//        						case 0: COWBOY.runDefender(rc); break;
//        						case 3: NOISE.runNoiseCreator(rc);
//        					}
//        				}
//        			}
        		}
	
        		rc.yield();
            }
		
    	} catch (GameActionException e) {
			e.printStackTrace();
		}
        
	}

}
    





