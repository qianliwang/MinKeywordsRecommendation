import java.util.Comparator;

/**
This class is a light-weighted class that stores only the document name and its weight, which is used for the computation of 
the ranking of documents, the class is discarded as well as InvertedIndex. 
*/
public class WeightedDocument {
	private String fileName;
	private double weight;
	
	private double length;
	
	public WeightedDocument(String fileName,double weight){
		this.fileName = fileName;
		this.weight = weight;
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getName(){
		return this.fileName;
	}
	
	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}
	public static Comparator<WeightedDocument> weightComparator 
    = new Comparator<WeightedDocument>() {

		public int compare(WeightedDocument d1, WeightedDocument d2) {

			if (d1.getWeight() == d2.getWeight()){
				return 0;
			}
			else{
				return d1.getWeight() < d2.getWeight() ? 1 : -1;
			}
		}
	};

}
