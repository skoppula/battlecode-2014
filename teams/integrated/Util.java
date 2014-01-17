package dragon;

import java.util.ArrayList;
import java.util.Random;

import dragon.RobotPlayer;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Util {
	
    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};
    static Random rand = new Random();
	public static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
	static boolean coastIsClear = true;
    
    public static int indexOfMin(int... arr) {
        int idx = -1;
        int p = Integer.MAX_VALUE;
        for(int i = 0; i < arr.length; i++)
            if(arr[i] < p) {
                p = arr[i];
                idx = i;
            }
        return idx;
    }
    
	static void cornerMove(RobotController rc) throws GameActionException {
		//For some reason this shows preference for corners, and random twitching
		for (int i = 0;i<7;i++) {
			Random rand = new Random(rc.getRobot().getID());
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	public static MapLocation valueOf(MapLocation a){
		return new MapLocation(a.x, a.y);
	}
	
	public static void RUNEVERYTURN(RobotController rc) throws GameActionException{ //PUT STUFF HERE YOU WANT TO RUN ERRY TURN
		System.out.println("Move run" + coastIsClear);
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
		Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,rc.getType().sensorRadiusSquared*2,rc.getTeam());//was 
		while(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			coastIsClear = false;
			MapLocation[] enemyRobotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc, true);
			if(enemyRobotLocations.length==0){//only HQ is in view
				//ok no problem
			}else{//shootable robots are in view
				MapLocation closestEnemyLoc = VectorFunctions.findClosest(enemyRobotLocations, rc.getLocation());
				boolean closeEnoughToShoot = closestEnemyLoc.distanceSquaredTo(rc.getLocation())<=rc.getType().attackRadiusMaxSquared;
				if((alliedRobots.length+1)>=enemyRobots.length){//attack when you have superior numbers
					attackClosest(closestEnemyLoc);
				}else{//otherwise regroup
					regroup(enemyRobots,alliedRobots,closestEnemyLoc);
				}
			}
			rc.yield();
		}
    }
	
	private static void regroup(Robot[] enemyRobots, Robot[] alliedRobots,
			MapLocation closestEnemyLoc) {
		// TODO Auto-generated method stub
		
	}

	private static void attackClosest(MapLocation closestEnemyLoc) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("incomplete-switch")
	public static Direction[] tryDirections(RobotController rc, Direction toDest, MapLocation dest){ //this method basically just returns a list of directions i think it should try when stuck. just logic'ed it out here.
		Direction[] res = new Direction[]{toDest.opposite()};
		switch(toDest){
			case NORTH:
				Direction nfirst;
				Direction nsecond;
				int dx = rc.getLocation().x - dest.x;
				if(dx>0){ //current x greater than dest.x
					nfirst = Direction.WEST;
				}
				else{ //current x is less than dest.x OR rarely that both are equal
					nfirst = Direction.EAST;
				}
				
				if(nfirst == Direction.WEST){
					nsecond = Direction.EAST;
				}
				else{
					nsecond = Direction.WEST;
				}
				
				res = new Direction[]{nfirst, nsecond, Direction.SOUTH};
				break;
			case NORTH_EAST:
				res = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
				break;
			case EAST:
				Direction efirst;
				Direction esecond;
				int edy = rc.getLocation().y - dest.y;
				if(edy>0){ //current y greater than dest.y
					efirst = Direction.SOUTH;
				}
				else{ //current x is less than dest.x OR rarely that both are equal
					efirst = Direction.NORTH;
				}
				
				if(efirst == Direction.SOUTH){
					esecond = Direction.NORTH;
				}
				else{
					esecond = Direction.SOUTH;
				}
				
				res = new Direction[]{efirst, esecond, Direction.WEST};
				break;
			case SOUTH_EAST:
				res = new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
				break;
			case SOUTH:
				Direction sfirst;
				Direction ssecond;
				int sdx = rc.getLocation().x - dest.x;
				if(sdx>0){ //current x greater than dest.x
					sfirst = Direction.WEST;
				}
				else{ //current x is less than dest.x OR rarely that both are equal
					sfirst = Direction.EAST;
				}
				
				if(sfirst == Direction.WEST){
					ssecond = Direction.EAST;
				}
				else{
					ssecond = Direction.WEST;
				}
				
				res = new Direction[]{sfirst, ssecond, Direction.NORTH};
				break;
			case SOUTH_WEST:
				res = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
				break;
			case WEST:
				Direction wfirst;
				Direction wsecond;
				int wdy = rc.getLocation().y - dest.y;
				if(wdy>0){ //current y greater than dest.y
					wfirst = Direction.SOUTH;
				}
				else{ //current x is less than dest.x OR rarely that both are equal
					wfirst = Direction.NORTH;
				}
				
				if(wfirst == Direction.SOUTH){
					wsecond = Direction.NORTH;
				}
				else{
					wsecond = Direction.SOUTH;
				}
				
				res = new Direction[]{wfirst, wsecond, Direction.EAST};
				break;
			case NORTH_WEST:
				res = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
				break;
				
		}
		return res;
	}
	
	public static void unstick(RobotController rc, Direction toDest, MapLocation dest) throws GameActionException{
		System.out.println("Trying to move " + toDest + " towards (" + dest.x + ", " + dest.y + ")");
		Direction[] directions = tryDirections(rc, toDest, dest);

		for(Direction tryDir: directions){ //think of ways that would make sense to try, ordered by likelihood of finding opening
			
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
				RUNEVERYTURN(rc);
				if(rc.isActive()){
					System.out.println("Moving " + tryDir);
					rc.move(tryDir);
				}
				rc.yield();
			}
			if(rc.canMove(tryDir) == false){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
				System.out.println("Can't move " + tryDir);
				continue;
			}
			if(rc.canMove(toDest)){
				System.out.println("found hole");
				if(rc.isActive()&&rc.canMove(toDest)){
					rc.move(toDest);
					System.out.println("Moving toDest");
				}
				else{
					rc.yield();
					if (rc.isActive()&&rc.canMove(toDest)){
						rc.move(toDest);
					}
					System.out.println("Took a nap and then moved toDest");
					break;
				}
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
		// TODO Auto-generated method stub
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);

    	while(rc.getLocation().equals(dest)==false){
    		RUNEVERYTURN(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait, I've been here before
    					Random randint = new Random();

    					while(rc.canMove(toDest) == false&& rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent()).length==0){
    						Direction randdir = allDirections[randint.nextInt(7)];
        					System.out.println("I'm stuck. Trying random direction " + randdir);
        					while(rc.canMove(randdir)&&rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent()).length==0){
        						if(rc.isActive()){
        							rc.move(randdir);
        						}
        						rc.yield();	
        					}
    					}
    				}
    				else{
    					beforelaststuck = valueOf(laststuck);
    					laststuck = rc.getLocation();
    					System.out.println("UNSTICKING");
    					unstick(rc, toDest, dest); //unstick it
    					toDest = rc.getLocation().directionTo(dest);
    				}
    			}
    		rc.yield();
    		}
    	}//until rc.getLocation.equals(dest)
	}
	
	public static void channelMove(RobotController rc) throws GameActionException{
		int x = rc.readBroadcast(rc.getRobot().getID());
		int team = VectorFunctions.getSquad(x); //Ash test
		MapLocation dest = VectorFunctions.intToLoc(rc.readBroadcast(team));
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		RUNEVERYTURN(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		x = rc.readBroadcast(rc.getRobot().getID());
    		team = VectorFunctions.getSquad(x); //Ash test
    		dest = VectorFunctions.intToLoc(rc.readBroadcast(team));
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait, I've been here before
    					Random randint = new Random();
    					while(rc.canMove(toDest) == false){
    						Direction randdir = allDirections[randint.nextInt(7)];
        					System.out.println("I'm stuck. Trying random direction " + randdir);
        					while(rc.canMove(randdir)){
        						if(rc.isActive()){
        							rc.move(randdir);
        						}
        						rc.yield();	
        					}
    					}
    				}
    				else{
    					beforelaststuck = valueOf(laststuck);
    					laststuck = rc.getLocation();
    					System.out.println("UNSTICKING");
    					unstick(rc, toDest, dest); //unstick it
    					toDest = rc.getLocation().directionTo(dest);
    				}
    			}
    		rc.yield();
    		}
    	}//until rc.getLocation.equals(dest)
	}
	
	public static void sneakunstick(RobotController rc, Direction toDest, MapLocation dest) throws GameActionException{
		System.out.println("Trying to sneak " + toDest + " towards (" + dest.x + ", " + dest.y + ")");
		Direction[] directions = tryDirections(rc, toDest, dest);
		for(Direction tryDir: directions){ //think of ways that would make sense to try, ordered by likelihood of finding opening
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
				RUNEVERYTURN(rc);
				if(rc.isActive()){
					System.out.println("Sneaking " + tryDir);
					rc.sneak(tryDir);
				}
				rc.yield();
			}
			if(rc.canMove(tryDir) == false){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
				System.out.println("Can't sneak " + tryDir);
				continue;
			}
			if(rc.canMove(toDest)){
				System.out.println("found hole");
				if(rc.isActive()){
					rc.sneak(toDest);
					System.out.println("Sneaking toDest");
				}
				else{
					rc.yield();
					rc.yield();
					if (rc.isActive()){
						rc.sneak(toDest);
					}
					System.out.println("Took a nap and then sneaked toDest");
					break;
				}
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static void sneakTo(RobotController rc, MapLocation dest) throws GameActionException {
		// TODO Auto-generated method stub
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		RUNEVERYTURN(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.sneak(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait... I've been here before
    					Random randint = new Random();
    					while(rc.canMove(toDest) == false){
							Direction randdir = allDirections[randint.nextInt(7)]; //try a random direction to go in to break from oscillation
							System.out.println("I'm stuck. Trying random direction " + randdir);
							while(rc.canMove(randdir)){
								if(rc.isActive()){
									rc.sneak(randdir);
								}
								rc.yield();
							}
    					}
    				}
    				else{
    					beforelaststuck = valueOf(laststuck);
    					laststuck = rc.getLocation();
    					System.out.println("UNSTICKING");
    					sneakunstick(rc, toDest, dest); //unstick it
    					toDest = rc.getLocation().directionTo(dest);
    				}
    			}
    		rc.yield();
    		}
    	}//until rc.getLocation.equals(dest)
	}
	
	public static void channelSneak(RobotController rc) throws GameActionException{
		int x = rc.readBroadcast(rc.getRobot().getID());
		int team = VectorFunctions.getSquad(x); //Ash test
		MapLocation dest = VectorFunctions.intToLoc(rc.readBroadcast(team));
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		RUNEVERYTURN(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		x = rc.readBroadcast(rc.getRobot().getID());
    		team = VectorFunctions.getSquad(x); //Ash test
    		dest = VectorFunctions.intToLoc(rc.readBroadcast(team));
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.sneak(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait... I've been here before
    					Random randint = new Random();
    					while(rc.canMove(toDest) == false){
							Direction randdir = allDirections[randint.nextInt(7)]; //try a random direction to go in to break from oscillation
							System.out.println("I'm stuck. Trying random direction " + randdir);
							while(rc.canMove(randdir)){
								if(rc.isActive()){
									rc.sneak(randdir);
								}
								rc.yield();
							}
    					}
    				}
    				else{
    					beforelaststuck = valueOf(laststuck);
    					laststuck = rc.getLocation();
    					System.out.println("UNSTICKING");
    					sneakunstick(rc, toDest, dest); //unstick it
    					toDest = rc.getLocation().directionTo(dest);
    				}
    			}
    		rc.yield();
    		}
    	}//until rc.getLocation.equals(dest)
	}
	

	public static void randomMove(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
		for (int i = 0;i<7;i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	
	static void shootNearby(RobotController rc) throws GameActionException {
		//shooting
		Robot[] enemyRobots = null;
		Robot[] enemyThings = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent()); //senses all enemy units (including HQ) on map
		ArrayList<Robot> enemyUnits = new ArrayList<Robot>();
		for(Robot unit: enemyThings){ //for every unit...
			RobotInfo enemyInfo = rc.senseRobotInfo(unit);
			if(enemyInfo.type != RobotType.HQ){ //if the unit is not a HQ
				enemyUnits.add(unit); //add it to an arraylist of things to attack
			}
		enemyRobots = enemyUnits.toArray(new Robot[enemyUnits.size()]); //add it to the array of things to attack
			
		}
		
		if(enemyRobots !=null){//if there are enemies
			Robot anEnemy = enemyRobots[0];
			RobotInfo anEnemyInfo;
			anEnemyInfo = rc.senseRobotInfo(anEnemy);
			if(anEnemyInfo.location.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){
				if(rc.isActive()){
					rc.attackSquare(anEnemyInfo.location);
				}
			}
		}
	}

	static Direction findDirToMove(RobotController rc){
		for(Direction dir:allDirections){
			if(rc.canMove(dir)){
				return dir;
			}
		}
		return null;
	}
	
	public static void tryToSpawn(RobotController rc) throws GameActionException {
		if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
			for(int i=0;i<8;i++){
				Direction trialDir = allDirections[i];
				if(rc.canMove(trialDir)){
					rc.spawn(trialDir);
					break;
				}
			}
		}
	}
	
	static void tryToMove(RobotController rc) throws GameActionException {
		// TODO Auto-generated method stub
		for (int i = 0;i<7;i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	static void simpleMove(RobotController rc, Direction chosenDirection) throws GameActionException{
		if(rc.isActive()){
			for(int directionalOffset:directionalLooks){
				int forwardInt = chosenDirection.ordinal();
				Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
				if(rc.canMove(trialDir)){
					rc.move(trialDir);
					break;
				}
			}
		}
	}
	
	

}
