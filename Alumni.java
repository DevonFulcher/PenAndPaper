package MainPackage;
import java.util.ArrayList;

public class Alumni {
    public String name;
    //the pattern of this Alumnus' state priorities
    //index 0 contains this Alumni's 1st stated priority, etc.
    //can contain zip, academic area, or co-curricular activities
    ArrayList<String> priorityTypes = new ArrayList<String>();
    //this Alumnus' ith stated priority.
    //The type of each priority matches the pattern in priorityTypes
    ArrayList<String> statedPriority = new ArrayList<String>();
    public int numLetters;
    public boolean noZip = false;
    
    public String toString() {
    	return "alumnus name: " + name + "\n" + "priority types: " 
    			+ priorityTypes + "\n" + "stated priority: " + statedPriority
    			+ "\n" + "number of letters: " + numLetters + "\n" + "noZip: " + noZip + "\n";
    }
}
