import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IndexBuilder {

	private String folderPath;
	private ArrayList<Document> docList;
	
	private HashMap<String,Integer> index;
	private HashMap<String,ArrayList<String>> posting;
	
	private HashMap<String,Double> docLengthMap;
	private HashMap<String,Document> docMap;
	
	private int numOfDocs;
	
	public IndexBuilder(String folderPath){
		this.folderPath = folderPath;
		this.index = new HashMap<String,Integer>();
		this.posting = new HashMap<String,ArrayList<String>>();
		this.docList = new ArrayList<Document>();
		this.docLengthMap = new HashMap<String,Double>();
		this.docMap = new HashMap<String,Document>();
		
		this.numOfDocs = 0;
		
		buildIndex(this.folderPath,this.index,this.posting,this.docList);
		System.out.println("Finish Building Index");
		
		findDocumentLength(this.index,this.docList,this.docLengthMap);
		System.out.println("Finish Finding Length");
		
		//Initialize the HashMap<String,Document> docMap
		for(Document d:this.docList){
			docMap.put(d.getFileName(), d);
		}
	}
	
	public ArrayList<Document> getDocuments(){
		return docList;
	}
	
	public HashMap<String, Integer> getIndex() {
		return index;
	}

	public HashMap<String, ArrayList<String>> getPosting() {
		return posting;
	}

	public HashMap<String,Document> getDocumentMap(){
		HashMap<String,Document> docMap = new HashMap<String,Document>();
		for(Document d: this.docList){
			docMap.put(d.getFileName(), d);
		}
		return docMap;
	}
	
	/**
	 * Read the files in folder, then fill up the index and posting.
	 * @param folderPath,
	 * @param index, HashMap<String,Integer>
	 * @param posting, HashMap<String,ArrayList<String>>
	 * @param docList, ArrayList<Document> 
	 */
	private void buildIndex(String folderPath,HashMap<String,Integer> index,HashMap<String,ArrayList<String>> posting,ArrayList<Document> docList){
		File folder = new File(folderPath);	
		File[] listOfFiles = folder.listFiles();
		this.numOfDocs = listOfFiles.length;
		
		System.out.println(folderPath+" contains "+listOfFiles.length+" files.\n");
		Document d;
		int count;
		String tempString;
		ArrayList<String> tempStrList;
		for(int i=0;i<listOfFiles.length;i++){
//			System.out.println(listOfFiles[i].getAbsolutePath());
			d = new Document(listOfFiles[i].getAbsolutePath());	
			docList.add(d);
			for(String s:d.getTerms().keySet()){
				if(index.containsKey(s)){
					count = index.get(s);
					count++;
					index.put(s,count);
				}else{
					index.put(s,1);
				}
				tempString = d.getFileName()+","+d.getTerms().get(s);
				if(posting.containsKey(s)){
					posting.get(s).add(tempString);
				}else{
					tempStrList = new ArrayList<String>();
					tempStrList.add(tempString);
					posting.put(s, tempStrList);
				}		
			}
		}
		
		System.out.println("There are "+index.keySet().size()+" terms in total!");
		
		HashMap<Integer,Integer> termStats = new HashMap<Integer,Integer>();
		
		for(String s:index.keySet()){
			if(!termStats.containsKey(index.get(s))){
				termStats.put(index.get(s), 1);
			}else{
				int temp = termStats.get(index.get(s));
				temp++;
				termStats.put(index.get(s),temp);
			}
		}
		int count2 = 0;
		for(Integer i:termStats.keySet()){
			System.out.println(i+","+termStats.get(i));
			count2 += termStats.get(i);
		}
		System.out.println(count2);
	}
	/*
	public double getWeight(String t,String d){
		ArrayList<String> termPosting = this.posting.get(t);
		String array[];
		int tf_td = 0;
		double weight;
		for(String s:termPosting){
			array = s.split(",");
			if(array[0].equals(d)){
				tf_td = Integer.parseInt(array[1]);
				break;
			}
		}
		
		int df_t = this.index.get(t);
		
		weight = calcWeight(tf_td,df_t,this.numOfDocs);
		
		return weight;
	}
	*/
	private double calcWeight(int tf_td,int df_t, int numOfDocs){
		double weight = Math.log(1.0+tf_td)*Math.log10(numOfDocs/df_t);
//		double weight = Math.log(1.0+tf_td)*Math.log10(numOfDocs+1-df_t)*Math.log10(df_t);
		return weight;
	}
	/*
	public ArrayList<WeightedDocument> getWeights(String t){
		ArrayList<WeightedDocument> tDocList = new ArrayList<WeightedDocument>();
		
		ArrayList<String> termPosting = this.posting.get(t);
		int df_t = this.index.get(t);
		int tf_td = 0;
		
		String array[];
		double weight;
		
		WeightedDocument d;
		
		for(String s:termPosting){
			array = s.split(",");
			tf_td = Integer.parseInt(array[1]);
			weight = calcWeight(tf_td,df_t,this.numOfDocs)/this.docLengthMap.get(array[0]);
			d = new WeightedDocument(array[0],weight);
			tDocList.add(d);
		}
		
		return tDocList;
	}
	*/
	public void getWeights(String t){
//		ArrayList<WeightedDocument> tDocList = new ArrayList<WeightedDocument>();
		
		ArrayList<String> termPosting = this.posting.get(t);
		int df_t = this.index.get(t);
		int tf_td = 0;
		
		String array[];
		double weight;
		
		for(String s:termPosting){
			array = s.split(",");
			tf_td = Integer.parseInt(array[1]);
			weight = calcWeight(tf_td,df_t,this.numOfDocs)/this.docLengthMap.get(array[0]);
			
			this.docMap.get(array[0]).getTermScores().put(t, weight);
/*			
			for(Document d:this.docList){
				if(d.getFileName().contains(array[0])){
					d.getTermScores().put(t, weight);
				}
			}
*/			
		}
		
	}
	private void findDocumentLength(HashMap<String,Integer> index,ArrayList<Document> docList,HashMap<String,Double> docLengthMap){
		
		String tempDocName;
		double termWeight;
		double squareSum = 0;
		double docLength;
		
		for(Document d:docList){
			squareSum = 0;
			tempDocName = d.getFileName();
			for(String term:d.getTerms().keySet()){
				termWeight = calcWeight(d.getTerms().get(term),index.get(term),this.numOfDocs);
				squareSum = squareSum+termWeight*termWeight;
			}
			docLength = Math.sqrt(squareSum);
//			System.out.println(d.getFileName()+" length : "+squareSum);
			docLengthMap.put(tempDocName, docLength);
		}
	}
	
	public void PrintIndexPostings(){
		for(String s: this.index.keySet()){
			System.out.println(s+"\t"+this.index.get(s)+this.posting.get(s)+"\t");
		}
	}
		
	public void HillClimbing(Document d){
		
		HashMap<String,Integer> termSet = new HashMap<String,Integer>();
		for(String s:d.getTerms().keySet()){
			termSet.put(s, 0);
		}
		ArrayList<String> highRankedTermList = new ArrayList<String>();
		String terminRound = "";
		
		while(!this.docList.get(0).getFileName().equals(d.getFileName())&&termSet.size()!=0){
			terminRound = selectHighRankedTerm(highRankedTermList,d,this.docList,termSet.keySet());
			highRankedTermList.add(terminRound);
			termSet.remove(terminRound);
			for(int i=0;i<this.docList.size();i++){
				this.docList.get(i).weightScore = 0;
			}
			getScoreWithTerms(highRankedTermList,this.posting,this.docMap);
			Collections.sort(this.docList,Document.weightComparator);
		
			System.out.print(d.getFileName()+",");
			
			for(String s:highRankedTermList){
				System.out.print(s+",");
			}
			System.out.println();
			
		}
		System.out.println("########################################");
		
	}
	
	public void getScoreWithTerms(ArrayList<String> termList,HashMap<String,ArrayList<String>> posting,HashMap<String,Document> docMap){
		for(String s:termList){
			ArrayList<String> termPosting = posting.get(s);
			String array[];
			for(String p:termPosting){
				array = p.split(",");
				Document d = docMap.get(array[0]);
				d.weightScore += d.getTermScores().get(s);
			}
	/*		
			for(Document d:docList){
				if(d.getTermScores().containsKey(s)){
					d.weightScore += d.getTermScores().get(s);
//					System.out.println(d.weightScore);
				}
			}
	*/		
		}	
	}
	public String selectHighRankedTerm(ArrayList<String> termList, Document targetD, ArrayList<Document> docList,Set<String> candidateTermSet){
		int highestRankinRound = docList.size();
		int tempRank;
		String highestRankTerm = "";
		for(String term: candidateTermSet){
			
			if(!targetD.getTermScores().containsKey(term)){
				getWeights(term);
			}
			for(int i=0;i<docList.size();i++){
				docList.get(i).weightScore = 0;
			}
			termList.add(term);
			getScoreWithTerms(termList,this.posting,this.docMap);
			Collections.sort(docList,Document.weightComparator);
			tempRank = 1;
			for(int i=0;i<docList.size();i++){
				if(docList.get(i).getFileName().equals(targetD.getFileName())){
					break;
				}
				tempRank++;
			}
			if(highestRankinRound > tempRank){
				highestRankinRound = tempRank;
				highestRankTerm = term;
//				System.out.println(term+","+highestRankinRound);
			}
			termList.remove(termList.size()-1);
		}
		System.out.println(targetD.getFileName()+","+highestRankTerm+","+highestRankinRound);
		return highestRankTerm;
		
	}
	
	public void searchForKeywords(String outputPath) throws FileNotFoundException{
				
		ArrayList<String> singleKeywordList;
		ArrayList<ArrayList<String>> keywordListinSingleDocument;
		
		PrintWriter pw = new PrintWriter(outputPath);
		
		for(int i=0;i<this.docList.size();i++){
			this.docList.get(i).score = i;
		}
		
		HashMap<Integer,ArrayList<String>> resultStat = new HashMap<Integer,ArrayList<String>>();
		
		for(int i=0;i<this.docList.size();i++){
			
			Collections.sort(this.docList);
			Document d = this.docList.get(i);
			System.out.println(d.getFileName()+","+d.getTerms().keySet().size());
			keywordListinSingleDocument = new ArrayList<ArrayList<String>>();
			singleKeywordList = searchKeywordsinSingleDoc(d);		
			
			System.out.println(d.getFileName()+","+singleKeywordList.size());
			for(String s:singleKeywordList){
				keywordListinSingleDocument.add(new ArrayList<String>(Arrays.asList(s)));
			}
			if(!resultStat.containsKey(singleKeywordList.size())){
				ArrayList<String> fileNameList = new ArrayList<String>();
				fileNameList.add(this.docList.get(i).getFileName());
				resultStat.put(singleKeywordList.size(), fileNameList);
			}else{
				resultStat.get(singleKeywordList.size()).add(this.docList.get(i).getFileName());
			}
			List<ArrayList<String>> doubleKeywordList = generateKeywordSet(d,singleKeywordList);
			
			for(ArrayList<String> doubleKeywords:doubleKeywordList){
				for(int j=0;j<this.docList.size();j++){
					this.docList.get(j).weightScore = 0;
				}
				getScoreWithTerms(doubleKeywords,this.posting,this.docMap);
				Collections.sort(this.docList,Document.weightComparator);
				if(this.docList.get(0).getFileName().equals(d.getFileName())){
					keywordListinSingleDocument.add(doubleKeywords);
				}
			}
			for(int j=0;j<this.docList.size();j++){
				this.docList.get(j).weightScore = 0;
			}
			System.out.println(d.getFileName()+","+keywordListinSingleDocument.size());	
			System.out.println("**************************");
			pw.append(d.getFileName()+";"+keywordListinSingleDocument.size()+";");
			for(int j=0;j<keywordListinSingleDocument.size();j++){
				ArrayList<String> tempList = keywordListinSingleDocument.get(j);
				for(String s:tempList){
					pw.append(s+",");
				}
				pw.append(";");
			}
			pw.append("\n");
			pw.flush();
		}
		
		
		pw.close();

		pw = new PrintWriter(outputPath+"_stat");
		for(Integer ii:resultStat.keySet()){
			System.out.println(ii+","+resultStat.get(ii).size());
			pw.println("Size:" + ii);
			for(String s:resultStat.get(ii)){
				pw.println(s);
			}
		}
		pw.flush();
		pw.close();
	}
	
	
	private ArrayList<String> searchKeywordsinSingleDoc(Document d){
		ArrayList<String> singleKeywordList = new ArrayList<String>();
		double termWeight;
		boolean isTermTopRanked;
		
		for(String t:d.getTerms().keySet()){
			
			if(!d.getTermScores().containsKey(t)){
				getWeights(t);
			}
			termWeight = d.getTermScores().get(t);
			
			if(termWeight == 0){
				continue;
			}
			isTermTopRanked = true;
			
			ArrayList<String> termPosting = this.posting.get(t);
//			System.out.println(termPosting.size());
			String array[];
			double tempWeight;
			
			for(String s:termPosting){
				array = s.split(",");
				tempWeight = this.docMap.get(array[0]).getTermScores().get(t);
				if(termWeight<tempWeight){
					isTermTopRanked = false;
					break;
				}
			}
			if(isTermTopRanked){
				System.out.println(t);
				singleKeywordList.add(t);
			}
		}
		
		return singleKeywordList;
	}
	
	private List<ArrayList<String>> generateKeywordSet(Document d,ArrayList<String> singleKeywordList){
		ArrayList<String> keywordList = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> weightZeroTermList = new ArrayList<String>();
		keywordList.addAll(d.getTerms().keySet());
		keywordList.removeAll(singleKeywordList);
		for(int i=0;i<keywordList.size();i++){
			if(d.getTermScores().get(keywordList.get(i))==0){
				weightZeroTermList.add(keywordList.get(i));
			}
		}
		keywordList.removeAll(weightZeroTermList);
		if(singleKeywordList.size()!=0){
			for(int i=0;i<singleKeywordList.size();i++){
				for(int j=0;j<keywordList.size();j++){
					result.add(new ArrayList<String>(Arrays.asList(singleKeywordList.get(i),keywordList.get(j))));
				}
			}
		}
		
		for(int i=0;i<keywordList.size();i++){
			for(int j=i+1;j<keywordList.size();j++){
				result.add(new ArrayList<String>(Arrays.asList(keywordList.get(i),keywordList.get(j))));
			}
		}
		
		return result;
	}
	
	public static void main(String args[]) throws FileNotFoundException{
		String folderPath = "/Users/Watson/Desktop/pythonCode/2010-2014/";
//		String folderPath = "/Users/Watson/workspace/KeywordSearchDataset/part1_2/";
		
		IndexBuilder indexBuilder = new IndexBuilder(folderPath);
		
//		String savedTermPath = "/Users/Watson/Desktop/pythonCode/2011-2014_terms";
	//	indexBuilder.searchForKeywords(savedTermPath);
	/*	
		for(int i=0;i<indexBuilder.docList.size();i++){			
			indexBuilder.HillClimbing(indexBuilder.docList.get(i));
		}
	*/
		
//		System.out.println("==============================================");
	}

}