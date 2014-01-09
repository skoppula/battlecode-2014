package dayonetesting2;

//this code mostly focuses on finding the areas of greatest cow density. It does mostly nothing else. Not even move the soldiers in that direction.

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc){
		//Setup the global variables
		
		//Establish directions
		Direction allDirections[] = Direction.values();
		for (int i = 0; i < allDirections.length; i ++) {
			System.out.println(allDirections[i]);
		}
		
		//Start the main method - I'll ignore the try catch things for now
		while (true) {
			if(rc.getType()==RobotType.HQ){//if I'm a headquarters
				try {
					rc.spawn(allDirections[(int)(Math.random()*8)]);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(rc.getType()==RobotType.SOLDIER){
				//Cowmap sense Cow Growth
				double cowmap[][] = rc.senseCowGrowth();
				int xmax1 = 0;
				int ymax1 = 0;
				for (int i = 0; i < cowmap.length; i++) {					
					double cowmapx[] = cowmap[i];
				
					double max = cowmapx[0];
					for (int j = 1; j < cowmapx.length; j++) {
						if (cowmapx[i] > max) {
							max = cowmapx[i];
							xmax1 = j;
							ymax1 = i;
							//Here we should probably sort them in another array that lists the locations with the highest cow growth
						}
					}
				}
				//Move to the MapLocation goal
				//This needs to be fixed --->

				MapLocation goal = new MapLocation(xmax1, ymax1);
				Direction chosenDirection = (rc.getLocation()).directionTo(goal);
				//Direction chosenDirection = Direction.NORTH;
				
//				if(rc.isActive()&&rc.canMove(chosenDirection)){
//					try {
//						//Robot me = rc.getRobot();
//						//THIS IS how you get the round number!!! int a = Clock.getRoundNum();
//						rc.move(chosenDirection);
//					} catch (GameActionException e) {
//						e.printStackTrace();
//					}
//				}
			}
			rc.yield();
		}
		
	}
}