package data;

public class SearchTracker {
	private SearchMessage sMessage;
	private int nodeToSendMessageTo;
	private int numOfRepliesExpected;
	
	public SearchTracker(SearchMessage sm, int nodeToSendMsgTo, int expectedReplies) {
		this.sMessage = sm;
		this.nodeToSendMessageTo = nodeToSendMsgTo;
		this.numOfRepliesExpected = expectedReplies;
	}

	public SearchMessage getSMessage() {
		return sMessage;
	}

	public int getNodeToSendMessageTo() {
		return nodeToSendMessageTo;
	}

	public int getNumOfRepliesExpected() {
		return numOfRepliesExpected;
	}
	
	public void decrementExpectedReplies(){
		this.numOfRepliesExpected--;
	}
}
