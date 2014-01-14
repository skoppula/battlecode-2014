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
		MapLocation target = new MapLocation(mapX, mapY);
		int round = Clock.getRoundNum()%3;
		if (round==0) {
			System.out.println(round + " :) " + HQ.mapX  + "and" + HQ.mapY);
			target = new MapLocation(3, HQ.mapY);
		} else {
			System.out.println(round + " :) ");
			target = new MapLocation(HQ.mapX/2, HQ.mapY/2);
		}
		
		if (rc.isActive()&&rc.canAttackSquare(target)) {
			rc.attackSquare(target);
			System.out.println(target + " :) :) ");
		}
		
		rc.yield();
	}

}
