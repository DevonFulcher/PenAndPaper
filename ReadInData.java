package MainPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ReadInData {
	public static ArrayList<String> readInMajors() throws IOException{
		ArrayList<String> majorsList=new ArrayList<String>();
		File majorsFile=new File("./confidential_data/Majors List.csv");//put file name here
		Scanner reader=new Scanner(majorsFile);
		while(reader.hasNextLine()) {
			String major = reader.nextLine();
			while(major.contains(" ")) major=major.replace(" ","");//remove whitespace
			majorsList.add(major);
		}
		reader.close();
		return majorsList;
	}

	public static ArrayList<String> readInScholarships() throws IOException{
		ArrayList<String> scholarshipsList=new ArrayList<String>();
		File scholarshipFile=new File("./confidential_data/Scholarship List.csv");//put file name here
		Scanner reader=new Scanner(scholarshipFile);
		while(reader.hasNextLine()) {
			String scholarship = reader.nextLine();
			scholarshipsList.add(scholarship);
		}
		reader.close();
		return scholarshipsList;
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

	public static Pair<Integer, ArrayList<Alumni>> readInAlumni(HashMap<String, Pair<Double, Double>> zipMap, int numPriorities) throws IOException{
		ArrayList<Alumni> alumniList = new ArrayList<Alumni>();
		int totalNumLetters = 0;
		File alumniFile=new File("./confidential_data/updated Alumni Data.csv");//put file path here
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
			tokenizer.nextToken(); //skip over email address
			String firstName = tokenizer.nextToken();			
			String lastName = tokenizer.nextToken();
			String name = firstName+lastName;
			while(name.contains(" ")) name=name.replace(" ","");
			//remove all hyphens in names
			while(name.contains("-")) name=name.replace("-", "");
			alumni.name = name;
			alumni.numLetters = Integer.parseInt(tokenizer.nextToken().trim());
			totalNumLetters += alumni.numLetters;
			tokenizer.nextToken(); //skip over graduation year

			String[] possiblePriorityTypes = {"Academic Interest", "Geographic Proximity", "Co-curricular Interest"};
			String priorityReadInError = "";
			for (int i = 0; i < numPriorities; i++) {
				if (alumni.name.equals("JayMarshall")) {
					System.out.println("here" + i + "\n"+ alumni);
				}
				String thisPriorityType = tokenizer.nextToken();
				if (!Arrays.asList(possiblePriorityTypes).contains(thisPriorityType)) {
					throw new IllegalArgumentException("\nincorrect priority type\n" + alumni + thisPriorityType);
				}
				alumni.priorityTypes.add(i, thisPriorityType);
				String statedPriority = tokenizer.nextToken();
				if (alumni.name.equals("JayMarshall")) {
					System.out.println("here\n"+ statedPriority);
				}
				String editedStatedPriority = editStatedPriority(statedPriority);
				if (editedStatedPriority.equals("error")) {
					if (statedPriority.equals("Men's") || statedPriority.equals("Women's") || statedPriority.equals("No preference or N/A")) {
						priorityReadInError = statedPriority;
					}
				}
				alumni.statedPriority.add(i, editedStatedPriority);
				if (thisPriorityType.equals("Geographic Proximity") && !zipMap.containsKey(editedStatedPriority)) {
					// if this alumnus' zip code is not in the zipMap then it is not a recognized zip code
					alumni.noZip = true;
				}
			}
			if (!priorityReadInError.equals("")) {
				//read in error quick fix
				alumni.sportsGender = priorityReadInError;
			} else {
				alumni.sportsGender = tokenizer.nextToken(); //can contain either "Men's", "Women's", or "No preference or N/A"
			}
			alumniList.add(alumni); //add alumni to list of Alumni objects
		}
		reader.close();
		return new Pair<Integer, ArrayList<Alumni>>(totalNumLetters, alumniList);
	}

	//edit stated priority to conform with data standards
	private static String editStatedPriority(String statedPriority) {
		if (statedPriority.equals("Men's") || statedPriority.equals("Women's") || statedPriority.equals("No preference or N/A")
				|| statedPriority.equals("Geographic Proximity") || statedPriority.equals("Academic Interest") || statedPriority.equals("Co-curricular Interest")) {
			statedPriority = "error";
		}
		while(statedPriority.contains(" "))statedPriority=statedPriority.replace(" ","");
		if(statedPriority.contains("(Pre-Engineering)")) statedPriority = statedPriority.replace("(Pre-Engineering)","");
		if(statedPriority.contains("(Studio)")) statedPriority = statedPriority.replace("(Studio)","");
		if(statedPriority.equals("None")) statedPriority = statedPriority.replace("None", "");

		statedPriority = statedPriority.toLowerCase();
		return statedPriority;
	}

	public static ArrayList<Student> readInStudents(HashMap<String, Pair<Double, Double>> zipMap, 
			ArrayList<String> possibleScholarships, ArrayList<String>majorsList) throws IOException{
		ArrayList<Student> studentList=new ArrayList<Student>();
		File studentFile=new File("./confidential_data/updated Student Data.csv");//file name
		Scanner reader=new Scanner(studentFile);
		reader.nextLine();//Skip the row with headers
		while(reader.hasNextLine()) {
			String currentLine=reader.nextLine();
			StringTokenizer tokenizer=new StringTokenizer(currentLine,",");//separates tokens by comma

			//read in student id
			int ref = Integer.parseInt(tokenizer.nextToken().trim());

			//read in gender
			String gender = tokenizer.nextToken();

			//read in first gen status and state
			boolean firstGen = false;
			String state;
			String nextToken = tokenizer.nextToken();
			if (nextToken.equals("1")) {
				firstGen = true;
				state = tokenizer.nextToken();
			} else if (nextToken.equals("0")) { 
				state = tokenizer.nextToken();
			} else {
				//state was read in as nextToken not firstGen status
				state = nextToken;
			}

			//read in scholarship
			boolean cody = false;
			boolean mood = false;
			nextToken = tokenizer.nextToken();
			//true if nextToken is within possibleScholarships and false otherwise
			if (possibleScholarships.contains(nextToken)) {
				if (nextToken.equals("Mood")) {
					mood = true;
				} else if (nextToken.equals("Cody")) {
					cody = true;
				}
				nextToken = tokenizer.nextToken();
			}

			//read in conversion score
			boolean noConversion = false;
			int conversion = -1;
			String[] possibleConversionScores = {"1","2","3","4","5","6"};
			//true if nextToken is within possibleConversionsScores and false otherwise
			if (Arrays.asList(possibleConversionScores).contains(nextToken)) {
				conversion = Integer.parseInt(nextToken.trim());
				nextToken = tokenizer.nextToken();
			} else {
				noConversion = true; // this is true if this student does not have a conversion score
			}

			//read in zip code
			String zip = "";
			boolean noZip = false; //noZip is true if this student doesn't have an identifiable zip code & false otherwise
			if (nextToken.length() >= 5) {
				zip = nextToken.substring(0, 5); //we just want the first 5 digits of the zip code to find distances
				if (!zipMap.containsKey(zip)) {
					noZip = true;
				}
			} else {
				noZip = true;
			}

			//read in major interests and extracurriculars
			ArrayListOverrideToString<String> majorInterests=new ArrayListOverrideToString<String>();
			ArrayListOverrideToString<String> extraInterests=new ArrayListOverrideToString<String>();
			while(tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				while(token.contains("\"")) token=token.replace("\"","");//remove excess spaces and quotation marks
				while(token.contains(" ")) token=token.replace(" ","");
				if(majorsList.contains(token)) {
					majorInterests.add(token);
				} else { //this token is a extra curricular interest
					extraInterests.add(token);
				}
			}

			//TODO: JUnit test to ensure student is created correctly
			if (noConversion) {
				assert conversion == -1;
			}
			Student student = new Student
					(ref,state,zip,majorInterests,extraInterests,firstGen,cody,mood,conversion,noZip,noConversion,gender);
			studentList.add(student);
		}
		reader.close();
		return studentList;
	}
}