package lotus;

import battlecode.common.MapLocation;

public class Channels {
	
	/*
	 * Channel 0: spawning signal: squad*100+type
	 * Channel 1: distress: [SS][T][SS][T]...SS=squad, and T = type of distressed robots
	 * Channel 2: current pastures and noise towers: [D][D][D]...where D=0 indicates no PASTR/NT, D=1 indicates PASTR, D=2 PASTR & NT set up 
	 * Channel 3-9: defensive squad locations & corresponding PASTR/NT locations: [A][XX][YY], A = count robots in squad, (XX,YY) = target
	 * Channel 10: ARE WE BEING RUSHED/
	 * Channel 11-19: offensive squad locations & corresponding PASTR/NT locations
	 * Channel 20: scout counter [#moves][XXYY][AA][1 or 0], AA = id, 1 = journey done, 0 = still en-route
	 * 
	 * Channel [ID]: assignment
	 * Channel [ID+1] for PASTRS: round number it was spawned
	 */
	
	
	static int spawnChannel = 0;
	static int pastrChannel = 2;
	static int firstDefenseChannel = 3;
	static int lastDefenseChannel = 9;
	static int firstOffenseChannel = 11;
	static int lastOffenseChannel = 19;
	//static int spawnNext = 10; //receives squad number for things that are dying. 
	//Attackers are on channels 11-19
	static int scoutChannel = 20;
	static int lastNTChannel = 50;
	static int NTexistenceChannel = 51;
	static int areaSafeChannel = 52;
	static int rushSuccess = 100; //channel that we broadcast to if our rush was a success
	static int failedPastr = 101; //channel that triggers reactive rush
	static int strategyChannel = 30;
	
	static int scoutEncoding(MapLocation m, int id, int status) {
		return Conversion.mapLocationToInt(m)*1000 + (id%100)*10 + status;
	}
	
	static int scoutEncoding(int loc, int id, int status) {
		return loc*1000 + (id%100)*10 + status;
	}
	
	static int[] scoutDecoding(int i) {
		int[] a = {(i/1000), (i/10)%100, i%10};
		return a;
	}
}
