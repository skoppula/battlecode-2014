package daytwodebug;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
    public static RobotController rc;
    static Direction allDirections[] = Direction.values();
    static Random randall = new Random();
    static int directionalLooks[] = new int[]{0,1,-1,2,-2};
    
    public static void run(RobotController rcin){
    	rc = rcin;
        randall.setSeed(rc.getRobot().getID());
        
        double cowDensMap[][] = rc.senseCowGrowth();
    	//Get dimensions of map
        int mapY = cowDensMap.length;
		int mapX = cowDensMap[0].length;
    	MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
        MapLocation goal = pastureLocs[0];
    	//MapLocation goal = new MapLocation(22, 18);
        
        while(true){
            try{
                    if(rc.getType()==RobotType.HQ){//if I'm a headquarters
                    	runHeadquarters();
                    }else if(rc.getType()==RobotType.SOLDIER){
                    	//run soldier
                    	
                    	MapLocation loc = rc.getLocation();
                        if (loc.equals(goal)) {
                    		if(rc.isActive()&&rc.sensePastrLocations(rc.getTeam()).length<5){
                				rc.construct(RobotType.PASTR);
                			}
                    	} else {
                    		runSoldier(goal, rc);
                    	}
                    }
                    
                    rc.yield();
            } catch (Exception e) {
            	e.printStackTrace();
            }
            }
    }
    
    private static void runSoldier(MapLocation goal, RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
    	
    	//tryToMove();
    	if (Clock.getRoundNum() < 30) {
        	if(rc.isActive()&&rc.canMove(Direction.WEST)){
        		rc.move(Direction.WEST);
        	} else {
        		tryToMove();
        	}
        }
        else if ( (Clock.getRoundNum() > 30) && (Clock.getRoundNum() < 60) ) {
        	if(rc.isActive()&&rc.canMove(Direction.NORTH)){
        		rc.move(Direction.NORTH);
        	} else {
        		tryToMove();
        	}
        }
        else {
        	Direction toGoal = rc.getLocation().directionTo(goal);
        	if(rc.isActive()&&rc.canMove(toGoal)){
        		rc.move(toGoal);
        		System.out.println("YAY");
        	}
        }

        
    	
	}

//	private static void moveToGoal(MapLocation goal) throws GameActionException {
//		// TODO Auto-generated method stub
//		//Move toward goal
//        
//        Direction d = rc.getLocation().directionTo(goal);
//        if(rc.isActive()&&rc.canMove(d)){
//        	rc.move(d);
//        }
//	}

	private static void tryToMove() throws GameActionException {
		// TODO Auto-generated method stub
		for (int i = 0;i<4;i++) {
    		Direction move = allDirections[(int)(randall.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.move(move);
            	break;
            }
    	}
	}

	private static void runHeadquarters() throws GameActionException {
    	//up to 375BC
    	for (int i = 0;i<8;i++) {
    		Direction spawnDir = allDirections[i];
            if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.spawn(spawnDir);
            	break;
            }
    	}	
    }

	//Finds best pasture locations
	private static MapLocation[] findPastureLocs(double cowmap[][], int mapY, int mapX, int numPastures) {
		
		MapLocation pstrLocs[] = new MapLocation[numPastures];
		int pstrCowDens[] = new int[numPastures];
		
		//Fill default
		for (int i = 0; i < numPastures; i++) {
			pstrLocs[i] = new MapLocation(mapX/2, mapY/2);			
		}
		
		//Slides a 3x3 window across the entire map, intervals of three and returns windows with highest 
		for(int i = 0; i < mapY-3; i+=4){
			for(int j = 0; j < mapX-3; j+=4){
				
				int sum = (int) (cowmap[i][j] + cowmap[i+1][j] + cowmap[i+2][j] 
							+ cowmap[i][j+1] + cowmap[i+1][j+1] + cowmap[i+2][j+1]
							+ cowmap[i][j+2] + cowmap[i+1][j+2] + cowmap[i+2][j+2]);
				
				for(int k = 0; k < numPastures; k++){
					if(sum>pstrCowDens[k]){
						pstrLocs[k] = new MapLocation(j+1, i+1);
						pstrCowDens[k] = sum;
						break;
					}
				}
				
				
			}
		}
		for (int i = 0; i < pstrLocs.length; i++) {
			System.out.println("SKANDA FOUND THESE LOCATIONS: " + pstrLocs[i]);
		}
		

		return pstrLocs;
	}
}