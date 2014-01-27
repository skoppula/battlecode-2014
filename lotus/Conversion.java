package lotus;

import battlecode.common.MapLocation;

public class Conversion {
	static int coordinatesToInt(int x, int y){
		return x*100 + y;
	}
	
	static MapLocation intToMapLocation(int c){
		return new MapLocation(c/100, c%100);
	}
	
	static int mapLocationToInt(MapLocation m){
		return (m.x*100 + m.y);
	}
}
