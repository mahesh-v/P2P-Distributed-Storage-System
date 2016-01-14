package data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SearchMessage implements Serializable {

	/**
	 * Auto-generated serial version ID
	 */
	private static final long serialVersionUID = -8841541608964222415L;
	
	private String word;
	private String request_id;
	private int hopCount;
	private LocalDateTime timeGenerated;
	private int timeoutPerHop;
	private List<String> searchResults = new ArrayList<String>();
	private int initiatorNode;
	private boolean allRespliesReceived = false;
	
	public SearchMessage(String sWord, String req_id, int hop_count, int timeout_per_hop, LocalDateTime time_generated){
		this.word = sWord;
		this.request_id = req_id;
		this.hopCount = hop_count;
		this.timeoutPerHop = timeout_per_hop;
		this.timeGenerated = time_generated;
		this.initiatorNode = MyData.getMyData().getMyNodeNum();
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getHopCount() {
		return hopCount;
	}
	public void decrementHopCount() {
		this.hopCount--;
	}
	public LocalDateTime getTimeGenerated() {
		return timeGenerated;
	}
	public void setTimeGenerated(LocalDateTime timeGenerated) {
		this.timeGenerated = timeGenerated;
	}
	public String getRequest_id() {
		return request_id;
	}
	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}
	public List<String> getSearchResults() {
		return searchResults;
	}
	
	public void mergeResultsFrom(List<String> list){
		this.searchResults.addAll(list);
	}
	public int getTimeoutPerHop() {
		return timeoutPerHop;
	}
	public void setTimeoutPerHop(int timeoutPerHop) {
		this.timeoutPerHop = timeoutPerHop;
	}
	
	public boolean isExpired(){
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime tempTime = LocalDateTime.from(timeGenerated);
		long difference = tempTime.until(currentTime, ChronoUnit.MILLIS);
		if(difference > (hopCount*timeoutPerHop))
			return true;
		return false;
	}

	public int getInitiatorNode() {
		return initiatorNode;
	}

	public boolean isAllRespliesReceived() {
		return allRespliesReceived;
	}

	public void setAllRespliesReceived(boolean allRespliesReceived) {
		this.allRespliesReceived = allRespliesReceived;
	}
	
}
