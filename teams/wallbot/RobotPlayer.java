package wallbot;
//This will go to the center and has several types of movements - swarm, movetogoal, try tomove in any direction

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	
	
	
	public static void run(RobotController rcin){
		rc = rcin;
		randall.setSeed(rc.getRobot().getID());
		
//    	MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
//        //MapLocation goal = pastureLocs[3];
//        MapLocation goal = new MapLocation(mapX/2, mapY/2);
    	
        //int round = 0;
        
		while(true){
			try{
				
				//Initialize terrain map array
				//int terrainMap[][] = new int[mapY][mapX];
				
				if(rc.getType()==RobotType.HQ){//if I'm a headquarters

					MapLocation goal;
					
					runHeadquarters();
					goal = findgoal();
					int goalint = locToInt(goal);
					int pastrs = rc.sensePastrLocations(rc.getTeam()).length;
					rc.broadcast(168+ pastrs, goalint);
					
					//draw(terrainMap, rc);
				}else if(rc.getType()==RobotType.SOLDIER){

					MapLocation loc = rc.getLocation();
					MapLocation goal = new MapLocation(1, 1);
					int round = Clock.getRoundNum();
					
					//Precaution and defending
					tryToShoot();
					
					//Setting up the pastrs
					if (rc.sensePastrLocations(rc.getTeam()).length < 1) {
						//run soldier as futurepastr
						int goalint = rc.readBroadcast(168);
						goal = intToLoc(goalint);
						System.out.println(goal.x + "  " + loc.x);
						runSoldier(goal, rc);
					} else {
						//go into cow herding mode
						runSoldier1(rc);
					}
					
					
					
				}
				//rc.resign();
				rc.yield();
				
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	private static MapLocation findgoal() {
		// TODO Auto-generated method stub
		
		//wallbot sense HQ locations
		MapLocation myHQ = rc.senseHQLocation();
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		MapLocation goal = new MapLocation(0, 0);
		double cowDensMap[][] = rc.senseCowGrowth();
		//Get dimensions of map
	    int mapY = cowDensMap.length;
		int mapX = cowDensMap[0].length;
		
		
		//
		int x = myHQ.x;
		int y = myHQ.y;
		int start = 0;
		int end = mapY;
		
		int clear = 0;
		
		//pp stands for possible pastrs
		int pp = 0;
		int p1X = x;
		int p1Y = y;
		
		if (myHQ.y > mapY/2) {
			end = myHQ.y;
		} else {
			start = myHQ.y;
		}
		
		//Figure out where the possible pastr is in p1X and p1Y
		for (int j = myHQ.x - 5; j < myHQ.x + 5; j++) {
			
			clear = 0;
			
			for(int i = start; i < end; i++){
				TerrainTile a = rc.senseTerrainTile(new MapLocation(j, i));
				if (a.equals(TerrainTile.NORMAL)||a.equals(TerrainTile.ROAD)) {
					//System.out.println(i + " YAAAY");
					clear +=1;
				}
			}
			
			if (clear >= end - start) {
				System.out.println("For x: " + j + "and y" + start);
				p1X = j;
				p1Y = start + 2;
				pp +=1;
				goal = new MapLocation(p1X, p1Y);
			}
			
		}
		return goal;
	}


	private static void runSoldier(MapLocation goal, RobotController rc) throws GameActionException {
//		int currentBytecode = Clock.getBytecodeNum();
		
		System.out.println("SOLDIER REPORTING; TRIED TO MOVE");
		//movement
		
		int x = rc.getLocation().x;
		int y = rc.getLocation().y;
		
		Direction toGoal = Direction.EAST;

		
		//Get the x right
		if (x < goal.x) {
			toGoal = Direction.EAST;
			if(rc.isActive()&&rc.canMove(toGoal)){
	    		rc.move(toGoal);
	    	}
		} else if (x == goal.x) {
			if (y==goal.y){
				if(rc.isActive()&&rc.sensePastrLocations(rc.getTeam()).length<5){
    				rc.construct(RobotType.PASTR);
    			}
			}else {
				toGoal = Direction.NORTH;
				if(rc.isActive()&&rc.canMove(toGoal)){
		    		rc.move(toGoal);
		    	}
			}
		}
		else {
			toGoal = Direction.WEST;
			if(rc.isActive()&&rc.canMove(toGoal)){
	    		rc.move(toGoal);
	    	}
		}
		
		
		//System.out.println(toGoal);
		
		//Try to move there until you get it
    	if(rc.isActive()&&rc.canMove(toGoal)){
    		rc.move(toGoal);
    	} else {
    		if (y < goal.y) {
    			toGoal = Direction.NORTH;
    		} else {
    			toGoal = Direction.SOUTH;
    		}
    		if(rc.isActive()&&rc.canMove(toGoal)){
        		rc.move(toGoal);
        	}
    	}
    	
	}

	private static void runSoldier1(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub

		int x = rc.getLocation().x;
		int y = rc.getLocation().y;
		MapLocation[] mypastrs = rc.sensePastrLocations(rc.getTeam());
		
		MapLocation[] enemypastrs = rc.sensePastrLocations(rc.getTeam().opponent());
		if (enemypastrs.length > 1) {
			int X = enemypastrs[0].x;
			int Y = enemypastrs[0].y;
			
			int chance = rc.getRobot().getID();
			
			int teams = mypastrs.length;
			if (chance%teams == 0) {
				MapLocation target = new MapLocation(X, Y);
				runSoldier(target, rc);
			}
			else {
				for (int i = 1;i<teams;i++){
					if (chance%teams ==i) {
						helppastr(rc, mypastrs[i]);
				}
				
				}
			}
		} else {
			helppastr(rc, mypastrs[0]);
		}
	}
	
	private static void helppastr(RobotController rc, MapLocation pastr) throws GameActionException {
		// TODO Auto-generated method stub
		
		//runSoldier(pastr, rc);
		tryToMove();
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
			if(randall.nextDouble()<0.001&&rc.sensePastrLocations(rc.getTeam()).length<5){
				//rc.senseCowsAtLocation(arg0);
				if(rc.isActive()){
					rc.construct(RobotType.PASTR);
				}
			}
		}
	}

	private static void runHeadquarters() throws GameActionException {
		//This changes according to myHQ position - right now myHQ is on lower half
		Direction[] wallspawnDirs = {Direction.NORTH, Direction.NORTH_EAST, Direction.NORTH_WEST};
		
		for (Direction i:wallspawnDirs) {
			if(rc.isActive()&&rc.canMove(i)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
				rc.spawn(i);
			}
		}
		
		int editingChannel = (Clock.getRoundNum()%2);
		int usingChannel = ((Clock.getRoundNum()+1)%2);
		rc.broadcast(editingChannel, 0);
		rc.broadcast(editingChannel+2, 0);
	}
}