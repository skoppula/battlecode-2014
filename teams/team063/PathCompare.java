package team063;

import java.util.Comparator;

class PathCompare implements Comparator<searchNode>{
	public int compare(searchNode o1, searchNode o2){
		return (Integer)o1.cost-(Integer)o2.cost;
	}
}
