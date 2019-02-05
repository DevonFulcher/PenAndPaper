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

	public static double[][][] createDistanceMatrix(
			HashMap<String, Pair<Double, Double>> zipMap, ArrayList<Student> studentList, ArrayList<Alumni> alumniList, int numPriorities) {
		double[][][] distanceMatrix = new double[studentList.size()][alumniList.size()][numPriorities];
		double studentLatitude, studentLongitude;
		double alumniLatitude, alumniLongitude;
		double maxDistance = 0;
		for(int i = 0; i < studentList.size(); i++) {
			Student thisStudent = studentList.get(i);
			if (thisStudent.noZip) { //noZip usually occurs if the student has an out of US zip code
				for(int j = 0; j < alumniList.size(); j++) {
					for(int k = 0; k < numPriorities; k++) {
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
						for(int k = 0; k < numPriorities; k++) {
							//if an alumnus doesn't have a zip then that alumnus doesn't have a distance to any alumni
							distanceMatrix[i][j][k] = -1;
						}
					} else {
						for(int k = 0; k < numPriorities; k++) {
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
				for(int k = 0; k < numPriorities; k++) {
					//scales every distance to be in [0,1] then subtracts from 1 to give lower distances greater value
					distanceMatrix[i][j][k] = 1 - (distanceMatrix[i][j][k] / maxDistance);
				}
			}
		}
		return distanceMatrix;
	}

	//haversine formula from https://github.com/jasonwinn/haversine
	private static double distance(double startLat, double startLong,
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

	private static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	/*
	 * the match between each pairwise alumni and prospective student
	 */
	public static Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> calculateMatch(
			double[][][] distanceMatrix, ArrayList<Student> studentList, ArrayList<Alumni> alumniList,
			double priorityOneConstant, double priorityTwoConstant, double priorityThreeConstant,
			int numReasonsToPrint, int numPriorities, 
			double firstGenerationImportance, double outOfStateImportance, double codyScholarshipImportance, 
			double moodScholarshipImportance, double lowConversionScoreImportance, double academicInterestImportance, double coCurricularInterestImportance) {
		//mapping from each pairwise student and alumni to the items they match on
		HashMap<String, Match> matchMap = new HashMap<String, Match>();
		double[][] matchScores = new double[studentList.size()][alumniList.size()];
		ValueAndReason[][][] matchReasons = new ValueAndReason[studentList.size()][alumniList.size()][numReasonsToPrint];
		double[] terms = new double[numReasonsToPrint];
		for (int i = 0; i < studentList.size(); i++) {
			Student thisStudent = studentList.get(i);
			for (int j = 0; j < alumniList.size(); j++) {
				Alumni thisAlumni = alumniList.get(j);				
				Match thisMatch = new Match(numPriorities);
				int k = 0;
				try {
					while(k < numPriorities) {
						double thisDistance = distanceMatrix[i][j][k];
						Triple<Double, Double, Match> returnTuple = matchValue(thisStudent, thisAlumni, thisMatch, thisDistance, k, academicInterestImportance, coCurricularInterestImportance);
						double thisMatchValue = returnTuple.element1;
						double thisTermAddition = returnTuple.element2; //this is added to this term to incentivize various priority types 
						thisMatch = returnTuple.element3;
						thisMatch.priorities[k] = thisMatchValue > 0; //TODO: consider distance of 0
						terms[k] = priorityOneConstant * thisMatchValue + thisTermAddition;
						//TODO: add distance as reason
						if(terms[k] > 0) {
							if(thisAlumni.priorityTypes.get(k).equals("Geographic Proximity")) {
								matchReasons[i][j][k] = new ValueAndReason(terms[k], "distance between them is " + thisDistance);
							} else {
								matchReasons[i][j][k] = new ValueAndReason(terms[k], thisAlumni.priorityTypes.get(k));
							}
						} else {
							matchReasons[i][j][k] = new ValueAndReason(terms[k], "null");
						}
						k++;
					}
					//populate match map for later analysis
					matchMap.put(thisAlumni.name + thisStudent.ref, thisMatch);

					//add constant to increase importance of first generation
					terms[k] = (thisStudent.firstGeneration)? firstGenerationImportance: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "first generation": "null");

					//add constant to increase importance of out of state
					terms[k] = (!thisStudent.state.equals("TX"))? outOfStateImportance: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "out of state": "null");

					//add constant to increase importance of Cody scholarship
					terms[k] = (thisStudent.codyRecipient)? codyScholarshipImportance: 0;
					matchReasons[i][j][k++] = new ValueAndReason(terms[k], (terms[k] > 0)? "cody scholarship": "null");

					//add constant to increase importance of Mood scholarship
					terms[k] = (thisStudent.moodRecipient)? moodScholarshipImportance: 0;
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
					terms[k] = adjustedConversion * lowConversionScoreImportance;
					System.out.println(thisStudent.conversionScore);

					matchReasons[i][j][k] = new ValueAndReason(terms[k], (terms[k] > 0)? "conversion score of " + thisStudent.conversionScore: "null");
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("be sure that the number of terms is equal to numReasonsToPrint. " + k + " should equal " + numReasonsToPrint);
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
		return new Triple<double[][], HashMap<String, Match>, ValueAndReason[][][]> (matchScores, matchMap, matchReasons); 
	}

	private static Triple<Double, Double, Match> matchValue(Student student, Alumni alumni, Match match, 
			double thisDistance, int priorityIndex, double academicInterestImportance, double coCurricularInterestImportance) {
		String thisPriority = alumni.priorityTypes.get(priorityIndex);
		//for each list below, the index that the value is at corresponds to the number priority it belongs to.
		if (thisPriority.equals("Academic Interest")) {
			if (student.majorInterests.contains(alumni.statedPriority.get(priorityIndex))) {
				match.academicInterestMatch = true;
				return new Triple<Double, Double, Match>(1.0, academicInterestImportance, match);
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
					return new Triple<Double, Double, Match>(1.0, coCurricularInterestImportance, match);
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

	public static String[][] createVariables(ArrayList<Alumni> alumniList, ArrayList<Student> studentList) {
		String[][] variableNames = new String[studentList.size()][alumniList.size()];
		for (int i = 0; i < studentList.size(); i++) {
			for (int j = 0; j < alumniList.size(); j++) {
				Student thisStudent = studentList.get(i);
				Alumni thisAlumni = alumniList.get(j);
				//create variables matrix
				variableNames[i][j] = thisAlumni.name + thisStudent.ref ;
			}
		}
		return variableNames;
	}
}
