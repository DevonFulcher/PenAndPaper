
public class Match {
	public boolean geographicMatch = false;
	public boolean coCurricularMatch = false;
	public boolean academicInterestMatch = false;
	public boolean priorityOne = false;
	public boolean priorityTwo = false;
	public boolean priorityThree = false;
	public double distanceOne = -1;
	public double distanceTwo = -1;
	public double distanceThree = -1;
	public double[] distances;
	
	public Match(int numPriorities) {
		this.distances = new double[numPriorities];
	}
}
