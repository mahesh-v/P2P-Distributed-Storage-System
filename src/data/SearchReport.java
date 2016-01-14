package data;

public class SearchReport {
	private int hopCount;
	private long timeElapsed;
	private String searchWord;
	
	public long getTimeElapsed() {
		return timeElapsed;
	}
	public void setTimeElapsed(long timeElapsed) {
		this.timeElapsed = timeElapsed;
	}
	public int getHopCount() {
		return hopCount;
	}
	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}
	public String getSearchWord() {
		return searchWord;
	}
	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}
	
}
