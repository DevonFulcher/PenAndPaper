package MainPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SolveLinearProgram {
	public static void createLPFile(double[][] matchScores, String[][] variableNames, ArrayList<Alumni> alumniList, ArrayList<Student> studentList) throws FileNotFoundException {
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
			totalAlumniLetters += alumni.numLetters;
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

			output.println(alumniList.get(alumni).numLetters);
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
}
