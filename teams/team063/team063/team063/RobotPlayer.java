package team063;
//goals expand out in fractal shapes

import battlecode.common.*;

import java.util.Random;
import java.util.Arrays;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction allDirections[] = Direction.values();
	static Random randall = new Random();
	static int directionalLooks[] = new int[]{0,1,-1,2,-2};
	
	
	
	public static void run(RobotController rcin){
		rc = rcin;
		int ID = rc.getRobot().getID();
		randall.setSeed(ID);
		MapLocation myHQ = rc.senseHQLocation();
		
		try {
			rc.broadcast(ID, 111);
			
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//    	MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
//        //MapLocation goal = pastureLocs[3];
//        MapLocation goal = new MapLocation(mapX/2, mapY/2);
    	
        //int round = 0;
        
		while(true){
			try{
				
				//Initialize terrain map array
				//int terrainMap[][] = new int[mapY][mapX];
				
				if(rc.getType()==RobotType.HQ){//if I'm a headquarters
					
					runHeadquarters();
					MapLocation[] mypstrs =rc.sensePastrLocations(rc.getTeam()); 
					int pastrs = mypstrs.length;
					if (pastrs==0) {
						findgoal(rc, myHQ);
					} else if (pastrs<10) {
						rc.broadcast(444, 0);
					}
					
					//draw(terrainMap, rc);
				}else if(rc.getType()==RobotType.SOLDIER){

					MapLocation loc = rc.getLocation();
					int round = Clock.getRoundNum();
					
					//Precaution and defending
					tryToShoot();
					
					//Method for posting location, possibility of swarm movement
					
					
					
					//Setting up the pastrs
					MapLocation[] pstrs = rc.sensePastrLocations(rc.getTeam());
					int num = pstrs.length;
					int goalint = rc.readBroadcast(168+num);
					MapLocation goal = intToLoc(goalint);
					////System.out.println(goalint + "and numpasturs is" + num);
					
					if (pstrs.length < 10) {
						
						//runSoldier(goal, rc);
						Util.moveTo(rc, goal);
					} else {
						//go into cow herding mode
						runSoldier1(rc);
					}
					
					//check if you can make pasture
					if (loc.equals(goal) ){
						int pastrs = rc.sensePastrLocations(rc.getTeam()).length;
						if(rc.isActive()&&pastrs<10){
		    				rc.construct(RobotType.PASTR);
		    				System.out.println("AAAHHHH THIS IS TOO MUCH");
		    				findgoal(rc, loc);
		    				//8888 means that you've successfully gotten a pastr where you want it,
		    				//kicking off another
		    				//findgoal method
		    			}
					}
					
					

					//Randomly set up pastrs where there are a ton of cows //sneak?
					boolean a = false;
					MapLocation[] corners = {loc.add(-1,1), loc.add(1,1), loc.add(-1,-1), loc.add(1,-1)};
					for (MapLocation c:corners)
					if (rc.canSenseSquare(c)) {
						double cows = rc.senseCowsAtLocation(c);
						if (cows > 2000) {
							a = true;
						}
					}
					
					boolean b = false;
					int count = 0;
					for (MapLocation i:pstrs ) {
						if (loc.distanceSquaredTo(i)>3) {
							count +=1;
						}
					}
					if (count ==pstrs.length) {
						b = true;
					}
					if (a &&b&& rc.isActive()) {
						rc.construct(RobotType.PASTR);
						System.out.println(a + "and " + b + "and" + count);
						rc.yield();
					}
					
				}
				//rc.resign();
				rc.yield();
				
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	private static MapLocation findgoal(RobotController rc, MapLocation initialLoc) throws GameActionException {
		// TODO Auto-generated method stub
		
		//wallbot sense HQ locations
		//MapLocation myHQ = rc.senseHQLocation();
		//MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		MapLocation loc = initialLoc;
		//System.out.println("FINDGOAL loc : " + loc.x + "and " + loc.y);

		int mapX = rc.getMapWidth();
		int mapY = rc.getMapHeight();
		
		int x_left = farthest(rc, loc, Direction.WEST);
		int x_right = farthest(rc, loc, Direction.EAST);
		int y_up = farthest(rc, loc, Direction.NORTH);
		int y_down = farthest(rc, loc, Direction.SOUTH);
		
		int pastrs = 0;
		int next = 0;
		pastrs = rc.sensePastrLocations(rc.getTeam()).length;
		
		MapLocation goal = new MapLocation(mapX - 3, initialLoc.y);
		
		int[] choices = {x_left, x_right, y_up, y_down};
		Arrays.sort(choices);

		
		//Broadcast to channel 10 which axes you moved in last
		//1 means up and down, y axis, 7 means right and left, x axis
		if (next==x_left) {
			goal = loc.add(-x_left+3, 0);
			//System.out.println("FINDGOAL set goal : " + goal.x + "and " + goal.y);
			rc.broadcast(10, 7);
		} else if (next==x_right) {
			goal = loc.add(x_right-3, 0);
			//System.out.println("FINDGOAL set goal : " + goal.x + "and " + goal.y);
			rc.broadcast(10, 7);
		} else if (next==y_up) {
			goal = loc.add(0, -y_up+3);
			//System.out.println("FINDGOAL set goal : " + goal.x + "and " + goal.y);
			rc.broadcast(10, 1);
		} else if (next==y_down) {
			goal = loc.add(0, y_down-3);
			//System.out.println("FINDGOAL set goal : " + goal.x + "and " + goal.y);
			rc.broadcast(10, 1);
		}
		
		for (int i:choices) {
			//System.out.println("DISTANCES : " + i);
		}

		int goalint = locToInt(goal);
		
		rc.broadcast(168+pastrs, goalint);
		//System.out.println("FINDGOAL braodcast goal : " + goalint);
		
		return goal;
		
	}


	private static int farthest(RobotController rc, MapLocation loc, Direction d) {
		// TODO Auto-generated method stub
		
		int clear = 0;
		//eventually myHQ will be HQ.myHQ and arg rc can go and this next line can go
		MapLocation myHQ = rc.senseHQLocation();
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		if (d==Direction.WEST) {
			for(int i = loc.x; i > 0; i--){
				TerrainTile a = rc.senseTerrainTile(new MapLocation(i, loc.y));
				if (a.equals(TerrainTile.VOID)||loc.distanceSquaredTo(enemyHQ)<30) {
					return clear;
				} else if (a.equals(TerrainTile.NORMAL)||a.equals(TerrainTile.ROAD)) {
					clear +=1;
				}
//				else if (a.equals(TerrainTile.ROAD)) {
//					clear +=2;
//				}
			}
		} else if (d==Direction.EAST) {
			//NOTE TO SELF 100 will have to change to HQ.MapWidth in the origrestructured
			for(int i = loc.x; i<100; i++){
				TerrainTile a = rc.senseTerrainTile(new MapLocation(i, loc.y));
				if (a.equals(TerrainTile.VOID)||loc.distanceSquaredTo(enemyHQ)<30) {
					return clear;
				} else if (a.equals(TerrainTile.NORMAL)||a.equals(TerrainTile.ROAD)) {
					clear +=1;
				}
//				else if (a.equals(TerrainTile.ROAD)) {
//					clear +=2;
//				}
			}
		} else if (d==Direction.NORTH) {
			for(int i = loc.y; i>0; i--){
				TerrainTile a = rc.senseTerrainTile(new MapLocation(loc.x, i));
				if (a.equals(TerrainTile.VOID)||loc.distanceSquaredTo(enemyHQ)<30) {
					return clear;
				} else if (a.equals(TerrainTile.NORMAL)||a.equals(TerrainTile.ROAD)) {
					clear +=1;
				}
//				else if (a.equals(TerrainTile.ROAD)) {
//					clear +=2;
//				}
			}
		} else if (d==Direction.SOUTH) {
			for(int i = loc.y; i<100; i++){
				TerrainTile a = rc.senseTerrainTile(new MapLocation(loc.x, i));
				if (a.equals(TerrainTile.VOID)||loc.distanceSquaredTo(enemyHQ)<30) {
					return clear;
				} else if (a.equals(TerrainTile.NORMAL)||a.equals(TerrainTile.ROAD)) {
					clear +=1;
				}
//				else if (a.equals(TerrainTile.ROAD)) {
//					clear +=2;
//				}
			}
		}
		
		////System.out.println("FARTHEST FOUND : " + clear);
		return clear;
		
	}


	private static void runSoldier(MapLocation goal, RobotController rc) throws GameActionException {
		
		//System.out.println("SOLDIER REPORTING; TRIED TO MOVE");
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
			//Get y right
			if (y==goal.y){
				int pastrs = rc.sensePastrLocations(rc.getTeam()).length;
				if(rc.isActive()&&pastrs<5){
    				rc.construct(RobotType.PASTR);
    				System.out.println(x + " AND " + y);
    				//8888 means that you've successfully gotten a pastr where you want it,
    				//kicking off another
    				//findgoal method
    				rc.broadcast(168, 8888);
    			}
			}else {
				if (y < goal.y) {
	    			toGoal = Direction.SOUTH;
	    		} else {
	    			toGoal = Direction.NORTH;
	    		}
	    		if(rc.isActive()&&rc.canMove(toGoal)){
	        		rc.move(toGoal);
	        	} else {
	        		tryToMove();
	        	}
			}
		}
		else {
			toGoal = Direction.WEST;
			if(rc.isActive()&&rc.canMove(toGoal)){
	    		rc.move(toGoal);
	    	} else {
	    		tryToMove();
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
			if (chance%teams == 1) {
				MapLocation target = new MapLocation(X, Y);
				runSoldier(target, rc);
				////System.out.println("SOLDIER ATTACKING MODE");
			}
			else {
				for (int i = 1;i<teams;i++){
					if (chance%teams ==i) {
						int ID = rc.getRobot().getID();
						//helppastr(rc, mypastrs[i]);
						tryToMove();
						////System.out.println("SOLDIER DEFENDING PASTR" + i);
						
				}
				
				}
			}
		} else {
			int ID = rc.getRobot().getID();
			tryToMove();
			//helppastr(rc, mypastrs[0]);
		}
	}
	
	private static void helppastr(RobotController rc, MapLocation pastr) throws GameActionException {
		// TODO Auto-generated method stub
		
		//runSoldier(pastr, rc);
		//two modes - heading back to HQ and heading back to pastr
		MapLocation myHQ = rc.senseHQLocation();
		MapLocation loc = rc.getLocation();
		int ID = rc.getRobot().getID();
		int mode = rc.readBroadcast(ID);
		
		MapLocation goal;
		MapLocation newgoal;
		
		Direction toGoal;
		////System.out.println("This is the mode " + mode + " on channel " + ID);
		
		if (mode==111) {
			//run to pastr
			goal = pastr;
			if (loc.y < goal.y) {
    			toGoal = Direction.SOUTH;
    		} else if (loc.x < goal.x) {
    			toGoal = Direction.EAST;
    		} else if (loc.x > goal.x) {
    			toGoal = Direction.WEST;
    		} else {
    			toGoal = Direction.NORTH;
    		}
    		if(rc.isActive()&&rc.canMove(toGoal)){
        		rc.move(toGoal);
        	} else {
        		if (loc.y == goal.y) {
    				rc.broadcast(ID, 999);
    				rc.yield();
    			} else {
            		tryToMove();
    			}
        	}
		}
		else if (mode ==999){
			newgoal = myHQ;
			if (loc.y < newgoal.y) {
    			toGoal = Direction.SOUTH;
    		} else {
    			toGoal = Direction.NORTH;
    		}
    		if(rc.isActive()&&rc.canMove(toGoal)){
        		rc.move(toGoal);
        	} else {
        		if (loc.y == newgoal.y) {
    				rc.broadcast(ID, 111);
    				rc.yield();
    			} else {
            		tryToMove();
    			}
        	}
    		
			////System.out.println("RUN BACK TO HQ! at: " + myHQ.x + myHQ.y);
			
			//robots runs back in the y direction
		}
		else {
			toGoal = Direction.WEST;
			if(rc.isActive()&&rc.canMove(toGoal)){
        		rc.move(toGoal);
        	}
		}
		

		
		//tryToMove();
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
			if(randall.nextDouble()<0.0001&&rc.sensePastrLocations(rc.getTeam()).length<5){
				//rc.senseCowsAtLocation(arg0);
				if(rc.isActive()){
					rc.construct(RobotType.PASTR);
					System.out.println("OMG BUGGGG");
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