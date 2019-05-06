package MainPackage;
import java.util.Arrays;
import java.util.Comparator;

public class ValueAndReason {
	double value;
	//reasons contain first the match category, then the corresponding alumni section, 
	//then the corresponding student section
	String[] reasons;
	public ValueAndReason(double value, String[] reasons) {
		this.value = value;
		this.reasons = reasons;
	}

	static class ValueAndReasonComparator implements Comparator<ValueAndReason> {
		@Override
		public int compare(ValueAndReason a, ValueAndReason b) {
			if (a.value < b.value) {
				return -1;
			} else if (a.value == b.value) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	
	public String toString() {
		return "value: " + value + "\nreason: " + Arrays.toString(reasons) + "\n";
	}
}

