package lotus;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Move {	

    public static Direction allDirections[] = {Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.WEST, Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.EAST};
    static Random rand = new Random();
	
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
	
	public static void randomSneak(RobotController rc) throws GameActionException {
		for (int i = 0; i<7; i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive() && rc.canMove(move)){
            	rc.sneak(move);
            	
            }
    	}
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
		
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, rc.getTeam().opponent());
		MapLocation loc = rc.getLocation();
		
		while(enemyRobots.length>0){
			
			enemyRobots = rc.senseNearbyGameObjects(Robot.class, rc.getType().sensorRadiusSquared*2, rc.getTeam().opponent());
			loc = rc.getLocation();
			
			if (enemyRobots.length > 0) {
				
				MapLocation eloc = Attack.nearestEnemyLoc(rc, enemyRobots, loc); //SHOULD NOT OUTPUT AN HQ LOCATION
				
				if(eloc != null && rc.isActive() && rc.canAttackSquare(eloc))
					rc.attackSquare(eloc);
				
				else if(eloc != null && rc.isActive() && rc.canMove(loc.directionTo(eloc)))
					if (loc.distanceSquaredTo(rc.senseEnemyHQLocation()) > 36)
 						rc.move(loc.directionTo(eloc));
					
				else if (rc.isActive()) 
					tryToMove(rc);
			}

			
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
		MapLocation enemyHQ = rc.senseEnemyHQLocation();

		for(Direction tryDir: directions){ //think of ways that would make sense to try, ordered by likelihood of finding opening
			MapLocation next = rc.getLocation().add(tryDir);
			while(rc.canMove(tryDir) && rc.canMove(toDest) == false && next.distanceSquaredTo(enemyHQ) > RobotType.HQ.attackRadiusMaxSquared){ //robot moves along wall to try to find way to move in toDest
				toDoWhileMoving(rc);
				if(rc.isActive()&&rc.canMove(tryDir)){
//					System.out.println("Moving " + tryDir);
					rc.move(tryDir);
				}
				rc.yield();
			}
			if(rc.canMove(tryDir) == false || next.distanceSquaredTo(enemyHQ) < RobotType.HQ.attackRadiusMaxSquared){ //robot couldn't find a way to move in toDest before hitting another wall in tryDir direction (corner case)
//				System.out.println("Can't move " + tryDir);
				continue;
			}
			if(rc.canMove(toDest) && rc.getLocation().add(toDest).distanceSquaredTo(enemyHQ) > RobotType.HQ.attackRadiusMaxSquared){
//				System.out.println("found hole");
				if(rc.isActive()&&rc.canMove(toDest)){
					rc.move(toDest);
//					System.out.println("Moving toDest");
				}
				else{
					rc.yield();
					if (rc.isActive()&&rc.canMove(toDest) && rc.getLocation().add(toDest).distanceSquaredTo(enemyHQ) > RobotType.HQ.attackRadiusMaxSquared){
						rc.move(toDest);
					}
//					System.out.println("Took a nap and then moved toDest");
					break;
				}
			}
		}
		//robot has found an opening that allows it to move in direction toDest
	}
	
	public static int surroundingWalls(RobotController rc, MapLocation loc){
		Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		int walls = 0;
		for(Direction direction:dirs){
			if(rc.senseTerrainTile(loc.add(direction)) == TerrainTile.VOID || rc.senseTerrainTile(loc.add(direction)) == TerrainTile.OFF_MAP){
				walls++;
			}
		}
		return walls;
	}
	
	public static Direction wallFollow(RobotController rc) throws GameActionException{ //consciously following left side of wall
		Direction res = null;
		if(surroundingWalls(rc, rc.getLocation())<3){
			if(rc.isActive() && rc.canMove(Direction.WEST) && (rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.WEST);
				rc.yield();
				res = Direction.WEST;
			} else if(rc.isActive() && rc.canMove(Direction.NORTH) && (rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.NORTH);
				rc.yield();
				res = Direction.NORTH;
			} else if(rc.isActive() && rc.canMove(Direction.EAST) && (rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.EAST);
				rc.yield();
				res = Direction.EAST;
			} else if(rc.isActive() && rc.canMove(Direction.SOUTH) &&(rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.SOUTH);
				rc.yield();
				res = Direction.SOUTH;
			} else if(rc.isActive() && rc.canMove(Direction.NORTH) && (rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH_EAST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH_EAST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.NORTH);
				rc.yield();
				res = Direction.NORTH;
			} else if(rc.isActive() && rc.canMove(Direction.EAST) && (rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH_EAST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH_EAST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.EAST);
				rc.yield();
				res = Direction.EAST;
			} else if(rc.isActive() && rc.canMove(Direction.SOUTH) && (rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH_WEST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH_WEST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.SOUTH);
				rc.yield();
				res = Direction.SOUTH;
			} else if(rc.isActive() && rc.canMove(Direction.WEST) && (rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH_WEST)) == TerrainTile.VOID || rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH_WEST)) == TerrainTile.OFF_MAP)){
				rc.move(Direction.WEST);
				rc.yield();
				res = Direction.WEST;
			}
		}
		else{
			if(rc.isActive() && rc.canMove(Direction.SOUTH_WEST) && ((rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.VOID &&  rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.VOID)|| (rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.OFF_MAP && rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.OFF_MAP))){
				rc.move(Direction.SOUTH_WEST);
				rc.yield();
				res = Direction.SOUTH_WEST;
			} else if(rc.isActive() && rc.canMove(Direction.NORTH_WEST) && ((rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.VOID &&  rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.VOID)|| (rc.senseTerrainTile(rc.getLocation().add(Direction.WEST)) == TerrainTile.OFF_MAP && rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.OFF_MAP))){
				rc.move(Direction.NORTH_WEST);
				rc.yield();
				res = Direction.NORTH_WEST;
			} else if(rc.isActive() && rc.canMove(Direction.NORTH_EAST) && ((rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.VOID &&  rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.VOID)|| (rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.OFF_MAP && rc.senseTerrainTile(rc.getLocation().add(Direction.NORTH)) == TerrainTile.OFF_MAP))){
				rc.move(Direction.NORTH_EAST);
				rc.yield();
				res = Direction.NORTH_EAST;
			} else if(rc.isActive() && rc.canMove(Direction.SOUTH_EAST) && ((rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.VOID &&  rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.VOID)|| (rc.senseTerrainTile(rc.getLocation().add(Direction.EAST)) == TerrainTile.OFF_MAP && rc.senseTerrainTile(rc.getLocation().add(Direction.SOUTH)) == TerrainTile.OFF_MAP))){
				rc.move(Direction.SOUTH_EAST);
				rc.yield();
				res = Direction.SOUTH_EAST;
			}
		}
		return res;
	}
	
	@SuppressWarnings("incomplete-switch")
	public static Direction wallFollow(RobotController rc, Direction lastdir) throws GameActionException{
		System.out.println("Last Direction was " + lastdir);
		Direction res = null;
		Direction[] directions = null;
		
		if (lastdir==null){
			lastdir = Direction.NORTH;
		}
		switch(lastdir){
		case NORTH:
			directions = new Direction[]{Direction.NORTH, Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.EAST, Direction.WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST};
			break;
		case NORTH_EAST:
			directions = new Direction[]{Direction.NORTH_EAST, Direction.EAST, Direction.NORTH, Direction.SOUTH_EAST, Direction.NORTH_WEST, Direction.SOUTH, Direction.WEST};
			break;
		case EAST:
			directions = new Direction[]{Direction.EAST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.NORTH, Direction.SOUTH, Direction.SOUTH_WEST, Direction.NORTH_WEST};
			break;
		case SOUTH_EAST:
			directions = new Direction[]{Direction.SOUTH_EAST, Direction.SOUTH, Direction.EAST, Direction.NORTH_EAST, Direction.SOUTH_WEST, Direction.NORTH, Direction.WEST};
			break;
		case SOUTH:
			directions = new Direction[]{Direction.SOUTH, Direction.SOUTH_EAST, Direction.SOUTH_WEST, Direction.WEST, Direction.EAST, Direction.NORTH_WEST, Direction.NORTH_EAST};
			break;
		case SOUTH_WEST:
			directions = new Direction[]{Direction.SOUTH_WEST, Direction.SOUTH, Direction.WEST, Direction.SOUTH_EAST, Direction.NORTH_WEST, Direction.EAST, Direction.NORTH};
			break;
		case WEST:
			directions = new Direction[]{Direction.WEST, Direction.NORTH_WEST, Direction.SOUTH_WEST, Direction.NORTH, Direction.SOUTH, Direction.SOUTH_EAST, Direction.NORTH_EAST};
			break;
		case NORTH_WEST:
			directions = new Direction[]{Direction.NORTH_WEST, Direction.NORTH, Direction.WEST, Direction.NORTH_EAST, Direction.SOUTH_WEST, Direction.SOUTH, Direction.EAST};
			break;
		default:
			directions = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};break;
		}
		for(Direction direction:directions){
			if(rc.isActive() && rc.canMove(direction) && surroundingWalls(rc, rc.getLocation().add(direction)) > 0){
				rc.move(direction);
				rc.yield();
				res = direction;
				break;
			}
		}
		return res;
	}
	
	public static int countWalls(RobotController rc, MapLocation dest) {
		int wall = 0;
		Direction toDest = rc.getLocation().directionTo(dest);
		int distance = (int) Math.sqrt(rc.getLocation().distanceSquaredTo(dest));
		for(int i = 1; i < distance; i++){
			TerrainTile tile = rc.senseTerrainTile(rc.getLocation().add(toDest, i));
			if(tile == TerrainTile.valueOf("VOID")){
				wall++;
			}
		}
		return wall;
	}
	
	public static void breakCycle(RobotController rc, MapLocation dest) throws GameActionException{
		MapLocation last = rc.getLocation();
		Direction lastdir, beforelast = null;
		rc.yield();
		lastdir = wallFollow(rc);
		int wallsToDest = countWalls(rc, dest);
		while(countWalls(rc, dest) >= wallsToDest){//distances should be getting closer to destination, so delta.get[0] should be negative
			if(rc.isActive()){
				if(lastdir != null){
					beforelast = lastdir;
					lastdir = wallFollow(rc, lastdir);
				}else
					lastdir = wallFollow(rc, beforelast);
				rc.yield();
				last = valueOf(rc.getLocation());
			}
			rc.yield();
		}

		if(countWalls(rc, dest) < wallsToDest){ //what conditions do we break out?
			//System.out.println("Broke free");
		}
	}
	
	public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
		
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	MapLocation next = rc.getLocation().add(toDest);
    	MapLocation enemyHQ = rc.senseEnemyHQLocation();

    	while(rc.getLocation().equals(dest)==false){
    		toDoWhileMoving(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 9){
    			break;
    		}
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && (rc.canMove(toDest) == false || next.distanceSquaredTo(enemyHQ)<RobotType.HQ.attackRadiusMaxSquared)){ //if robot can't move toDest either because there's a wall or HQ is in way...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait, I've been here before
    					breakCycle(rc, dest);
    					System.out.println("temporary fix");
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
		int team = Channels.assignmentDecoding(x)[0]; //Ash test
		MapLocation dest = intToLoc(rc.readBroadcast(Math.abs(team)));
		MapLocation laststuck = new MapLocation(0,0);
		MapLocation beforelaststuck = new MapLocation(0,0);
    	Direction toDest = rc.getLocation().directionTo(dest);
    	while(rc.getLocation().equals(dest) == false){
    		toDoWhileMoving(rc);
    		if(rc.getType() == RobotType.SOLDIER && rc.getLocation().distanceSquaredTo(dest) < 9){
    			break;
    		}
    		x = rc.readBroadcast(rc.getRobot().getID());
    		team = Channels.assignmentDecoding(x)[0]; //Ash test
    		dest = intToLoc(rc.readBroadcast(Math.abs(team)));
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.move(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait, I've been here before
    					breakCycle(rc, dest);
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
    					breakCycle(rc, dest);
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
		return new MapLocation((i/100)%100,i%100);
	}
	
	public static void channelSneak(RobotController rc) throws GameActionException{
		int x = rc.readBroadcast(rc.getRobot().getID());
		int team = Channels.assignmentDecoding(x)[0]; //Ash test
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
    		team = Channels.assignmentDecoding(x)[0]; //Ash test
    		dest = intToLoc(rc.readBroadcast(team));
    		if(rc.isActive() && rc.canMove(toDest)){
    			rc.sneak(toDest);
    			rc.yield();
    			toDest = rc.getLocation().directionTo(dest);
    		}else{ //robot is either inactive or can't move toDest
    			if(rc.isActive() && rc.canMove(toDest) == false){ //if robot can't move toDest...
    				if(laststuck.equals(rc.getLocation()) || beforelaststuck.equals(rc.getLocation())){ //wait... I've been here before
    					breakCycle(rc, dest);
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
		for (int i = 0;i<7;i++) {
    		Direction move = allDirections[(int)(rand.nextDouble()*8)];
            if(rc.isActive()&&rc.canMove(move)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            	rc.move(move);
            	break;
            }
    	}
	}
	
	

}
