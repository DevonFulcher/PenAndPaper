package MainPackage;
import java.util.Comparator;

public class ValueAndReason {
	double value;
	String reason;
	public ValueAndReason(double value, String reason) {
		this.value = value;
		this.reason = reason;
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
}

