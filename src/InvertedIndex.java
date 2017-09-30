import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class InvertedIndex {

	private ArrayList<Document> docList;
	private HashMap<String,Integer> termIDFMap;
	private TreeSet<String> allTerms;
	
	public InvertedIndex(String folderPath){
		
		this.docList = new ArrayList<Document>();
		this.allTerms = new TreeSet<String>();
		
		File folder = new File(folderPath);
		
		for(File f: folder.listFiles()){
			if(f.isFile() && !f.isHidden()){
				Document d = new Document(f.getAbsolutePath());
				this.allTerms.addAll(d.getTerms().keySet());
				this.docList.add(d);
			}
		}
		System.out.println("Files: "+this.docList.size()+", terms: "+this.allTerms.size());
	}
	
	public void printResult(){
		for(String term:termIDFMap.keySet()){
			System.out.println(term+","+termIDFMap.get(term));
		}
	}

	public void testEachFile(){
		
		for(int i=0;i<this.docList.size();i++){
			HashMap<String,Integer> termIDFMap = new HashMap<String,Integer>();
			for(String term:this.docList.get(i).getTerms().keySet()){
				termIDFMap.put(term, 0);	
			}
			
//			System.out.println("Document of interest: "+docList.get(targetDocIndex).getFileName());
			
			for(String term:termIDFMap.keySet()){
				int count = 0;
				for(Document d:docList){
					if(d.getTerms().containsKey(term)){
						d.score++;
						count++;
					}
				}
				termIDFMap.put(term, count);
			}
			findKeywords(termIDFMap,this.docList);
			for(Document d:this.docList){
				d.score = 0;
			}
		}
	}
	
	public void findKeywords(HashMap<String,Integer> termIDFMap, ArrayList<Document> docList){
		String highestDocument = "";
		int i = 0;
		String highestRankedTerm = "";
		do {
			Collections.sort(docList);
			highestDocument = docList.get(0).getFileName();

//			System.out.println("The 1st document:" + docList.get(0).getFileName() + ", score is "
//					+ docList.get(0).score);

			Document secondOne = docList.get(1);
//			System.out.println("The 2nd document:" + docList.get(1).getFileName() + ", score is "
//					+ docList.get(1).score);

			int highestRank = 0;
			highestRankedTerm = "";
			for (String term : secondOne.getTerms().keySet()) {
				if (termIDFMap.containsKey(term) && termIDFMap.get(term) > highestRank) {
					highestRank = termIDFMap.get(term);
					highestRankedTerm = term;
				}
			}
//			termIDFMap.put(highestRankedTerm, 0);
			termIDFMap.remove(highestRankedTerm);
			for (Document d : docList) {
				if (d.getTerms().containsKey(highestRankedTerm)) {
					d.score--;
				}
			}
			Collections.sort(docList);
			i++;
//			System.out.println("After " + i + " round, remove term \"" + highestRankedTerm
//					+ "\", the highest ranked document is " + docList.get(0).getFileName());
		} while (highestDocument.equals(docList.get(0).getFileName()) && docList.get(1).score!=0);
/*	
		for(Document d:docList){
			System.out.println(d.getFileName()+","+d.score);
		}
*/		
		ArrayList<String> keywordList = new ArrayList<String>();
		for(String s:termIDFMap.keySet()){
//			System.out.print(s+",");
			keywordList.add(s);
		}
		System.out.println(highestDocument+","+keywordList.size()+","+docList.get(1).score);
	}
	
	public static void main(String args[]){
//		String folderPath = "/Users/Watson/workspace/KeywordSearch/20news-18828/sci.space/";
		String folderPath = "/Users/Watson/workspace/KeywordSearch/part1_whole/";

		//		int targetedIndex = 15;
		InvertedIndex ii = new InvertedIndex(folderPath);
		ii.testEachFile();
	}
}
