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
	
	int NTPASTRchannel;
	
	int startRound;
	int maxJobLength = 250;
	
	int numReinforcementsSent = 0;
	boolean startedSpawning;
	boolean finishedSpawning;
	
	public Job(int desiredPASTRs_index, MapLocation m, int numRobotsNeeded, int squadNum, int maxJobLength){
		this.desiredPASTRs_index = desiredPASTRs_index;
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.numRobotsAssigned = 0;
		this.startRound = Clock.getRoundNum();
		this.maxJobLength = maxJobLength;
		this.type = this.squadNum < Channels.firstOffenseChannel ? 0 : 1;
		
		this.NTPASTRchannel = this.squadNum + 1;
		
		System.out.println("Job spawned. squad: " + this.squadNum + " with target " + this.target);
	}
	
	public Job(MapLocation m, int numRobotsNeeded, int squadNum, int maxJobLength){
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.numRobotsAssigned = 0;
		this.startRound = Clock.getRoundNum();
		this.maxJobLength = maxJobLength;
		this.type = this.squadNum < Channels.firstOffenseChannel ? 0 : 1;
		
		this.NTPASTRchannel = this.squadNum + 1;
		System.out.println("Job spawned. squad: " + this.squadNum + " with target " + this.target);
	}
	
	void restartRobotsAssigned(int enemies) {
		numRobotsNeeded = enemies;
		numRobotsAssigned = enemies;
		startedSpawning = false;
		finishedSpawning = false;
		numReinforcementsSent++;
	}
	
	void updateTarget(MapLocation m) {
		this.target = m;
	}
	
	void addRobotAssigned(int num){
		this.numRobotsAssigned += num;
		
		if(this.numRobotsAssigned == this.numRobotsNeeded)
			this.finishedSpawning = true;
		
		else if(this.numRobotsAssigned == 1) {
			this.startedSpawning = true;
			startRound = Clock.getRoundNum(); //TODO
		}
	}

	void prepareForRemoval(RobotController rc) throws GameActionException {
		rc.broadcast(this.squadNum, 0);
		rc.broadcast(this.NTPASTRchannel, 0);
		System.out.println("Job deleted (timed out). squad: " + this.squadNum + " with target " + this.target);
	}
	
	public String toString(){
		return "Job " + startRound + "[Squad: " + squadNum + ", type: " + type + "]";
	}

	void updateSquadChannel(RobotController rc) throws GameActionException {
		rc.broadcast(squadNum, Conversion.mapLocationToInt(this.target));
	}
}
