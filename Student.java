import java.util.*;

public class Student {
	public int ref;
	public String state; //string "TX" if state is Texas
	public String zipCode;
	public ArrayList<String> majorInterests;
	public ArrayList<String> extraCurricularInterests;
	public boolean firstGeneration;
	public boolean codyRecipient; //true if this student has been offered the Cody scholarship
	public boolean moodRecipient; //true if this student has been offered the Mood scholarship
	public int conversionScore; //ranges from 1 to 6 with students with a score of 1 and 2 being priority
	public boolean receivesLetter = false; //no student receives a letter upon construction
	public double averageMatch = 0;
	public boolean noZip;
	public Student(int reff,String st,String zip,ArrayList<String> majInterests, 
			ArrayList<String> extraCurrInterests, boolean firstGeneration,
			boolean codyRecipient, boolean moodRecipient, int conversionScore,boolean flag) {
		this.ref = reff;
		this.state = st;
		this.zipCode = zip;
		this.majorInterests = majInterests;
		this.extraCurricularInterests = extraCurrInterests;
		this.firstGeneration = firstGeneration;
		this.codyRecipient = codyRecipient;
		this.moodRecipient = moodRecipient;
		this.conversionScore = conversionScore;
		this.noZip = flag;
	}
	
	public String toString() {
		return ref + "\n" + "firstGen: " + firstGeneration + "\n" + state + "\n" + "cody: " + codyRecipient 
				+ "\n" + "mood: " + moodRecipient + "\n" + conversionScore+ "\n" + zipCode + "\n" + majorInterests + "\n" + extraCurricularInterests   + "\n" 
				+ "receivesLetter: " + receivesLetter + "\n" + "averageMatch: " + averageMatch + "\n" + "noZip: " + noZip + "\n";
	}
	
	static class StudentAverageMatchComparator implements Comparator<Student> {
		@Override
		public int compare(Student a, Student b) {
			if (a.averageMatch < b.averageMatch) {
				return -1;
			} else if (a.averageMatch == b. averageMatch) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}