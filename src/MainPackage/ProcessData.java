package MainPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProcessData {
	public static int[] countTypesOfStudents(ArrayList<Student> studentList) {
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

	public static DistanceWithNormalization[][][] createDistanceMatrix(
			HashMap<String, Pair<Double, Double>> zipMap, ArrayList<Student> studentList, ArrayList<Alumni> alumniList, int numPriorities) {
		DistanceWithNormalization[][][] distanceMatrix = new DistanceWithNormalization[studentList.size()][alumniList.size()][numPriorities];
		double studentLatitude, studentLongitude;
		double alumniLatitude, alumniLongitude;
		double maxDistance = 0;
		for(int i = 0; i < studentList.size(); i++) {
			Student thisStudent = studentList.get(i);
			if (thisStudent.noZip) { //noZip usually occurs if the student has an out of US zip code
				for(int j = 0; j < alumniList.size(); j++) {
					for(int k = 0; k < numPriorities; k++) {
						//if a student doesn't have a zip then that student doesn't have a distance to any alumni
						distanceMatrix[i][j][k] = new DistanceWithNormalization(-1,-1);
					}
				}
			} else {
				studentLatitude = zipMap.get(thisStudent.zipCode).element1;
				studentLongitude = zipMap.get(thisStudent.zipCode).element2;
				for(int j = 0; j < alumniList.size(); j++) {
					Alumni thisAlumnus = alumniList.get(j);
					if (thisAlumnus.noZip) {
						for(int k = 0; k < numPriorities; k++) {
							//if an alumnus doesn't have a zip then that alumnus doesn't have a distance to any alumni
							distanceMatrix[i][j][k] = new DistanceWithNormalization(-1,-1);
						}
					} else {
						for(int k = 0; k < numPriorities; k++) {
							if(thisAlumnus.priorityTypes.get(k).equals("Geographic Proximity") && !thisAlumnus.statedPriority.get(k).equals("error")) {
								alumniLatitude = zipMap.get(thisAlumnus.statedPriority.get(k)).element1;
								alumniLongitude = zipMap.get(thisAlumnus.statedPriority.get(k)).element2;
								distanceMatrix[i][j][k] = new DistanceWithNormalization(distance(studentLatitude, studentLongitude, alumniLatitude, alumniLongitude),-1);
								maxDistance = Double.max(maxDistance, distanceMatrix[i][j][k].distance);
							} else {
								distanceMatrix[i][j][k] = new DistanceWithNormalization(-1,-1);
							}
						} 
					}
				}
			}
		}
		for(int i = 0; i < studentList.size(); i++) {
			for(int j = 0; j < alumniList.size(); j++) {
				for(int k = 0; k < numPriorities; k++) {
					//scales every distance to be in [0,1] then subtracts from 1 to give lower distances greater value
					distanceMatrix[i][j][k].normalizedDistance = 1 - (distanceMatrix[i][j][k].distance / maxDistance);
				}
			}
		}
		return distanceMatrix;
	}

	//haversine formula from https://github.com/jasonwinn/haversine
	private static double distance(double startLat, double startLong, double endLat, double endLong) {

		double dLat  = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat   = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		final int EARTH_RADIUS = 3959; // Approx Earth radius in mi
		return EARTH_RADIUS * c; // <-- d
	}

	private static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	/*
	 * the match between each pairwise alumni and prospective student
	 */
	public static Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> calculateMatch(
			DistanceWithNormalization[][][] distanceMatrix, ArrayList<Student> studentList, ArrayList<Alumni> alumniList,
			double priorityOneConstant, double priorityTwoConstant, double priorityThreeConstant,
			int numReasonsToPrint, int numPriorities, 
			double firstGenerationImportance, double outOfStateImportance, double codyScholarshipImportance, 
			double moodScholarshipImportance, double lowConversionScoreImportance, double academicInterestImportance, double coCurricularInterestImportance) {
		//mapping from each pairwise student and alumni to the items they match on
		HashMap<String, Match> matchMap = new HashMap<String, Match>();
		double[][] matchScores = new double[studentList.size()][alumniList.size()];
		ValueAndReason[][][] matchReasonsMatrix = new ValueAndReason[studentList.size()][alumniList.size()][numReasonsToPrint];
		double[] terms = new double[numReasonsToPrint];
		for (int i = 0; i < studentList.size(); i++) {
			Student student = studentList.get(i);
			for (int j = 0; j < alumniList.size(); j++) {
				Alumni alumni = alumniList.get(j);				
				Match thisMatch = new Match(numPriorities);
				int k = 0;
				try {
					while(k < numPriorities) {
						Triple<Double, Double, Match> returnTuple = matchValue(student, alumni, thisMatch, distanceMatrix[i][j][k].normalizedDistance, k, academicInterestImportance, coCurricularInterestImportance);
						double thisMatchValue = returnTuple.element1;
						double thisTermAddition = returnTuple.element2; //this is added to this term to incentivize various priority types 
						thisMatch = returnTuple.element3;
						thisMatch.priorities[k] = thisMatchValue > 0;
						terms[k] = priorityOneConstant * thisMatchValue + thisTermAddition;
						//reasons contain first the match category, then the corresponding alumni section, 
						//then the corresponding student section
						String[] reasons = {" ", " ", " "};
						if(terms[k] > 0) {
							String priorityType = alumni.priorityTypes.get(k);
							String statedPriority = alumni.statedPriority.get(k);
							if (statedPriority.equals("error")) {
								matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], reasons);
							} else {
								if(priorityType.equals("Geographic Proximity")) {
									reasons[0] = ((int) Math.round(distanceMatrix[i][j][k].distance)) + " distance in miles";
									reasons[1] = statedPriority;
									reasons[2] = student.zipCode;
									matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], reasons);
								} else if (priorityType.equals("Academic Interest")){
									reasons[0] = "Academic Interest";
									reasons[1] = statedPriority;
									reasons[2] = student.majorInterests.toString();
									matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], reasons);
								} else {
									assert priorityType.equals("Co-Curricular Activity");
									reasons[0] = "Co-Curricular Activity";
									reasons[1] = statedPriority;
									reasons[2] = student.extraCurricularInterests.toString();
									matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], reasons);
								}
							}
						} else {
							matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], reasons);
						}
						k++;
					}
					//populate match map for later analysis
					matchMap.put(alumni.name + student.ref, thisMatch);

					//empty string array is printed if value of a term is 0
					String[] notApplicableStringArray = {" ", " ", " "};

					//add constant to increase importance of first generation
					terms[k] = (student.firstGeneration)? firstGenerationImportance: 0;
					String[] reasonsFirstGen = {"first generation", " ", " "};
					matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? reasonsFirstGen: notApplicableStringArray);
					k++;

					//add constant to increase importance of out of state
					terms[k] = (!student.state.equals("TX"))? outOfStateImportance: 0;
					String[] reasonsOutOfState = {"out of state", " ", " "};
					matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? reasonsOutOfState: notApplicableStringArray);
					k++;

					//add constant to increase importance of Cody scholarship
					terms[k] = (student.codyRecipient)? codyScholarshipImportance: 0;
					String[] reasonsCody = {"Cody scholarship", " ", " "};
					matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? reasonsCody: notApplicableStringArray);
					k++;

					//add constant to increase importance of Mood scholarship
					terms[k] = (student.moodRecipient)? moodScholarshipImportance: 0;
					String[] reasonsMood = {"Mood scholarship", " ", " "};
					matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? reasonsMood: notApplicableStringArray);
					k++;
					
					
					//calculate adjusted conversion score
					double adjustedConversion = 0;
					if (student.noConversion) {
						//set to be approximately the average of adjusted conversion scores
						adjustedConversion = 0.5;
					} else {
						//changes range of conversion score from [1,6] to [0,5] then scales score to be in [0,1]
						//then subtracts score from one to increase importance of lower scores 
						adjustedConversion = 1 - (((double) (student.conversionScore - 1)) / 5.0);
					}
					terms[k] = adjustedConversion * lowConversionScoreImportance;
					String[] reasonsConversion = {"conversion score of " + student.conversionScore, " ", " "};
					matchReasonsMatrix[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? reasonsConversion: notApplicableStringArray);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("be sure that the number of terms is equal to numReasonsToPrint. " + k + " should equal " + numReasonsToPrint);
				}
				
				//sort reasons by value
				Arrays.sort(matchReasonsMatrix[i][j], new ValueAndReason.ValueAndReasonComparator());
				//reverse order of sorted array
				int backIndex = matchReasonsMatrix[i][j].length - 1;
				for (int l = 0; l < matchReasonsMatrix[i][j].length / 2; l++) {
					ValueAndReason placeHolder = matchReasonsMatrix[i][j][l];
					matchReasonsMatrix[i][j][l] = matchReasonsMatrix[i][j][backIndex];
					matchReasonsMatrix[i][j][backIndex] = placeHolder;
					backIndex--;
				}
				
				//sum terms across this match
				for (double term: terms) {
					matchScores[i][j] += term;
				}
				student.averageMatch += matchScores[i][j];
			}
			student.averageMatch /= alumniList.size();
		}
		return new Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> (matchScores, matchMap, matchReasonsMatrix); 
	}

	private static Triple<Double, Double, Match> matchValue(Student student, Alumni alumni, Match match, 
			double thisDistance, int priorityIndex, double academicInterestImportance, double coCurricularInterestImportance) {
		String thisPriority = alumni.priorityTypes.get(priorityIndex);
		//for each list below, the index that the value is at corresponds to the number priority it belongs to.
		if (thisPriority.equals("Academic Interest")) {
			String alumniStatedPriority = alumni.statedPriority.get(priorityIndex);
			if (student.majorInterests.contains(alumniStatedPriority) && !alumniStatedPriority.equals("")) {
				match.academicInterestMatch = true;
				return new Triple<Double, Double, Match>(1.0, academicInterestImportance, match);
			} else {
				return new Triple<Double, Double, Match>(0.0, 0.0, match);
			}
		}
		if (thisPriority.equals("Co-Curricular Activity") || thisPriority.equals("Co-Curricular Interest") || thisPriority.equals("Co-curricular Interest")) {
			for(int i=0;i<student.extraCurricularInterests.size();i++) {
				//checks if student extracurricular contains alumni input for co curricular
				//"Swimming - Men" will match with alumni input of "Swimming"
				//"Musicc" will match with alumni input of "Music"
				String studentExtraCurricularInterest = student.extraCurricularInterests.get(i);
				String alumniStatedPriority = alumni.statedPriority.get(priorityIndex);
				if (studentExtraCurricularInterest.contains(alumniStatedPriority) && !alumniStatedPriority.equals("")) {
					//TODO: make separate read in doc
					String[] allSports = {"baseball", "basketball", "cross country", "football", "golf", "lacrosse", "soccer", "softball", "swimming", "tennis", "track & field", "volleyball"};
					//true if alumniStatedPriority is in allSports and false otherwise
					if (Arrays.asList(allSports).contains(alumniStatedPriority)) {
						//the following if/else sequence ensures that alumni are only paired with students if they play the same gender of sport
						if (alumni.sportsGender.equals("Men's") && student.gender.equals("M")) {
							match.coCurricularMatch = true;
							return new Triple<Double, Double, Match>(1.0, coCurricularInterestImportance, match);
						} else if (alumni.sportsGender.equals("Women's") && student.gender.equals("F")) {
							match.coCurricularMatch = true;
							return new Triple<Double, Double, Match>(1.0, coCurricularInterestImportance, match);
						} else if (alumni.sportsGender.equals("No preference or N/A")) {
							match.coCurricularMatch = true;
							return new Triple<Double, Double, Match>(1.0, coCurricularInterestImportance, match);
						} else {
							match.coCurricularMatch = false;
							return new Triple<Double, Double, Match>(0.0, coCurricularInterestImportance, match);
						}
					} else {
						match.coCurricularMatch = true;
						return new Triple<Double, Double, Match>(1.0, coCurricularInterestImportance, match);
					}
				}
			}
			return new Triple<Double, Double, Match>(0.0, 0.0, match);
		}
		if (thisPriority.equals("Geographic Proximity")) {
			if (student.noZip || alumni.noZip) { //student does not have a recognized US zip code
				return new Triple<Double, Double, Match>(0.0, 0.0, match);
			} else {
				return new Triple<Double, Double, Match>(thisDistance, 0.0, match);
			}
		}
		//if the code doesnt exit on an if statement, prints out which priority was the issue
		throw new IllegalArgumentException("no match on priority" + "\npriority: " + thisPriority + 
				"\npriority index: " + priorityIndex + "\nthis alumnus: " + alumni);
	}

	public static String[][] createVariables(ArrayList<Alumni> alumniList, ArrayList<Student> studentList) {
		String[][] variableNames = new String[studentList.size()][alumniList.size()];
		for (int i = 0; i < studentList.size(); i++) {
			for (int j = 0; j < alumniList.size(); j++) {
				Student thisStudent = studentList.get(i);
				Alumni thisAlumni = alumniList.get(j);
				//create variables matrix
				variableNames[i][j] = thisAlumni.name + thisStudent.ref;
			}
		}
		return variableNames;
	}
}