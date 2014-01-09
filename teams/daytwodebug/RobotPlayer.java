package daytwodebug;

import java.util.Random;
import battlecode.common.*;

public class RobotPlayer {
    
    static Random randall = new Random();
    
    boolean initializerRun = false;
    
    static Direction allDirections[] = Direction.values();
    double cowDensMap[][] = rc.senseCowGrowth();
    
    
    
    public static void run(RobotController rc){

        randall.setSeed(rc.getRobot().getID());
        
        double cowDensMap[][] = rc.senseCowGrowth();
        int mapY = cowDensMap.length;
		int mapX = cowDensMap[0].length;
    	
		MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
    	
        while(true){
        	try{
                    if(rc.getType()==RobotType.HQ){
                    	runHeadquarters();
                    	
                    } else if (rc.getType()==RobotType.SOLDIER){
                    	
                    	//GAME NOTES
                    	//Round ends when rc.yield() for every robot.
                    	//New rounds starts -> run() is run again
                    	
                    	
                    	//Spawn three types of robots. Type of robot spawned depends on:
                    	//	Current distribution of robots
                    	//	Time or rounds
                    	//	Size of map and distance to enemy HQ
                    	

                    	//Four behavior paradigms:
                    	//
                    	//	HQ
                    	//		Run initializers
                    	//		Keep track robots: store robot ID and occupation 
                    	//		Manage communication and manage robots (HOW TO?)
                    	//
                    	//	Pasture creators
                    	//		
                    	//	Defending
                    	//		Defend from enemies
                    	//		Pushing cows into pastrs
                    	//
                    	///	Offensive
                    	//		Search for enemy pastrs
                    	//		Goto and shoot
                    	
                    	
                    	//If the location is at a node, make a pastr
                    	MapLocation loc = rc.getLocation();
                        if (loc.equals(goal)) {
                    		if(rc.isActive()&&rc.sensePastrLocations(rc.getTeam()).length<5){
                				rc.construct(RobotType.PASTR);
                			}
                    	} else {
                    		//move the soldier
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
        } else if ( (Clock.getRoundNum() > 30) && (Clock.getRoundNum() < 60) ) {
        	if(rc.isActive()&&rc.canMove(Direction.NORTH)){
        		rc.move(Direction.NORTH);
        	} else {
        		tryToMove();
        	}
        } else {
        	Direction toGoal = rc.getLocation().directionTo(goal);
        	if(rc.isActive()&&rc.canMove(toGoal)){
        		//Kevin's move function goes here
        		rc.move(toGoal);
        	}
        }
	}

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