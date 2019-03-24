package MainPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class ProcessResults {
	public static Triple<int[], int[], int[]> readResults(HashMap<String, Match> matchMap,
			ArrayList<Student> studentList, ArrayList<Alumni> alumniList, ValueAndReason[][][] matchReasons,
			double[][] matchScores, int numReasonsToPrint, int numPriorities) throws FileNotFoundException {
		PrintWriter output = null;
		try {
			File finalOutput = new File("./results/Final Output.csv");
			output = new PrintWriter(finalOutput);
			output.print("Student,Alumni,Match Score");
			for (int i = 1; i < numReasonsToPrint; i++) {
				output.print(",Reason " + i + ",Alumni Info " + i + ",Student Info " + i);
			}
			output.println();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with finalOutput.csv already open in excel");
			throw e;
		}
		int[] numLettersForEachGroup = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] numLettersForEachMatch = { 0, 0, 0 };
		int[] numLettersForEachPriority = new int[numPriorities];
		File results = new File("./PenAndPaperResults.txt");
		// File results=new File("./src/PenAndPaperResults.txt");
		Scanner scan = new Scanner(results);
		String thisString;
		while (scan.hasNext()) {
			thisString = scan.next();
			if (matchMap.containsKey(thisString)) {
				scan.next(); // skip over asterisk in file
				if (scan.nextInt() == 1) {
					for (int i = 0; i < studentList.size(); i++) {
						Student thisStudent = studentList.get(i);
						for (int j = 0; j < alumniList.size(); j++) {
							Alumni thisAlumni = alumniList.get(j);
							if (thisString.equals(thisAlumni.name + thisStudent.ref)) {

								thisStudent.receivesLetter = true;

								// increment the count of the letters that are sent to first generation students
								if (thisStudent.firstGeneration) {
									numLettersForEachGroup[0]++;
								}
								// increment the count of the letters that are sent to cody recipients
								if (thisStudent.codyRecipient) {
									numLettersForEachGroup[1]++;
								}
								// increment the count of the letters that are sent to mood recipients
								if (thisStudent.moodRecipient) {
									numLettersForEachGroup[2]++;
								}
								// increment the count of the letters that are sent to out of state students
								if (!thisStudent.state.equals("TX")) {
									numLettersForEachGroup[3]++;
								}
								// increment the count of the letters that are sent to a particular conversion
								// score
								if (!thisStudent.noConversion) {
									switch (thisStudent.conversionScore) {
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
								}

								Match thisMatch = matchMap.get(thisAlumni.name + thisStudent.ref);
								if (thisMatch.academicInterestMatch) {
									numLettersForEachMatch[0]++;
								}
								if (thisMatch.coCurricularMatch) {
									numLettersForEachMatch[1]++;
								}

								/*
								 * TODO: add distance between alumni and student if (thisMatch.geographicMatch)
								 * { numLettersForEachMatch[2]++; }
								 */

								for (int k = 0; k < numPriorities; k++) {
									if (thisMatch.priorities[k]) {
										numLettersForEachPriority[k]++;
									}
								}

								output.print(thisStudent.ref + "," + thisAlumni.name + "," + matchScores[i][j]);

								// fill in reasons matrix
								for (int k = 0; k < numReasonsToPrint; k++) {
									for (int m = 0; m < matchReasons[i][j][k].reasons.length; m++) {
										output.print("," + matchReasons[i][j][k].reasons[m]);
									}
								}
								output.println();
							}
						}
					}
				}
			}
		}
		scan.close();
		output.close();
		return new Triple<int[], int[], int[]>(numLettersForEachGroup, numLettersForEachMatch,
				numLettersForEachPriority);
	}

	public static void categoryAnalysis(int[] numStudentsForEachGroup, int[] numLettersForEachGroup,
			int totalNumLetters, int[] numLettersForEachMatch, int numStudents, int[] numLettersForEachPriority)
					throws FileNotFoundException {
		assert numStudentsForEachGroup.length == numLettersForEachGroup.length;
		PrintWriter output = null;
		try {
			File studentCategoryAnalysis = new File("./results/Category Analysis.csv");
			output = new PrintWriter(studentCategoryAnalysis);

			// student category statistics
			String[] groupNames = { "first generation", "cody recipient", "mood recipient", "out of state",
					"conversion 1", "conversion 2", "conversion 3", "conversion 4", "conversion 5", "conversion 6" };
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

			// match statistics
			String[] matchNames = { "Academic Interest", "Co-curricular Interest", "Geographic Proximity" };
			output.println(",percent of letters sent that contained match from each category");
			for (int i = 0; i < numLettersForEachMatch.length; i++) {
				output.print(matchNames[i] + ",");
				output.println(numLettersForEachMatch[i] * 1.0 / totalNumLetters * 100);
			}
			output.println();

			// priority statistics
			output.println(",percent of letters written from each priority");
			for (int i = 0; i < numLettersForEachPriority.length; i++) {
				output.println(
						"priority " + (i + 1) + "," + numLettersForEachPriority[i] * 1.0 / totalNumLetters * 100);
			}

			output.close();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with categoryAnalysis.csv already open in excel");
			throw e;
		}
	}

	public static void createStudentsLackingLetter(ArrayList<Student> studentList) throws FileNotFoundException {
		ArrayList<Student> studentsLackingLetter = new ArrayList<>();
		for (Student student : studentList) {
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
			for (Student student : studentsLackingLetter)
				output.println(student.ref + "," + student.averageMatch);
			output.println();
			output.close();
		} catch (FileNotFoundException e) {
			System.out.println("cannot run program with studentsLackingLetters.csv already open in excel");
			throw e;
		}
	}

	public static void outputMatchScores(ArrayList<Alumni> alumniList, ArrayList<Student> studentList,
			double[][] matchScores) throws FileNotFoundException {
		PrintWriter output = null;
		try {
			File studentsLackingLetters = new File("./results/Match Scores.csv");
			output = new PrintWriter(studentsLackingLetters);
			output.print(",");
			for (Alumni alumni : alumniList) {
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