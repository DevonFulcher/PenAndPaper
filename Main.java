import java.util.*;
import java.io.*;


public class Main {
	//start options 
	//Warning! variables do not necessarily linearly scale the output. experimentation is required to create ideal matching
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
	//end options

	public static ArrayList<Student> studentList=new ArrayList<Student>();
	public static ArrayList<Alumni> alumniList=new ArrayList<Alumni>();
	public static ArrayList<String> majorsList=new ArrayList<String>();
	public static double[][] matchScores;
	//holds the reason for each alumni student match. Student x Alumni x Reason
	public static ValueAndReason[][][] matchReasons;	
	public static String[][] variableNames;

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

		readInMajors();
		HashMap<String, Pair<Double, Double>> zipMap = readInZipCodes();
		int totalNumLetters = readInAlumni(zipMap);
		int numStudents = readInStudents(zipMap);
		int[] numStudentsForEachGroup = countTypesOfStudents();
		double[][][] distanceMatrix = createDistanceMatrix(zipMap);
		HashMap<String, Match> matchMap = calculateMatch(distanceMatrix);
		createVariables();
		createLPFile();
		System.out.println("run this command: \n"
				+ "glpsol --lp PenAndPaper.lp --ranges PenAndPaperSensitivity.txt -o PenAndPaperResults.txt \n"
				+ "in the command line within the Pen And Paper src folder then press c and enter to continue.");

		//wait for response
		Scanner scan = new Scanner(System.in);
		scan.next();
		scan.close();

		Triple<int[], int[], int[]> parameterTriple = readResults(matchMap);
		int[] numLettersForEachGroup = parameterTriple.element1;
		int[] numLettersForEachMatch = parameterTriple.element2;
		int[] numLettersForEachPriority = parameterTriple.element3;
		categoryAnalysis(numStudentsForEachGroup, numLettersForEachGroup, totalNumLetters, numLettersForEachMatch, numStudents, numLettersForEachPriority);
		createStudentsLackingLetter();
		outputMatchScores();
		System.out.println("done");
	}	

	public static void readInMajors() throws IOException{
		File majorsFile=new File("./confidential_data/majorsList.txt");//put file name here
		Scanner reader=new Scanner(majorsFile);
		while(reader.hasNextLine()) {
			String major = reader.nextLine();
			while(major.contains(" ")) major=major.replace(" ","");//remove whitespace
			majorsList.add(major);
		}
		reader.close();
	}

	public static HashMap<String, Pair<Double, Double>> readInZipCodes() throws IOException{
		HashMap<String, Pair<Double, Double>> zipMap = new HashMap<String, Pair<Double, Double>>();
		//zip code data has been obtained from this source: https://gist.github.com/erichurst/7882666
		File zipFile=new File("./src/US Zip Codes from 2013 Government Data.txt");//put file name here
		Scanner reader=new Scanner(zipFile);
		reader.nextLine(); //skip row with header
		StringTokenizer tokenizer;
		String zip; 
		double latitude, longitude;
		while (reader.hasNextLine()) {
			tokenizer = new StringTokenizer(reader.nextLine(),",");//separates tokens by comma
			zip = tokenizer.nextToken();
			latitude = Double.parseDouble(tokenizer.nextToken());
			longitude = Double.parseDouble(tokenizer.nextToken());
			zipMap.put(zip, new Pair<Double, Double>(latitude, longitude));
		}
		reader.close();
		return zipMap;
	}

	public static int readInAlumni(HashMap<String, Pair<Double, Double>> zipMap) throws IOException{
		int totalNumLetters = 0;
		File alumniFile=new File("./confidential_data/Alumni Data.txt");//put file path here
		Scanner reader=new Scanner(alumniFile);
		reader.nextLine(); //skip row with header
		StringTokenizer tokenizer;
		while (reader.hasNextLine()) {
			tokenizer = new StringTokenizer(reader.nextLine(),","); //separates tokens by comma
			Alumni alumni=new Alumni();
			// TODO: fix this try/catch later
			try {
				tokenizer.nextToken(); //get rid of timestamp
			} catch (NoSuchElementException e) {
				break;
			}
			String firstName = tokenizer.nextToken();			
			String lastName = tokenizer.nextToken();
			String name = firstName+lastName;
			while(name.contains(" ")) name=name.replace(" ","");
			alumni.name = name;
			alumni.numLetters = Integer.parseInt(tokenizer.nextToken().trim());
			totalNumLetters += alumni.numLetters;

			for (int i = 0; i < NUM_PRIORITIES; i++) {
				String thisPriorityType = tokenizer.nextToken();
				alumni.priorityTypes.add(i, thisPriorityType);
				String thisStatedPriority = tokenizer.nextToken();
				thisStatedPriority = editStatedPriority(thisStatedPriority);
				alumni.statedPriority.add(i, thisStatedPriority);
				if (thisPriorityType.equals("Geographic Proximity") && !zipMap.containsKey(thisStatedPriority)) {
					// if this alumnus' zip code is not in the zipMap then it is not a recognized zip code
					alumni.noZip = true;
				}
			}

			alumniList.add(alumni);//add alumni to list of Alumni objects
		}
		reader.close();
		return totalNumLetters;
	}

	//edit stated priority to conform with data standards
	public static String editStatedPriority(String statedPriority) {
		while(statedPriority.contains(" "))statedPriority=statedPriority.replace(" ","");
		if(statedPriority.contains("(Pre-Engineering)")) statedPriority = statedPriority.replace("(Pre-Engineering)","");
		if(statedPriority.contains("(Studio)")) statedPriority = statedPriority.replace("(Studio)","");
		return statedPriority;
	}

	public static int readInStudents(HashMap<String, Pair<Double, Double>> zipMap) throws IOException{
		int numStudents = 0;
		File studentFile=new File("./confidential_data/Student Data.txt");//file name
		Scanner reader=new Scanner(studentFile);
		reader.nextLine();//Skip the row with headers
		while(reader.hasNextLine()) {
			String currentLine=reader.nextLine();
			StringTokenizer tokenizer=new StringTokenizer(currentLine,",");//separates tokens by comma

			int ref = Integer.parseInt(tokenizer.nextToken().trim());

			boolean firstGen = false;
			String state;
			String nextToken = tokenizer.nextToken();
			if (nextToken.equals("1")) {
				firstGen = true;
				state = tokenizer.nextToken();
			} else {
				//state was read in as nextToken not firstGen status
				state = nextToken;
			}

			boolean cody = false;
			boolean mood = false;
			int conversion = -1;
			nextToken = tokenizer.nextToken();
			//true if nextToken is within POSSIBLE_SCHOLARSHIPS and false otherwise
			if (Arrays.binarySearch(POSSIBLE_SCHOLARSHIPS, nextToken) >= 0) {
				if (nextToken.equals("Mood")) {
					mood = true;
				} else if (nextToken.equals("Cody")) {
					cody = true;
				}
				nextToken = tokenizer.nextToken();
			}
			boolean noConversion = false;
			String[] possibleConversionScores = {"1","2","3","4","5","6"};
			//true if nextToken is within possibleConversionsScores and false otherwise
			if (Arrays.binarySearch(possibleConversionScores, nextToken) >= 0) {
				conversion = Integer.parseInt(nextToken.trim());
				nextToken = tokenizer.nextToken();
			} else {
				noConversion = true; // this is true if this student does not have a conversion score
			}
			if (!noConversion) {
				assert conversion != -1;
			}

			String zip = "";
			boolean noZip = false; //noZip is true if this student doesn't have an identifiable zip code & false otherwise
			if (nextToken.length() >= 5) {
				zip = nextToken.substring(0, 5); //we just want the first 5 digits of the zipcode to find distances
				if (!zipMap.containsKey(zip)) {
					noZip = true;
				}
			} else {
				noZip = true;
			}

			ArrayList<String> majorInterests=new ArrayList<String>();//read in Major Interests and extracurriculars
			ArrayList<String> extraInterests=new ArrayList<String>();
			while(tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				while(token.contains("\"")) token=token.replace("\"","");//remove excess spaces and quotation marks
				while(token.contains(" ")) token=token.replace(" ","");
				if(majorsList.contains(token)) {
					majorInterests.add(token);
				}
				else {
					extraInterests.add(token);
				}
			}

			Student student = new Student
					(ref,state,zip,majorInterests,extraInterests,firstGen,cody,mood,conversion,noZip,noConversion);
			numStudents++;
			studentList.add(student);
		}
		reader.close();
		return numStudents;
	}

	public static int[] countTypesOfStudents() {
		int[] numStudentsForEachGroup = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		for (Student student: studentList) {
			if (student.firstGeneration) {
				numStudentsForEachGroup[0]++;
			}
			if (student.codyRecipient) {
				numStudentsForEachGroup[1]++;
			}
			if (student.moodRecipient) {
				numStudentsForEachGroup[2]++;
			}
			if (!student.state.equals("TX")) {
				numStudentsForEachGroup[3]++;
			}
			switch(student.conversionScore) {
			case 1:
				numStudentsForEachGroup[4]++;
				break;
			case 2:
				numStudentsForEachGroup[5]++;
				break;
			case 3:
				numStudentsForEachGroup[6]++;
				break;
			case 4:
				numStudentsForEachGroup[7]++;
				break;
			case 5:
				numStudentsForEachGroup[8]++;
				break;
			case 6:
				numStudentsForEachGroup[9]++;
			}
		}
		return numStudentsForEachGroup;
	}

	public static double[][][] createDistanceMatrix(HashMap<String, Pair<Double, Double>> zipMap) {
		double[][][] distanceMatrix = new double[studentList.size()][alumniList.size()][NUM_PRIORITIES];
		double studentLatitude, studentLongitude;
		double alumniLatitude, alumniLongitude;
		double maxDistance = 0;
		for(int i = 0; i < studentList.size(); i++) {
			Student thisStudent = studentList.get(i);
			if (thisStudent.noZip) { //noZip usually occurs if the student has an out of US zip code
				for(int j = 0; j < alumniList.size(); j++) {
					for(int k = 0; k < NUM_PRIORITIES; k++) {
						//if a student doesn't have a zip then that student doesn't have a distance to any alumni
						distanceMatrix[i][j][k] = -1;
					}
				}
			} else {
				studentLatitude = zipMap.get(thisStudent.zipCode).element1;
				studentLongitude = zipMap.get(thisStudent.zipCode).element2;
				for(int j = 0; j < alumniList.size(); j++) {
					Alumni thisAlumnus = alumniList.get(j);
					if (thisAlumnus.noZip) {
						for(int k = 0; k < NUM_PRIORITIES; k++) {
							//if an alumnus doesn't have a zip then that alumnus doesn't have a distance to any alumni
							distanceMatrix[i][j][k] = -1;
						}
					} else {
						for(int k = 0; k < NUM_PRIORITIES; k++) {
							if(thisAlumnus.priorityTypes.get(k).equals("Geographic Proximity")) {
								alumniLatitude = zipMap.get(thisAlumnus.statedPriority.get(k)).element1;
								alumniLongitude = zipMap.get(thisAlumnus.statedPriority.get(k)).element2;
								distanceMatrix[i][j][k] = distance(studentLatitude, studentLongitude, alumniLatitude, alumniLongitude);
								maxDistance = Double.max(maxDistance, distanceMatrix[i][j][k]);
							} else {
								distanceMatrix[i][j][k] = -1;
							}
						} 
					}
				}
			}
		}
		for(int i = 0; i < studentList.size(); i++) {
			for(int j = 0; j < alumniList.size(); j++) {
				for(int k = 0; k < NUM_PRIORITIES; k++) {
					//scales every distance to be in [0,1] then subtracts from 1 to give lower distances greater value
					distanceMatrix[i][j][k] = 1 - (distanceMatrix[i][j][k] / maxDistance);
				}
			}
		}
		return distanceMatrix;
	}

	//haversine formula from https://github.com/jasonwinn/haversine
	public static double distance(double startLat, double startLong,
			double endLat, double endLong) {

		double dLat  = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat   = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
		return EARTH_RADIUS * c; // <-- d
	}

	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	/*
	 * the match between each pairwise alumni and prospective student
	 */
	public static HashMap<String, Match> calculateMatch(double[][][] distanceMatrix) {
		//mapping from each pairwise student and alumni to the items they match on
		HashMap<String, Match> matchMap = new HashMap<String, Match>();
		matchScores = new double[studentList.size()][alumniList.size()];
		variableNames = new String[studentList.size()][alumniList.size()];
		matchReasons = new ValueAndReason[studentList.size()][alumniList.size()][NUM_REASONS_TO_PRINT];
		double priorityOneConstant = PRIORITY_ONE_IMPORTANCE;
		double priorityTwoConstant = PRIORITY_TWO_IMPORTANCE;
		double priorityThreeConstant = PRIORITY_THREE_IMPORTANCE;
		if (PARTIAL_MATCH_FEATURE) {
			priorityOneConstant++;
			priorityTwoConstant++;
			priorityThreeConstant++;
		}
		double[] terms = new double[NUM_REASONS_TO_PRINT];
		for (int i = 0; i < studentList.size(); i++) {
			Student thisStudent = studentList.get(i);
			for (int j = 0; j < alumniList.size(); j++) {
				Alumni thisAlumni = alumniList.get(j);				
				Match thisMatch = new Match(NUM_PRIORITIES);
				int k = 0;
				try {
					while(k < NUM_PRIORITIES) {
						double thisDistance = distanceMatrix[i][j][k];
						Triple<Double, Double, Match> returnTuple = matchValue(thisStudent, thisAlumni, thisMatch, thisDistance, k);
						double thisMatchValue = returnTuple.element1;
						double thisTermAddition = returnTuple.element2; //this is added to this term to incentivize various priority types 
						thisMatch = returnTuple.element3;
						thisMatch.priorities[k] = thisMatchValue > 0; //TODO: consider distance of 0
						terms[k] = priorityOneConstant * thisMatchValue + thisTermAddition;
						matchReasons[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? thisAlumni.priorityTypes.get(k): "null");
						k++;
					}
					//populate match map for later analysis
					matchMap.put(thisAlumni.name + thisStudent.ref, thisMatch);

					//add constant to increase importance of first generation
					terms[k] = (thisStudent.firstGeneration)? FIRST_GENERATION_IMPORTANCE: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "first generation": "null");

					//add constant to increase importance of out of state
					terms[k] = (!thisStudent.state.equals("TX"))? OUT_OF_STATE_IMPORTANCE: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "out of state": "null");

					//add constant to increase importance of Cody scholarship
					terms[k] = (thisStudent.codyRecipient)? CODY_SCHOLARSHIP_IMPORTANCE: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "cody scholarship": "null");

					//add constant to increase importance of Mood scholarship
					terms[k] = (thisStudent.moodRecipient)? MOOD_SCHOLARSHIP_IMPORTANCE: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "mood scholarship": "null");

					double adjustedConversion = 0;
					if (thisStudent.noConversion) {
						//set to be approximately the average of conversion scores
						adjustedConversion = 0.5;
					} else {
						//changes range of conversion score from [1,6] to [0,5] then scales score to be in [0,1]
						//then subtracts score from one to increase importance of lower scores 
						adjustedConversion = (1 - (((double) (thisStudent.conversionScore - 1)) / 5.0));
					}
					terms[k] = adjustedConversion * LOW_CONVERSION_SCORE_IMPORTANCE;
					
					matchReasons[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? "adjusted conversion score of " + adjustedConversion: "null");
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("be sure that the number of terms is equal to numReasonsToPrint. " + k + " should equal " + NUM_REASONS_TO_PRINT);
				}
				Arrays.sort(matchReasons[i][j], new ValueAndReason.ValueAndReasonComparator());
				//reverse order of sorted array
				int backIndex = matchReasons[i][j].length - 1;
				for (int l = 0; l < matchReasons[i][j].length / 2; l++) {
					ValueAndReason placeHolder = matchReasons[i][j][l];
					matchReasons[i][j][l] = matchReasons[i][j][backIndex - l];
					matchReasons[i][j][backIndex - l] = placeHolder;
				}
				for (double term: terms) {
					matchScores[i][j] += term;
				}
				thisStudent.averageMatch += matchScores[i][j];
			}
			thisStudent.averageMatch /= alumniList.size();
		}
		return matchMap; 
	}

	public static Triple<Double, Double, Match> matchValue(Student student, Alumni alumni, Match match, 
			double thisDistance, int priorityIndex) {
		String thisPriority = alumni.priorityTypes.get(priorityIndex);
		//for each list below, the index that the value is at corresponds to the number priority it belongs to.
		if (thisPriority.equals("Academic Interest")) {
			if (student.majorInterests.contains(alumni.statedPriority.get(priorityIndex))) {
				match.academicInterestMatch = true;
				return new Triple<Double, Double, Match>(1.0, ACADEMIC_INTEREST_IMPORTANCE, match);
			} else {
				return new Triple<Double, Double, Match>(0.0, 0.0, match);
			}
		}
		if (thisPriority.equals("Co-Curricular Activity") || thisPriority.equals("Co-Curricular Interest")) {
			for(int i=0;i<student.extraCurricularInterests.size();i++) {
				//checks if student extracurricular contains alumni input for co curricular
				//"Swimming - Men" will match with alumni input of "Swimming"
				//"Musicc" will match with alumni input of "Music"
				if (student.extraCurricularInterests.get(i).contains(alumni.statedPriority.get(priorityIndex))) {
					match.coCurricularMatch = true;
					return new Triple<Double, Double, Match>(1.0, COCURRICULAR_INTEREST_IMPORTANCE, match);
				}
			}
			return new Triple<Double, Double, Match>(0.0, 0.0, match);
		}
		if (thisPriority.equals("Geographic Proximity")) {
			if (student.noZip || alumni.noZip) { //student does not have a recognized US zip code
				return new Triple<Double, Double, Match>(0.0, 0.0, match);
			} else {
				//TODO: possibly change 2nd parameter of this triple
				return new Triple<Double, Double, Match>(thisDistance, 0.0, match);
			}
		}
		//if the code doesnt exit on an if statement, prints out which priority was the issue
		throw new IllegalArgumentException("no match on priority" + "\npriority: " + thisPriority + 
				"\npriority index: " + priorityIndex + "\nthis alumnus: " + alumni);
	}

	public static void createVariables() {
		for (int i = 0; i < studentList.size(); i++) {
			for (int j = 0; j < alumniList.size(); j++) {
				Student thisStudent = studentList.get(i);
				Alumni thisAlumni = alumniList.get(j);
				//create variables matrix
				variableNames[i][j] = thisAlumni.name + thisStudent.ref ;
			}
		}
	}

	public static void createLPFile() throws FileNotFoundException {
		//the total number of letters that alumni are willing to write
		int totalAlumniLetters = 0; 
		File glpFile = new File("./src/PenAndPaper.lp");
		PrintWriter output = new PrintWriter(glpFile);
		output.println("Maximize");
		output.print("Total_Match:");

		for (int row = 0; row < matchScores.length; row++) {
			for (int col = 0; col < matchScores[row].length; col++) {
				output.print(" " + matchScores[row][col] + " " + variableNames[row][col]);
				if(col != matchScores[row].length-1 || row != matchScores.length-1)
					output.println(" +");				
			}
		}
		output.println();
		output.println("\nsubject to");

		for (Alumni alumni: alumniList) {
			if (PARTIAL_MATCH_FEATURE) {
				totalAlumniLetters += alumni.numLetters - 1;
			} else {
				totalAlumniLetters += alumni.numLetters;
			}
		}
		//if true then dummy supply will be appended to the LP
		//otherwise a dummy demand will be appended to the LP
		boolean dummySupply = totalAlumniLetters > studentList.size();
		//the amount of dummy supply/demand to be transported
		int dummyValue = Math.abs(totalAlumniLetters - studentList.size());
		//counter for the number of dummy supply/demand terms added to the LP
		int dummyCount = 0;

		//student letter constraints
		for (int student = 0; student < studentList.size(); student++) {
			output.print("student" + studentList.get(student).ref + ":");

			//create each term of this student constraint
			for (int term = 0; term < alumniList.size(); term++) {
				output.print(" " + variableNames[student][term] + " ");
				if (term + 1 < alumniList.size()) {
					output.print("+");
				}
			}
			if (!dummySupply) {
				output.print("+ " + "dummyAlumniStudent" + ((dummyCount++) + 1) + " ");
			}
			output.print("<= ");
			output.println(1);
		}
		if (dummySupply) {
			output.print("dummyStudent:");
			while (dummyCount < alumniList.size()) {
				output.print(" dummyStudentAlumni" + (dummyCount + 1) + " ");
				if (dummyCount + 1 < alumniList.size()) {
					output.print("+");
				}
				dummyCount++;
			}
			output.print("<= ");
			output.println(dummyValue);
		}
		//reusing the dummy variables
		dummyCount = 0;

		//alumni letter constraints
		for (int alumni = 0; alumni < alumniList.size(); alumni++) {
			output.print(alumniList.get(alumni).name + ":");

			//create each term of this alumni constraint
			for (int term = 0; term < studentList.size(); term++) {
				output.print(" " + variableNames[term][alumni] + " ");
				if (term + 1 < studentList.size()) {
					output.print("+");
				}
			}
			if (dummySupply) {
				output.print("+ " + "dummyStudentAlumni" + ((dummyCount++) + 1) + " ");
			}

			output.print("<= ");

			if (PARTIAL_MATCH_FEATURE) {
				output.println(alumniList.get(alumni).numLetters - 1);
			} else {
				output.println(alumniList.get(alumni).numLetters);
			}
		}
		if (!dummySupply) {
			output.print("dummyAlumni:");
			while (dummyCount < studentList.size()) {
				output.print(" dummyAlumniStudent" + (dummyCount + 1) + " ");
				if (dummyCount + 1 < studentList.size()) {
					output.print("+");
				}
				dummyCount++;
			}
			output.print("<= ");
			output.println(dummyValue);
		}

		output.println();

		output.println("binary");
		for (int row = 0; row < matchScores.length; row++) {
			for (int col = 0; col < matchScores[row].length; col++) {
				output.println(variableNames[row][col]);
			}
		}
		for (int i = 1; i < dummyCount + 1; i++) {
			if (dummySupply) {
				output.println("dummyStudentAlumni" + i);
			} else {
				output.println("dummyAlumniStudent" + i);
			}
		}

		output.println("\nEnd");
		output.close();
	}

	public static Triple<int[], int[], int[]> readResults(HashMap<String, Match> matchMap) throws FileNotFoundException{
		PrintWriter output = null;
		try {
			File finalOutput = new File("./results/Final Output.csv");
			output = new PrintWriter(finalOutput);
			output.print("Student,Alumni,Match Score");
			for (int i = 0; i < NUM_REASONS_TO_PRINT; i++)
				output.print(",Reason" + (i + 1));
			output.println();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with finalOutput.csv already open in excel");
			throw e;
		}
		int[] numLettersForEachGroup = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] numLettersForEachMatch = {0, 0, 0};
		int[] numLettersForEachPriority = new int[NUM_PRIORITIES];
		File results=new File("./src/PenAndPaperResults.txt");
		Scanner scan=new Scanner(results);
		String thisString;
		for (int i = 0; i < studentList.size(); i++) {
			for (int j = 0; j < alumniList.size(); j++) {
				while (scan.hasNext()) {
					thisString = scan.next();
					Student thisStudent = studentList.get(i);
					Alumni thisAlumni = alumniList.get(j);
					if (thisString.equals(thisAlumni.name + thisStudent.ref)) {
						scan.next(); //skip over asterisk in file
						if (scan.nextInt() == 1) {
							thisStudent.receivesLetter = true;

							//increment the count of the letters that are sent to first generation students
							if (thisStudent.firstGeneration) {
								numLettersForEachGroup[0]++;
							}
							//increment the count of the letters that are sent to cody recipients
							if (thisStudent.codyRecipient) {
								numLettersForEachGroup[1]++;
							}
							//increment the count of the letters that are sent to cody recipients
							if (thisStudent.moodRecipient) {
								numLettersForEachGroup[2]++;
							}
							//increment the count of the letters that are sent to out of state students
							if (!thisStudent.state.equals("TX")) {
								numLettersForEachGroup[3]++;
							}
							switch(thisStudent.conversionScore) {
							case 1:
								numLettersForEachGroup[4]++;
								break;
							case 2:
								numLettersForEachGroup[5]++;
								break;
							case 3:
								numLettersForEachGroup[6]++;
								break;
							case 4:
								numLettersForEachGroup[7]++;
								break;
							case 5:
								numLettersForEachGroup[8]++;
								break;
							case 6:
								numLettersForEachGroup[9]++;
							}

							Match thisMatch = matchMap.get(thisAlumni.name + thisStudent.ref);
							if (thisMatch.academicInterestMatch) {
								numLettersForEachMatch[0]++;
							}
							if (thisMatch.coCurricularMatch) {
								numLettersForEachMatch[1]++;
							}

							/*TODO: fix this
								if (thisMatch.geographicMatch) {
									numLettersForEachMatch[2]++;
								}*/

							for (int k = 0; k < NUM_PRIORITIES; k++) {
								if (thisMatch.priorities[k]) {
									numLettersForEachPriority[k]++;
								}
							}

							output.print(thisStudent.ref + "," + thisAlumni.name + "," + matchScores[i][j]);

							//fill in reasons matrix
							for (int k = 0; k < NUM_REASONS_TO_PRINT; k++) {
								output.print("," + matchReasons[i][j][k].reason);
							}
							output.println();
						}
						break;
					}
				}
			}
		}
		scan.close();
		output.close();
		return new Triple<int[], int[], int[]> (numLettersForEachGroup, numLettersForEachMatch, numLettersForEachPriority);
	}

	public static void categoryAnalysis(int[] numStudentsForEachGroup, int[] numLettersForEachGroup, int totalNumLetters, 
			int[] numLettersForEachMatch, int numStudents, int[] numLettersForEachPriority) throws FileNotFoundException {
		assert numStudentsForEachGroup.length == numLettersForEachGroup.length;
		PrintWriter output = null;
		try {
			File studentCategoryAnalysis = new File("./results/Category Analysis.csv");
			output = new PrintWriter(studentCategoryAnalysis);

			//student category statistics
			String[] groupNames = {"first generation", "cody recipient", "mood recipient", "out of state", "conversion 1", "conversion 2", 
					"conversion 3", "conversion 4", "conversion 5", "conversion 6"};
			output.println(",percent of students who belong to each group,"
					+ "percent of students from each group who received a letter,"
					+ "percent of letters sent to each group");
			for (int i = 0; i < numStudentsForEachGroup.length; i++) {
				output.print(groupNames[i] + ",");
				output.print(numStudentsForEachGroup[i] * 1.0 / numStudents * 100 + ",");
				if (numStudentsForEachGroup[i] == 0) {
					output.print("0,");
				} else {
					output.print(numLettersForEachGroup[i] * 1.0 / numStudentsForEachGroup[i] * 100 + ",");
				}
				output.println(numLettersForEachGroup[i] * 1.0 / totalNumLetters * 100);
			}
			output.println();

			//match statistics
			String[] matchNames = {"Academic Interest", "Co-curricular Interest", "Geographic Proximity"};
			output.println(",percent of letters sent that contained match from each category");
			for (int i = 0; i < numLettersForEachMatch.length; i++) {
				output.print(matchNames[i] + ",");
				output.println(numLettersForEachMatch[i] * 1.0 / totalNumLetters * 100);
			}
			output.println();

			//priority statistics
			output.println(",percent of letters written from each priority");
			for (int i = 0; i < numLettersForEachPriority.length; i++) {
				output.println("priority " + (i + 1) + "," + numLettersForEachPriority[i] * 1.0 / totalNumLetters * 100);
			}

			output.close();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with categoryAnalysis.csv already open in excel");
			throw e;
		}
	}

	public static void createStudentsLackingLetter() throws FileNotFoundException {
		ArrayList<Student> studentsLackingLetter = new ArrayList<>();
		for (Student student: studentList) {
			if (!student.receivesLetter) {
				studentsLackingLetter.add(student);
			}
		}
		studentsLackingLetter.sort(new Student.StudentAverageMatchComparator());
		Collections.reverse(studentsLackingLetter);

		PrintWriter output = null;
		try {
			File studentsLackingLetters = new File("./results/Students Lacking Letters.csv");
			output = new PrintWriter(studentsLackingLetters);
			output.println("Student,Average Match");
			for (Student student: studentsLackingLetter)
				output.println(student.ref + "," + student.averageMatch);
			output.println();
			output.close();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with studentsLackingLetters.csv already open in excel");
			throw e;
		}
	}

	public static void outputMatchScores() throws FileNotFoundException {
		PrintWriter output = null;
		try {
			File studentsLackingLetters = new File("./results/Match Scores.csv");
			output = new PrintWriter(studentsLackingLetters);
			output.print(",");
			for (Alumni alumni: alumniList) {
				output.print(alumni.name + ",");
			}
			output.println();
			for (int i = 0; i < studentList.size(); i++) {
				output.print(studentList.get(i).ref + ",");
				for (int j = 0; j < alumniList.size(); j++) {
					output.print(matchScores[i][j] + ",");
				}
				output.println();
			}
			output.println();
			output.close();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with matchScores.csv already open in excel");
			throw e;
		}
	}
}