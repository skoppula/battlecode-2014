package lotus;

public class Util {
	
	public static boolean areAllFalse(boolean[] array) {
	    for(boolean b : array)
	    	if(b) return false;
	    
	    return true;
	}
	
}
