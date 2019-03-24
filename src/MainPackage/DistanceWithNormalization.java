package MainPackage;

public class DistanceWithNormalization {
	//the actual distance between an alumni and student in miles
	public double distance;
	//the normalized distance between an alumni and student across all alumni and students
	//higher values me lesser distance
	public double normalizedDistance;
	
	DistanceWithNormalization(double distance, double normalizedDistance) {
		this.distance = distance;
		this.normalizedDistance = normalizedDistance;
	}
	
	public String toString() {
		return "distance: " + distance + "normalizedDistance: " + normalizedDistance;
	}
}
