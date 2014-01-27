package lotus;

import battlecode.common.MapLocation;

public class Channels {
	
	/*
	 * Channel 0: spawning signal: squad*100+type
	 * Channel 2: current pastures and noise towers: [D][D][D]...where D=0 indicates no PASTR/NT, D=1 indicates PASTR, D=2 PASTR & NT set up 
	 * Channel 3-9: defensive squad locations & corresponding PASTR/NT locations: [XX][YY], (XX,YY) = target
	 * Channel 10: ARE WE BEING RUSHED/
	 * Channel 11-19: offensive squad locations & corresponding PASTR/NT locations
	 * Channel 20: scout counter [#moves][XXYY][AA][1 or 0], AA = id, 1 = journey done, 0 = still en-route
	 * 
	 * Channel [ID]: assignment
	 * Channel [ID+1] for PASTRS: round number it was spawned
	 */
	
	
	static int spawnChannel = 0;
	static int firstDefenseChannel = 1;
	//Alternating squad channel, and corresponding PASTR/NT channel -> [AB], A = 0 if no NT, 1 if NT constructed, B = 0 if no PASTR
	static int lastDefenseChannel = 10;
	static int firstOffenseChannel = 11;
	static int lastOffenseChannel = 19;
	static int scoutChannel = 20;
	static int lastNTChannel = 50;
	static int NTexistenceChannel = 51;
	static int areaSafeChannel = 52;
	static int rushSuccess = 100; //channel that we broadcast to if our rush was a success
	static int failedPastr = 101; //channel that triggers reactive rush
	static int strategyChannel = 30;
	
	static int NTPASTREncoding(int NT, int PASTR) {
		return NT*10+PASTR;
	}
	
	static int[] NTPASTRDecoding(int i) {
		int[] a = {i/10, i%10}; //{NT status, PASTR status} 
		return a;
	}
	
	static int scoutEncoding(int round, MapLocation m, int status) {
		return round*100000 + Conversion.mapLocationToInt(m)*10 + status;
	}
	
	static int scoutEncoding(int round, int loc, int status) {
		return round*100000 + loc*10 + status;
	}
	
	static int[] scoutDecoding(int i) {
		int[] a = {(i/100000), (i/10)%10000, i%10}; //{current clock or num rounds to reach enemy HQ, XXYY, done/not done} 
		return a;
	}

	public static int assignmentEncoding(int squad, int role) {
		return squad*100 + role;
	}
	
	public static int[] assignmentDecoding(int assignment) {
		int[] a = {(assignment/100)%100, assignment%10}; //{squad, role}
		return a;
	}
}
