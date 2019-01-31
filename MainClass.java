package MainPackage;
import java.util.*;
import java.io.*;


public class MainClass {
	//Experimentation is required to create ideal matching
	//If true then alumni will be matched with 1 less letter than they designate
	//otherwise alumni will be matched with the number of letters that they designate
	public static final boolean PARTIAL_MATCH_FEATURE = false;
	//The greater this value the more likely a first generation 
	//student will get a letter.
	public static final double FIRST_GENERATION_IMPORTANCE = 10;
	//The greater this value the more likely an out of state
	//student will get a letter.
	public static final double OUT_OF_STATE_IMPORTANCE = 10;
	//The greater this value the more likely a Cody scholarship
	//recipient will receive a letter. Independent of the 
	//Mood scholarship.
	public static final double CODY_SCHOLARSHIP_IMPORTANCE = 10;
	//The greater this value the more likely Mood scholarship
	//recipient will receive a letter. Independent of the
	//Mood scholarship.
	public static final double MOOD_SCHOLARSHIP_IMPORTANCE = 2;
	//The greater this value the more likely a student with a low
	//conversion score will receive a letter.
	public static final double LOW_CONVERSION_SCORE_IMPORTANCE = 10;

	//The greater this value the more likely a match will occur based on academic interest
	public static final double ACADEMIC_INTEREST_IMPORTANCE = 2;
	//The greater this value the more likely a match will occur based on co-curricular interest
	public static final double COCURRICULAR_INTEREST_IMPORTANCE = 2;
	//The greater this value the more likely a match will occur based on geographic proximity interest
	//public static final double GEOGRAPHIC_PROXIMITY_IMPORTANCE = 2;
	//change GEOGRAPHIC_PROXIMITY_IMPORTANCE to be a multiplier

	//For best results priorities should be proportional to each other

	//The greater this values the more likely a match will occur based on an alumni's priority one.
	//Should be greater than the other 2 priorities
	public static final double PRIORITY_ONE_IMPORTANCE = 9;
	//The greater this values the more likely a match will occur based on an alumni's priority two
	//Should be greater than priority 3.
	public static final double PRIORITY_TWO_IMPORTANCE = 3;
	//The greater this values the more likely a match will occur based on an alumni's priority three
	public static final double PRIORITY_THREE_IMPORTANCE = 1;

	//This is equal to the number of priorities that alumni specify
	public static final int NUM_PRIORITIES = 3;

	//Office of Admissions criteria. e.g. out of state, first gen, conversion score, scholarships, etc.
	public static final int NUM_ADMISSIONS_CRITERIA = 5;

	//This must equal the number of terms in the match function
	public static final int NUM_REASONS_TO_PRINT = NUM_PRIORITIES + NUM_ADMISSIONS_CRITERIA;

	//this designates all possible scholarships that a student could have
	//TODO: later create a read in final for this information
	public static final String[] POSSIBLE_SCHOLARSHIPS = {"Mood", "Cody", "Southwestern Award", "Ruter", "McKenzie", "University Award"};

	public static void main(String args[]) throws IOException {
		//read in data
		ArrayList<String> majorsList = ReadInData.readInMajors();
		HashMap<String, Pair<Double, Double>> zipMap = ReadInData.readInZipCodes();
		Pair<Integer, ArrayList<Alumni>> alumniPair = ReadInData.readInAlumni(zipMap, NUM_PRIORITIES);
		int totalNumLetters = alumniPair.element1;
		ArrayList<Alumni> alumniList = alumniPair.element2;
		ArrayList<Student> studentList = ReadInData.readInStudents(zipMap, POSSIBLE_SCHOLARSHIPS, majorsList);

		//process data
		int[] numStudentsForEachGroup = ProcessData.countTypesOfStudents(studentList);
		double[][][] distanceMatrix = ProcessData.createDistanceMatrix(zipMap, studentList, alumniList, NUM_PRIORITIES);
		Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> matchTriple = ProcessData.calculateMatch(
				distanceMatrix, studentList, alumniList,
				PRIORITY_ONE_IMPORTANCE, PRIORITY_TWO_IMPORTANCE, PRIORITY_THREE_IMPORTANCE,
				NUM_REASONS_TO_PRINT, NUM_PRIORITIES, 
				FIRST_GENERATION_IMPORTANCE, OUT_OF_STATE_IMPORTANCE, CODY_SCHOLARSHIP_IMPORTANCE, 
				MOOD_SCHOLARSHIP_IMPORTANCE, LOW_CONVERSION_SCORE_IMPORTANCE, ACADEMIC_INTEREST_IMPORTANCE, COCURRICULAR_INTEREST_IMPORTANCE);
		double[][] matchScores = matchTriple.element1;
		HashMap<String, Match> matchMap = matchTriple.element2;
		ValueAndReason[][][] matchReasons = matchTriple.element3;
		String[][] variableNames = ProcessData.createVariables(alumniList, studentList);

		//create and run linear program
		SolveLinearProgram.createLPFile(matchScores, variableNames, alumniList, studentList);
		System.out.println("run this command: \n"
				+ "glpsol --lp PenAndPaper.lp --ranges PenAndPaperSensitivity.txt -o PenAndPaperResults.txt \n"
				+ "in the command line within the Pen And Paper src folder then press c and enter to continue.");
		//a parameter needs to exist if the program is being run from the command line with jar
		//TODO: ensure this works
		if (args.length == 0) {
			//wait for response
			Scanner scan = new Scanner(System.in);
			scan.next();
			scan.close();
		} else {
			//TODO need code here
			System.out.println("glpsol --lp PenAndPaper.lp --ranges PenAndPaperSensitivity.txt -o PenAndPaperResults.txt");
		}

		//process results
		Triple<int[], int[], int[]> parameterTriple = ProcessResults.readResults(matchMap, studentList, alumniList, 
				matchReasons, matchScores, NUM_REASONS_TO_PRINT, NUM_PRIORITIES);
		int[] numLettersForEachGroup = parameterTriple.element1;
		int[] numLettersForEachMatch = parameterTriple.element2;
		int[] numLettersForEachPriority = parameterTriple.element3;
		ProcessResults.categoryAnalysis(numStudentsForEachGroup, numLettersForEachGroup, totalNumLetters, numLettersForEachMatch, studentList.size(), numLettersForEachPriority);
		ProcessResults.createStudentsLackingLetter(studentList);
		ProcessResults.outputMatchScores(alumniList, studentList, matchScores);
		System.out.println("done");
	}	
}