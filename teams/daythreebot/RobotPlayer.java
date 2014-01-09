package daythreebot;


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
        Direction[] allDirections = Direction.values();
        Direction[] kevinDir = allDirections;
        int round = 0;
        //robotID + how much it has gone through the list
        //put zeroes in the places that it has already gone through
        
        
        while(true){
            try{
                    if(rc.getType()==RobotType.HQ){//if I'm a headquarters
                    	runHeadquarters();
                    }else if(rc.getType()==RobotType.SOLDIER){
                    	
                    	//First I want to get the robot out of the headquarters a little
                    	if(Clock.getRoundNum() < 10 && rc.isActive()&&rc.canMove(Direction.NORTH)){
                    		rc.move(Direction.NORTH);
                    	} else {
                    		//Now it will go in a clockwise circle following the array
                    		if (rc.isActive()&&rc.canMove(kevinDir[round])) {
                    			rc.move(kevinDir[round]);
                    			round +=1;
                    		}
                    		
                    		
                    		
                    	}
                    }
                    
                    rc.yield();
            } catch (Exception e) {
            	e.printStackTrace();
            }
            }
    }
    
	private static void tryToMove() throws GameActionException {
		// TODO Auto-generated method stub
		for (int i = 0;i<7;i++) {
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
            if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()< 1){
            	rc.spawn(spawnDir);
            	break;
            }
    	}	
    }

}