package findPastureLocations;

//this code mostly focuses on finding the areas of greatest cow density. It does mostly nothing else. Not even move the soldiers in that direction.

import battlecode.common.*;

public class RobotPlayer{
	
	public static void run(RobotController rc){
		
		double cowDensMap[][] = rc.senseCowGrowth();
		//Get dimensions of map
		int mapY = cowDensMap.length, mapX = cowDensMap[0].length;
		MapLocation pastureLocs[] = findPastureLocs(cowDensMap, mapY, mapX, 5);
		
	}
	
	//Finds best pasture locations
	private static MapLocation[] findPastureLocs(double cowmap[][], int mapY, int mapX, int numPastures) {
		
		MapLocation pstrLocs[] = new MapLocation[numPastures];
		int pstrCowDens[] = new int[numPastures];
		
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

		return pstrLocs;
	}
}
