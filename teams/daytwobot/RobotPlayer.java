package daytwobot;

//this code mostly focuses on finding the areas of greatest cow density. It does mostly nothing else. Not even move the soldiers in that direction.
//for some reason every robot is using 10000 bytecodes. Fix this please.

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer{
	
		static Random randall = new Random(); //Random number generator
        public static RobotController rc; //Make rc available to all the methods
    	static Direction allDirections[] = Direction.values(); //Directions
        
        public static void run(RobotController rcin){
            //Run the robot that the engine gives you
        	rc = rcin;   
        	
        	//Before while loop, initialize several global variables such as cowDensMap, terrainMap, random numbers, etc
        	
        	//Initialize cow density map array? matrix?
            double cowDensMap[][] = rc.senseCowGrowth();
                
            //Get dimensions of map
            int mapY = cowDensMap.length, mapX = cowDensMap[0].length;
            MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
                
        	//Initialize terrain map array
        	int terrainMap[][] = new int[mapY][mapX];
        	draw(terrainMap);
        	
        	//Random
            randall.setSeed(rc.getRobot().getID());
            
            
            while(true){
                try{
                        if(rc.getType()==RobotType.HQ){//if I'm a headquarters
                                runHeadquarters();
                        }else if(rc.getType()==RobotType.SOLDIER){
                        		
                        		//Some initial maneuvering to get out into the middle
                        		//This needs to be changed to figure out how to factor in walls and roads, terrainMap
                        		//Replace this later
                                if (Clock.getRoundNum() < 90) {
                                	if(rc.canMove(Direction.NORTH)){
                                		rc.move(Direction.NORTH);
                                	}
                                }
                                else if ( (Clock.getRoundNum() > 100) && (Clock.getRoundNum() < 130) ) {
                                	if(rc.canMove(Direction.EAST)){
                                		rc.move(Direction.EAST);
                                	}
                                }

                                //Move toward goal
                                //This needs to be replaced with Kevin's goto function
                                //This also needs to iterate through the different pastureLocs and not just one
                                MapLocation goal = pastureLocs[3];
                                Direction d = rc.getLocation().directionTo(goal);
                                
                                if(rc.isActive()&&rc.canMove(d)){
                                        if ((int)(randall.nextDouble()) < .5) {
                                        	rc.move(d);
                                        } else {
                                        	rc.move(allDirections[(int)(randall.nextDouble()*8)]);
                                        }
                                } else {
                                		//this may have to change. WHat can you do if you're not active? o.O
                            			tryToShoot();
                                }
                                
                                if (rc.getLocation().equals(goal)) {
	                                	if(rc.isActive()){
	                                		//This isn't working
	                                        rc.construct(RobotType.PASTR);
	                                	}
                                }
                        } 
                        //Here would probably be a good idea to put a condition saying - if PASTR robot has not produced enough milk in the last 100 turns, suicide it

                        rc.yield();
                }catch (Exception e){
                        e.printStackTrace();
                }
            }
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
            }else {//if there are no enemies
            	//SWARM THE MAP!!!
            	rc.move(Direction.OMNI);
            }
        }

		//The HQ basically spawns all the robots as fast as it can
    	private static void runHeadquarters() throws GameActionException {
    		Direction spawnDir = allDirections[(int)(randall.nextDouble()*8)]; //Chooses a random direction
            if(rc.isActive()&&rc.canMove(spawnDir)&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
                    rc.spawn(spawnDir);
            }
			
		}


		//Draws the terrain map
    	private static void draw(int[][] terrainMap) {
    		
    		//Scan over map to identify types of terrain at each location
    		final int	NORMAL = 10;
    		final int	ROAD = 0;
    		final int	WALL = 1000;
    		final int	OFFMAP = -1;
    		int mapY = terrainMap.length, mapX = terrainMap[0].length;
    		
    		for(int i = 0; i < mapY; i++){
    			for(int j = 0; j < mapX; j++){
    				TerrainTile t = rc.senseTerrainTile(new MapLocation(j, i));
    				if(t==TerrainTile.valueOf("NORMAL"))
    					terrainMap[i][j] = NORMAL;
    				else if(t==TerrainTile.valueOf("ROAD"))
    					terrainMap[i][j] = ROAD;
    				else if(t==TerrainTile.valueOf("VOID"))
    					terrainMap[i][j] = WALL;
    				else
    					terrainMap[i][j] = OFFMAP;
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
    		for (int j = 0; j<100;j++) {
    			for (int i = 0; i < pstrLocs.length; i++) {
        			System.out.println("SKANDA FOUND THESE LOCATIONS: " + pstrLocs[i]);
        		}
    		}
    		

    		return pstrLocs;
    	}
    	
}

