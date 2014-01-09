package dayonetesting;

//this is the code from the first Battlecode 2014 lecture
//paste this text into RobotPlayer.java in a package called bob
//this code is badly organized. We'll fix it in later lectures.
//you can use this as a reference for how to use certain methods.

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc){
		while(true){
			if(rc.getType()==RobotType.HQ){//if I'm a headquarters
				Direction allDirections[] = Direction.values();
				Direction spawnDir = allDirections[(int)(Math.random()*8)];
				try {
					if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
						rc.spawn(spawnDir);
					}
				} catch (GameActionException e) {
					// TODO hi contestant who downloaded this.
					e.printStackTrace();
				}
			}else if(rc.getType()==RobotType.SOLDIER){
				//shooting
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
				if(enemyRobots.length>0){//if there are enemies
					Robot anEnemy = enemyRobots[0];
					RobotInfo anEnemyInfo;
					try {
						anEnemyInfo = rc.senseRobotInfo(anEnemy);
						if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
							if(rc.isActive()){
								rc.attackSquare(anEnemyInfo.location);
							}
						}
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{//there are no enemies, so build a tower
					if(Math.random()<0.01){
						if(rc.isActive()){
							try {
								rc.construct(RobotType.PASTR);
							} catch (GameActionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else {
						//Move the robot toward the areas with the greatest cow growth
						double cowmap[][] = rc.senseCowGrowth();
						for (int i = 0; i < cowmap.length; i++) {					
							double cowmapx[] = cowmap[i];
						
							double max = cowmapx[0];
							for (int j = 1; j < cowmapx.length; j++) {
								if (cowmapx[i] > max) {
									max = cowmapx[i];
									//Here we should probably sort them in another array that lists the locations with the highest cow growth
								}
							}
						}
						if (rc.canMove(Direction.NORTH_EAST)) {try {
							rc.move(Direction.NORTH_EAST);
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}}
					}
				}
				//movement
				Direction allDirections[] = Direction.values();
				Direction chosenDirection = allDirections[(int)(Math.random()*8)];
				if(rc.isActive()&&rc.canMove(chosenDirection)){
					try {
						rc.move(chosenDirection);
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
			}
			rc.yield();
		}
	}
}