package noisebot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NOISE {

	public static void runNoiseCreator(RobotController rc) {
		
		rc.yield();
	}

	public static void maintainNoiseTower(RobotController rc) throws GameActionException {
		
		int mapX = rc.getMapWidth();
		int mapY = rc.getMapHeight();
		MapLocation target = new MapLocation(mapX/2, mapY/2);
		MapLocation loc = rc.getLocation();
		MapLocation HQ = rc.senseHQLocation();
		MapLocation y1 = new MapLocation(HQ.x, mapY - 4);
		MapLocation y2 = new MapLocation(HQ.x, mapY - 10);
		MapLocation y3 = new MapLocation(HQ.x, mapY - 16);
		MapLocation x1 = new MapLocation(mapX - 4, HQ.y);
		MapLocation x2 = new MapLocation(mapX - 10, HQ.y);
		MapLocation x3 = new MapLocation(mapX - 16, HQ.y);
		
		MapLocation[] targets = {y1, y2, y3, x1, x2, x3};
		
		int round = Clock.getRoundNum()%7;
		if (round==0) {
			System.out.println(round + " :) " + mapX  + "and" + mapY);
			target = targets[0];
		} else if (round==1) {
			System.out.println(round + " :) ");
			target = targets[1];
		} else if (round==2) {
			target = targets[2];
		} else if (round==3) {
			System.out.println(round + " :) ");
			target = targets[3];
		} else if (round==4) {
			target = targets[4];
		} else if (round==5) {
			target = targets[5];
		}
		
		if (rc.isActive()&&rc.canAttackSquare(target)) {
			rc.attackSquare(target);
			System.out.println(target + " :) :) ");
		} else {
			if (rc.isActive()) {
				rc.attackSquare(loc);
			}
		}
		
		rc.yield();
	}

}
