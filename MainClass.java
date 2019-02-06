package MainPackage;
import java.util.*;
import java.io.*;


public class MainClass {
	//This is equal to the number of priorities that alumni specify
	public static final int NUM_PRIORITIES = 3;

	//Office of Admissions criteria. e.g. out of state, first gen, conversion score, scholarships, etc.
	public static final int NUM_ADMISSIONS_CRITERIA = 5;

	//This must equal the number of terms in the match function
	public static final int NUM_REASONS_TO_PRINT = NUM_PRIORITIES + NUM_ADMISSIONS_CRITERIA;

	public static void main(String args[]) throws IOException, InterruptedException {
		Scanner scan = new Scanner(System.in);
		System.out.println("email DevonFulcher3@gmail.com if you experience any problems.\n");
		
		//read in student attribute importance from user
		System.out.println("You will be prompted to input the relative importance \n"
				+ "of each of the student attributes. These are first-generation, out-of-state, \n"
				+ "Cody sholarship, Mood scholarship, and low conversion score. \n"
				+ "Greater numbers mean that attribute is more important to you and lesser numbers \n"
				+ "mean that attribute is less important to you. \n");
		System.out.print("first-generation: ");
		double firstGenerationImportance = Double.parseDouble(scan.next());
		System.out.print("out-of-state: ");
		double outOfStateImportance = Double.parseDouble(scan.next());
		System.out.print("Cody sholarship: ");
		double codyScholarshipImportance = Double.parseDouble(scan.next());
		System.out.print("Mood scholarship: ");
		double moodScholarshipImportance = Double.parseDouble(scan.next());
		System.out.print("low conversion score: ");
		double lowConversionScoreImportance = Double.parseDouble(scan.next());
		
		//read in match categories importance from user
		System.out.println("\nNow, you will be prompted to input the relative importance \n"
				+ "of categories that a student and alumni can be matched on. These are \n"
				+ "academic interests and co-curricular interests. Geographic proximity importance \n"
				+ "does not apply right now. \n");
		System.out.print("academic interests: ");
		double academicInterestImportance = Double.parseDouble(scan.next());
		System.out.print("co-curricular activities: ");
		double coCurricularActivityImportance = Double.parseDouble(scan.next());
		System.out.print("geographic proximity: N/A");
		
		//read in priority importance from user
		System.out.println("\n\nFinally, of the 3 categories that an alumni can choose, they can designate \n"
				+ "each as their first, second, and third priority. You will be prompted to input the \n"
				+ "relative importance of each of these priorities. For best results, first priority \n"
				+ "importance should be greater than second priority importance and second priority \n"
				+ "importance should be greater than third priority importance. \n");
		System.out.print("first priority importance: ");
		double firstPriorityImportance = Double.parseDouble(scan.next());
		System.out.print("second priority importance: ");
		double secondPriorityImportance = Double.parseDouble(scan.next());
		System.out.print("third priority importance: ");
		double thirdPriorityImportance = Double.parseDouble(scan.next());
		
		scan.close();
		System.out.println("\nPlease wait about 30 seconds. If the results are not satisfactory, try running \n"
				+ "the program again but with different values for the importance of different variables.");
		
		
		//read in data
		ArrayList<String> majorsList = ReadInData.readInMajors();
		ArrayList<String> scholarshipsList = ReadInData.readInScholarships();
		HashMap<String, Pair<Double, Double>> zipMap = ReadInData.readInZipCodes();
		Pair<Integer, ArrayList<Alumni>> alumniPair = ReadInData.readInAlumni(zipMap, NUM_PRIORITIES);
		int totalNumLetters = alumniPair.element1;
		ArrayList<Alumni> alumniList = alumniPair.element2;
		ArrayList<Student> studentList = ReadInData.readInStudents(zipMap, scholarshipsList, majorsList);

		//process data
		int[] numStudentsForEachGroup = ProcessData.countTypesOfStudents(studentList);
		DistanceWithNormalization[][][] distanceMatrix = ProcessData.createDistanceMatrix(zipMap, studentList, alumniList, NUM_PRIORITIES);
		Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> matchTriple = ProcessData.calculateMatch(
				distanceMatrix, studentList, alumniList,
				firstPriorityImportance, secondPriorityImportance, thirdPriorityImportance,
				NUM_REASONS_TO_PRINT, NUM_PRIORITIES, 
				firstGenerationImportance, outOfStateImportance, codyScholarshipImportance, 
				moodScholarshipImportance, lowConversionScoreImportance, academicInterestImportance, coCurricularActivityImportance);
		double[][] matchScores = matchTriple.element1;
		HashMap<String, Match> matchMap = matchTriple.element2;
		ValueAndReason[][][] matchReasons = matchTriple.element3;
		String[][] variableNames = ProcessData.createVariables(alumniList, studentList);

		//create and run linear program
		SolveLinearProgram.createLPFile(matchScores, variableNames, alumniList, studentList);
		SolveLinearProgram.runLP(args);
		
		//process results
		Triple<int[], int[], int[]> parameterTriple = ProcessResults.readResults(matchMap, studentList, alumniList, 
				matchReasons, matchScores, NUM_REASONS_TO_PRINT, NUM_PRIORITIES);
		int[] numLettersForEachGroup = parameterTriple.element1;
		int[] numLettersForEachMatch = parameterTriple.element2;
		int[] numLettersForEachPriority = parameterTriple.element3;
		ProcessResults.categoryAnalysis(numStudentsForEachGroup, numLettersForEachGroup, totalNumLetters, 
				numLettersForEachMatch, studentList.size(), numLettersForEachPriority);
		ProcessResults.createStudentsLackingLetter(studentList);
		ProcessResults.outputMatchScores(alumniList, studentList, matchScores);
		System.out.println("\nDone. Results are now in the results folder.");
	}	
}