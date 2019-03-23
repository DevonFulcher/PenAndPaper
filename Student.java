package MainPackage;
import java.util.*;

public class Student {
	public int ref;
	public String state; //string "TX" if state is Texas
	public String zipCode;
	public ArrayListOverrideToString<String> majorInterests;
	public ArrayListOverrideToString<String> extraCurricularInterests;
	public boolean firstGeneration;
	public boolean codyRecipient; //true if this student has been offered the Cody scholarship
	public boolean moodRecipient; //true if this student has been offered the Mood scholarship
	public int conversionScore; //ranges from 1 to 6 with students with a score of 1 and 2 being priority
	public boolean receivesLetter = false; //no student receives a letter upon construction
	public double averageMatch = 0;
	public boolean noZip; //a student has noZip if their zip code is not recognized to be a US zip. TODO: ask Mrs. Bowman if in US/out of US data is available
	public boolean noConversion; //some students do not have a specified conversion score
	public Student(int reff,String st,String zip,ArrayListOverrideToString<String> majInterests, 
			ArrayListOverrideToString<String> extraCurrInterests, boolean firstGeneration,
			boolean codyRecipient, boolean moodRecipient, 
			int conversionScore, boolean noZip, boolean noConversion) {
		this.ref = reff;
		this.state = st;
		this.zipCode = zip;
		this.majorInterests = majInterests;
		this.extraCurricularInterests = extraCurrInterests;
		this.firstGeneration = firstGeneration;
		this.codyRecipient = codyRecipient;
		this.moodRecipient = moodRecipient;
		this.conversionScore = conversionScore;
		this.noZip = noZip;
		this.noConversion = noConversion;
	}
	
	public String toString() {
		return "student id: " + ref + "\n" + "firstGen: " + firstGeneration + "\n" + "state: " + state + "\n" + "cody: " + codyRecipient 
				+ "\n" + "mood: " + moodRecipient + "\n" + "conversion score: " + conversionScore+ "\n" + "zip code: " + zipCode + "\n" 
				+ "major interests: " + majorInterests + "\n" + "extra curricular interests: " + extraCurricularInterests   + "\n" 
				+ "receives letter: " + receivesLetter + "\n" + "average match: " + averageMatch + "\n" + "noZip: " + noZip + "\n";
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