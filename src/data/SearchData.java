package data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SearchData {
	private static final SearchData searchData = new SearchData();
	private HashMap<String, ArrayList<String>> indexMap = new HashMap<String, ArrayList<String>>();
	private HashSet<String> keyWordSet = new HashSet<String>();
	private ConcurrentHashMap<String, SearchTracker> searchTrackerMap = new ConcurrentHashMap<String, SearchTracker>();
	private List<String> myLatestSearchResult = new ArrayList<String>();
	private Path filesLocation;
	private int searchCount;
	private String fileRequested;
	private HashMap<String, SearchReport> reportMap = new HashMap<String, SearchReport>();
	private int defaultTimeoutPerHop;
	
	private SearchData(){
		searchCount = 0;
	}
	
	public static SearchData getSearchData()
	{
		return searchData;
	}

	public HashMap<String, ArrayList<String>> getIndexMap() {
		return indexMap;
	}

	public HashSet<String> getKeyWordSet() {
		return keyWordSet;
	}

	public int getSearchCount() {
		return searchCount;
	}
	
	public String generateSearchID()
	{
		this.searchCount++;
		return MyData.getMyData().getMyNodeNum()+"_"+searchCount;
	}

	public ConcurrentHashMap<String, SearchTracker> getSearchTrackerMap() {
		return searchTrackerMap;
	}

	public List<String> getMyLatestSearchResult() {
		return myLatestSearchResult;
	}

	public void setMyLatestSearchResult(List<String> myLatestSearchResult) {
		this.myLatestSearchResult = myLatestSearchResult;
	}

	public String getFilesLocation() {
		return filesLocation.toString();
	}

	public void setFilesLocation(Path filesLocation) {
		this.filesLocation = filesLocation;
	}

	public String getFileRequested() {
		return fileRequested;
	}

	public void setFileRequested(String fileRequested) {
		this.fileRequested = fileRequested;
	}

	public HashMap<String, SearchReport> getReportMap() {
		return reportMap;
	}

	public int getDefaultTimeoutPerHop() {
		return defaultTimeoutPerHop;
	}

	public void setDefaultTimeoutPerHop(int defaultTimeoutPerHop) {
		this.defaultTimeoutPerHop = defaultTimeoutPerHop;
	}
}
