import java.util.ArrayList;

public class Alumni {
    public String name;
    ArrayList<String> priorities = new ArrayList<String>(); //can contain zip, academic area, or co-curricular activities
    public String pOne;
    public String pTwo;
    public String pThree;
    public int numLetters;
    
    public void assignPriority(String priorityValue) {
    	if(priorityValue.contains("(Pre-Engineering)")) priorityValue = priorityValue.replace("(Pre-Engineering)","");
		if(priorityValue.contains("(Studio)")) priorityValue = priorityValue.replace("(Studio)","");
		priorities.add(priorityValue);
	}
    
    public String toString() {
    	return name + "\n" + priorities + "\n" + pOne + "\n" + pTwo + "\n" + pThree + "\n" + numLetters + "\n";
    }
}
