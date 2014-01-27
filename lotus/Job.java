package lotus;

import battlecode.common.MapLocation;

public class Job {
	
	MapLocation target;
	int numRobotsNeeded, numRobotsAssigned;
	boolean finished = false;
	String[] typesOfJobs = {"defense", "offense", "enemy_hq_rush"};
	String description;
	int typeOfJob;
	int squadNum;
	int startRound;
	
	public Job(MapLocation m, int numRobotsNeeded, int squadNum, String typeOfJob, int startRound){
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.typeOfJob = decipherJob(typeOfJob);
		this.numRobotsAssigned = 0;
		this.startRound = startRound;
	}
	
	public Job(MapLocation m, int numRobotsNeeded, int squadNum, String typeOfJob, int startRound, String description){
		this.target = m;
		this.numRobotsNeeded = numRobotsNeeded;
		this.squadNum = squadNum;
		this.typeOfJob = decipherJob(typeOfJob);
		this.numRobotsAssigned = 0;
		this.startRound = startRound;
		this.description = description;
	}
	
	int decipherJob(String text){
		if(text.toLowerCase().contains("defens"))
			return 0;
		else if(text.toLowerCase().contains("off"))
			return 1;
		else
			return 2;	
	}
	
	void addRobotAssigned(int num){
		numRobotsAssigned += num;
	}
}
