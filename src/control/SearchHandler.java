package control;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sockets.Server;
import data.MyData;
import data.SearchData;
import data.SearchMessage;
import data.SearchReport;
import data.SearchTracker;

public class SearchHandler {

	public static List<String> handleInitialSearchRequest(String word) {
		int timeOutPerHop = SearchData.getSearchData().getDefaultTimeoutPerHop();
		String[] inputs = word.split(",");
		if(inputs.length > 1){
			timeOutPerHop = Integer.parseInt(inputs[1]);
		}
		String searchWord = inputs[0];
		System.out.println("Initiating search with search word: "+searchWord);
		System.out.println("Timeout per hop count: "+timeOutPerHop+"ms");
		SearchReport sr = new SearchReport();
		sr.setSearchWord(searchWord);
		long start = System.currentTimeMillis();
		ArrayList<String> searchResults = getResultsFromMyNode(searchWord);
//		if(searchResults.size() > 0){
//			sr.setHopCount(0);
//			sr.setTimeElapsed(System.currentTimeMillis() - start);
//			SearchData.getSearchData().getReportMap().put(SearchData.getSearchData().generateSearchID(), sr);
//			return searchResults;
//		}
		
		//send search to neighbors
		SearchMessage searchResult = null;
		int hopCount = 1;
		String req_id = null;
		for (; hopCount <= 16; hopCount*=2) {
			req_id = SearchData.getSearchData().generateSearchID();
			SearchMessage searchMessage = new SearchMessage(searchWord, req_id, hopCount, timeOutPerHop, LocalDateTime.now());
			SearchTracker sTracker = new SearchTracker(searchMessage, MyData.getMyData().getMyNodeNum(), MyData.getMyData().getNeighbors().size());
			SearchData.getSearchData().getSearchTrackerMap().put(req_id, sTracker);
			System.out.println("Sending out requests with hop count "+hopCount);
			for (Server neighbor : MyData.getMyData().getNeighbors()) {
				neighbor.sendObject(searchMessage);
			}
			while(!searchMessage.isExpired()&&SearchData.getSearchData().getSearchTrackerMap().get(req_id).getNumOfRepliesExpected()>0);
			searchResult = SearchData.getSearchData().getSearchTrackerMap().get(req_id).getSMessage();
			if(searchResult!= null && searchResult.getSearchResults().size() > 0) {
				System.out.println("Found results");
				break;
			}
		}
		if(searchResult == null)
			return null;
		searchResult.mergeResultsFrom(searchResults);
		if(searchResult.getSearchResults()!= null && !searchResult.getSearchResults().isEmpty()){
			sr.setHopCount(hopCount);
			sr.setTimeElapsed(System.currentTimeMillis() - start);
			SearchData.getSearchData().getReportMap().put(req_id, sr);
		}
		return searchResult.getSearchResults();
	}

	public static ArrayList<String> getResultsFromMyNode(String searchWord) {
		ArrayList<String> resultList = new ArrayList<String>();
		if(SearchData.getSearchData().getKeyWordSet().contains(searchWord.toLowerCase()))
		{
//			System.out.println("Found result in this node:");
			HashMap<String, ArrayList<String>> indexMap = SearchData.getSearchData().getIndexMap();
			for (String fileName : indexMap.keySet()) {
				ArrayList<String> keyWords = indexMap.get(fileName);
				boolean added = false;
				for (String keyWord : keyWords) {
					if(keyWord.equalsIgnoreCase(searchWord)){
						resultList.add("("+searchWord+", "+fileName+", node "+MyData.getMyData().getMyNodeNum()+")");
						added=true;
					}
				}
				if(!added && fileName.contains(searchWord.toLowerCase()))
					resultList.add("("+searchWord+", "+fileName+", node "+MyData.getMyData().getMyNodeNum()+")");
			}
		}
		return resultList;
	}
}
