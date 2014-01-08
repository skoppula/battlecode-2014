package skandascowmaptest;
//This goes to the place Skanda talked about of the map

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer{
	
	public static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	
	public static void run(RobotController rcin){
		rc = rcin;
		randall.setSeed(rc.getRobot().getID());
		
		double cowDensMap[][] = rc.senseCowGrowth();
        //Get dimensions of map
        int mapY = cowDensMap.length, mapX = cowDensMap[0].length;
        MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
        
        
		while(true){
			try{
				if(rc.getType()==RobotType.HQ){//if I'm a headquarters
					runHeadquarters();
				}else if(rc.getType()==RobotType.SOLDIER){
					runSoldier();
					//Get dimensions of map
					//Move toward the center of the map
					//This is terrible code change it!
					MapLocation goal = pastureLocs[0];
					Direction d = rc.getLocation().directionTo(goal);
					if(rc.isActive()&&rc.canMove(d)){
						rc.move(d);
					}
				}
				rc.yield();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void runSoldier() throws GameActionException {
		int currentBytecode = Clock.getBytecodeNum();
		tryToShoot();
		//communication
		//rc.setIndicatorString(0, "read ID: "+rc.readBroadcast(0));
		int editingChannel = (Clock.getRoundNum()%2);
		int usingChannel = ((Clock.getRoundNum()+1)%2);
		
		int runningTotal = rc.readBroadcast(editingChannel);
		rc.broadcast(editingChannel, runningTotal+1);
		
		MapLocation runningVectorTotal = intToLoc(rc.readBroadcast(editingChannel+2));
		rc.broadcast(editingChannel+2,locToInt(mladd(runningVectorTotal,rc.getLocation())));
		MapLocation averagePositionOfSwarm = mldivide(intToLoc(rc.readBroadcast(usingChannel+2)),rc.readBroadcast(usingChannel));
		
		rc.setIndicatorString(0, ""+locToInt(averagePositionOfSwarm));
		
		//movement
//		Direction chosenDirection = allDirections[(int)(randall.nextDouble()*8)];
//		if(rc.isActive()&&rc.canMove(chosenDirection)){
//			rc.move(chosenDirection);
//		}
		swarmMove(averagePositionOfSwarm);
	}
	
	private static void swarmMove(MapLocation averagePositionOfSwarm) throws GameActionException{
		Direction chosenDirection = rc.getLocation().directionTo(averagePositionOfSwarm);
		if(rc.isActive()){
			if(randall.nextDouble()<0.2){//go to swarm center
				for(int directionalOffset:directionalLooks){
					int forwardInt = chosenDirection.ordinal();
					Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
					if(rc.canMove(trialDir)){
						rc.move(trialDir);
						break;
					}
				}
			}else{//go wherever the wind takes you
//				Direction d = allDirections[(int)(randall.nextDouble()*8)];
//				if(rc.isActive()&&rc.canMove(d)){
//					rc.move(d);
//				}
				
				System.out.println("hi");
				
			}
		}
	}
	
	private static MapLocation mladd(MapLocation m1, MapLocation m2){
		return new MapLocation(m1.x+m2.x,m1.y+m2.y);
	}
	
	private static MapLocation mldivide(MapLocation bigM, int divisor){
		return new MapLocation(bigM.x/divisor, bigM.y/divisor);
	}

	private static int locToInt(MapLocation m){
		return (m.x*100 + m.y);
	}
	
	private static MapLocation intToLoc(int i){
		return new MapLocation(i/100,i%100);
	}
	
	private static void tryToShoot() throws GameActionException {
		//shooting
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		if(enemyRobots.length>0){//if there are enemies
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(anEnemyInfo.location);
				}
			}
		}else{//there are no enemies, so build a tower
			//checknearbypastr();
			MapLocation pastrs[] = rc.sensePastrLocations(rc.getTeam());
			//write your own function for this
			boolean a = false;
			boolean b = false;
			for (MapLocation i:pastrs) {
				if (rc.getLocation().distanceSquaredTo(i) < 20) {
					a = true;
				}
			}
			
			if (rc.sensePastrLocations(rc.getTeam()).length<5) {
				b = true;
			}
			
			if( (randall.nextDouble()<0.01&&a && b) || ((Clock.getRoundNum()<100) &&rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 100)){
				//rc.senseCowsAtLocation(arg0);
				if(rc.isActive()){
					rc.construct(RobotType.PASTR);
				}
			}
		}
	}

	private static void runHeadquarters() throws GameActionException {
		Direction spawnDir = Direction.NORTH;
		if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			rc.spawn(Direction.NORTH);
		}
		
		int editingChannel = (Clock.getRoundNum()%2);
		int usingChannel = ((Clock.getRoundNum()+1)%2);
		rc.broadcast(editingChannel, 0);
		rc.broadcast(editingChannel+2, 0);
	}
	
	//Finds best pasture locations
    private static MapLocation[] findPastureLocs(double cowmap[][], int mapY, int mapX, int numPastures) {
            
            MapLocation pstrLocs[] = new MapLocation[numPastures];
            int pstrCowDens[] = new int[5];
            
            //Slides a 3x3 window across the entire map, intervals of three and returns windows with highest 
            for(int i = 0; i < mapY-3; i+=3){
                    for(int j = 0; j < mapX-3; j+=3){
                            
                            int sum = (int) (cowmap[i][j] + cowmap[i+1][j] + cowmap[i+2][j] 
                                                    + cowmap[i][j+1] + cowmap[i+1][j+1] + cowmap[i+2][j+1]
                                                    + cowmap[i][j+2] + cowmap[i+1][j+2] + cowmap[i+2][j+2]);
                            
                            for(int k = 0; k < numPastures; k++){
                                    if(sum>pstrCowDens[k]){
                                            pstrLocs[k+1] = new MapLocation(j, i);
                                            pstrCowDens[k+1] = sum;
                                            break;
                                    }
                            }
                    }
            }

            return pstrLocs;
    }
}