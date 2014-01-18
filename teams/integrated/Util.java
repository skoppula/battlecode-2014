package integrated;
import java.util.Random;

import battlecode.common.Clock;
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
    
	/***********SKANDA APPROVED FUNCTIONS ****************/
	
	static void moveToward(RobotController rc, MapLocation m) throws GameActionException{
		MapLocation loc = rc.getLocation();
		Direction dir = loc.directionTo(m);
		if(dir.equals(Direction.NONE) || dir.equals(Direction.OMNI))
			return;
		else if(rc.isActive() && rc.canMove(loc.directionTo(m)))
			rc.move(dir);
	}
	
	static Direction findDirToMove(RobotController rc){
		for(Direction dir:allDirections){
			if(rc.canMove(dir))
				return dir;
		}
		return null;
	}
	
	public static void randomMove(RobotController rc) throws GameActionException {
		for (int i = 0; i<7; i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive() && rc.canMove(move)){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	//Shoots any *all* nearby robots: does not coordinate shooting with other robots
	static void indivShootNearby(RobotController rc, Robot[] enemyRobots) throws GameActionException {
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		for(Robot enemy:enemyRobots){
			if(rc.isActive()) {
				RobotInfo info = rc.senseRobotInfo(enemy);
				
				if(info.location.equals(enemyHQ))
					continue;
				
				if(info.location.distanceSquaredTo(rc.getLocation()) < rc.getType().attackRadiusMaxSquared && Clock.getBytecodeNum()<2000){
					rc.attackSquare(info.location);
				}
			}
		}
	}
	
	public static int assignmentToInt(int squad, int role) {
		return squad*100+role;
	}

	static int locToInt(MapLocation m){
		return (m.x*100 + m.y);
	}
	
	//broadcast to channel ID the assignment: AABB: A = squad[01-20] and B = type[00-03]
	public static int getSquad(int i) {
		return (i/100)%100;
	}

	public static int getRole(int i) {
		return i%100;
	}
	
	public static MapLocation nearestEnemyLoc(RobotController rc, Robot[] enemyRobots, MapLocation loc) throws GameActionException {
		
		int minDist = HQ.mapX*HQ.mapY;
		MapLocation bestLoc = null;
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		
		if(rc.canSenseObject(enemyRobots[0]))
			bestLoc = rc.senseRobotInfo(enemyRobots[0]).location;
		
		for(Robot robot:enemyRobots){
			
			if(!rc.canSenseObject(robot))
				continue;
			
			RobotInfo info = rc.senseRobotInfo(robot);

			if(info.location.equals(enemyHQ))
				continue;
			
			MapLocation m = info.location;
			int dist = m.distanceSquaredTo(loc);
			if(minDist < dist){
				minDist = dist;
				bestLoc = m;
			}
		}
		
		return bestLoc;
	}
	
	/***************************/
	
	
	
	
	
	
	
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
	
	public static void toDoWhileMoving (RobotController rc) throws GameActionException{
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().attackRadiusMaxSquared, rc.getTeam().opponent());
		MapLocation loc = rc.getLocation();
		
		int val = rc.readBroadcast(rc.getRobot().getID());
		int squad = val/100;
		
		//Keep a running average location of swarm
		int squadInfo = rc.readBroadcast(squad);
		int targetX = (squadInfo/100)%100, targetY = squadInfo%100;
		int currX = (squadInfo/10000000), currY = (squadInfo/100000)%100;
		int x = (loc.x+currX)/2, y = (loc.y+currY)/2;
		
		while(enemyRobots.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
			
//			if(Math.random()>0.05)
//				Util.moveToward(rc, new MapLocation(x, y)); //Regroup
//			else
//				Util.moveToward(rc, new MapLocation(targetX, targetY));
			
			loc = rc.getLocation();
			
			MapLocation eloc = Util.nearestEnemyLoc(rc, enemyRobots, loc); //SHOULD NOT OUTPUT AN HQ LOCATION
			if(eloc == null) {
//				System.out.println("ENEMY LOCATION IS NULL");
				break;
			}
			
			int maxAttackRad = rc.getType().attackRadiusMaxSquared;
			
			if(rc.isActive() && eloc.distanceSquaredTo(rc.getLocation())<=maxAttackRad)
				rc.attackSquare(eloc);
//			else if(rc.isActive() && rc.canMove(eloc.directionTo(loc)))
//				rc.move(eloc.directionTo(loc));
			
			rc.yield();
		}
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
//		System.out.println("Trying to move " + toDest + " towards (" + dest.x + ", " + dest.y + ")");
		Direction[] directions = tryDirections(rc, toDest, dest);

		for(Direction tryDir: directions){ //think of ways that would make sense to try, ordered by likelihood of finding opening
			
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false){ //robot moves along wall to try to find way to move in toDest
				toDoWhileMoving(rc);
				if(rc.isActive()){
//					System.out.println("Moving " + tryDir);
					rc.move(tryDir);
				}
				rc.yield();
			}
			if(rc.canMove(tryDir) == false){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
//				System.out.println("Can't move " + tryDir);
				continue;
			}
			if(rc.canMove(toDest)){
//				System.out.println("found hole");
				if(rc.isActive()&&rc.canMove(toDest)){
					rc.move(toDest);
//					System.out.println("Moving toDest");
				}
				else{
					rc.yield();
					if (rc.isActive()&&rc.canMove(toDest)){
						rc.move(toDest);
					}
//					System.out.println("Took a nap and then moved toDest");
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
    		toDoWhileMoving(rc);
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
//        					System.out.println("I'm stuck. Trying random direction " + randdir);
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
//    					System.out.println("UNSTICKING");
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
		int team = getSquad(x); //Ash test
		MapLocation dest = intToLoc(rc.readBroadcast(team));
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		toDoWhileMoving(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		x = rc.readBroadcast(rc.getRobot().getID());
    		team = getSquad(x); //Ash test
    		dest = intToLoc(rc.readBroadcast(team));
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
//        					System.out.println("I'm stuck. Trying random direction " + randdir);
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
//    					System.out.println("UNSTICKING");
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
				toDoWhileMoving(rc);
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
    		toDoWhileMoving(rc);
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
	
	public static MapLocation intToLoc(int i){
		return new MapLocation(i/100,i%100);
	}
	
	public static void channelSneak(RobotController rc) throws GameActionException{
		int x = rc.readBroadcast(rc.getRobot().getID());
		int team = getSquad(x); //Ash test
		MapLocation dest = intToLoc(rc.readBroadcast(team));
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		toDoWhileMoving(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 4){
    			break;
    		}
    		x = rc.readBroadcast(rc.getRobot().getID());
    		team = getSquad(x); //Ash test
    		dest = intToLoc(rc.readBroadcast(team));
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
