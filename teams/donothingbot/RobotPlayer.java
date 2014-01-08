package donothingbot;
//This bot does nothing so you can see the cows, or whatever the opponent is doing

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc){
		
		while(true){
			try{
				if(rc.getType()==RobotType.HQ){//if I'm a headquarters
					System.out.println("HQ");
				}else if(rc.getType()==RobotType.SOLDIER){
					System.out.println("Soldier");
				}
				rc.yield();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}