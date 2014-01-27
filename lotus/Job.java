package lotus;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Job {
	
	int numRobotsNeeded, numRobotsAssigned;
	
	boolean ongoingPASTR = false;
	int desiredPASTRs_index;
	MapLocation target;
	
	int squadNum;
	String[] typesOfJobs = {"defense", "offense"};
	int type;
	
	int startRound;
	int maxJobLength = 250;
	
	public Job(int desiredPASTRs_index, MapLocation m, int numRobotsNeeded, int squadNum, int maxJobLength){
		this.desiredPASTRs_index = desiredPASTRs_index;
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.numRobotsAssigned = 0;
		this.startRound = Clock.getRoundNum();
		this.maxJobLength = maxJobLength;
		this.type = this.squadNum < 10 ? 0 : 1;
	}
	
	public Job(MapLocation m, int numRobotsNeeded, int squadNum, int maxJobLength){
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.numRobotsAssigned = 0;
		this.startRound = Clock.getRoundNum();
		this.maxJobLength = maxJobLength;
		this.type = this.squadNum < 10 ? 0 : 1;
	}
	
	
	void addRobotAssigned(int num){
		this.numRobotsAssigned += num;
	}

	void prepareForRemoval(RobotController rc) throws GameActionException {
		rc.broadcast(this.squadNum, 0);
	}
	
	public String toString(){
		return "Job " + startRound + "[Squad: " + squadNum + ", type: " + type + "]";
	}

	void updateSquadChannel(RobotController rc) throws GameActionException {
		rc.broadcast(squadNum, Conversion.mapLocationToInt(this.target));
	}
}
