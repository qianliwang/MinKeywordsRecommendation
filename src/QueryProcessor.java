import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class QueryProcessor {

	private IndexBuilder index;
	
	
	public QueryProcessor(String folderPath){
		this.index = new IndexBuilder(folderPath);
		HashMap<String,Document> docMap = this.index.getDocumentMap();
	}
	
	public void query(String q, int k){
		
		String tArray[] = q.split(" ");
		ArrayList<String> tPosting;
		
		String postArray[];
		String docName;
		ArrayList<WeightedDocument> tDocList;
		HashMap<String,WeightedDocument> docMap = new HashMap<String,WeightedDocument>();
		
		double weight;
		double w;
		String tempName;
		double queryWeight[] = new double[tArray.length];
		int termFrequency;
		double queryLength = 0;
		for(int i=0;i<tArray.length;i++){
			termFrequency = this.index.getIndex().get(tArray[i]);
			queryWeight[i] = calcWeight(1,termFrequency,this.index.getDocumentMap().keySet().size());
			queryLength = queryLength + queryWeight[i]*queryWeight[i];
		}
		
		queryLength = Math.sqrt(queryLength);
		
		for(int i=0;i<queryWeight.length;i++){
			queryWeight[i] = queryWeight[i]/queryLength;
		}
		
		tDocList = null;
		
		for(int i=0;i<tArray.length;i++){
//			tDocList = this.index.getWeights(tArray[i]);
			for(WeightedDocument d:tDocList){
				tempName = d.getName();
				weight = d.getWeight();
				weight = weight*queryWeight[i];
				if(docMap.containsKey(tempName)){
					w = docMap.get(tempName).getWeight();
					docMap.get(tempName).setWeight(w+weight);
				}else{
					docMap.put(tempName, new WeightedDocument(tempName,weight));
				}
			}
		}
		ArrayList<WeightedDocument> docList = new ArrayList<WeightedDocument>();
		
		for(String fileName:docMap.keySet()){
			docList.add(docMap.get(fileName));
		}
		
		Collections.sort(docList,WeightedDocument.weightComparator);
		
		System.out.println("*************Sorted By Weight*************");
		for(WeightedDocument wd:docList){
			System.out.println(wd.getName());
		}
	}
	private double calcWeight(int tf_td,int df_t, int numOfDocs){
		double weight = Math.log(1.0+tf_td)*Math.log10(numOfDocs/df_t);
		return weight;
	}
}