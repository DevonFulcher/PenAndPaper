package MainPackage;

public class Match {
	public boolean coCurricularMatch = false;
	public boolean academicInterestMatch = false;
	public boolean[] priorities;
	public double[] distances;
	
	public Match(int numPriorities) {
		this.distances = new double[numPriorities];
		this.priorities = new boolean[numPriorities]; //all elements are initialized to false
	}
}
