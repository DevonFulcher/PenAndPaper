package MainPackage;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ArrayListOverrideToString<T> extends ArrayList<T> {
	//This is needed to print lists without commas because CSV are comma delimited
	@Override
	public String toString() {
		String printString = "[" + this.get(0);
		for(int i = 1; i < this.size(); i++) {
			printString = printString + " " + this.get(i);
		}
		return printString + "]";
	}
}
