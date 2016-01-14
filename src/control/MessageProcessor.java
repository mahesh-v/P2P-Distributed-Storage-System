package control;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import sockets.InitiatingServer;
import sockets.Server;
import data.Message;
import data.MyData;
import data.SearchData;
import data.SearchMessage;
import data.SearchTracker;



public class MessageProcessor extends Thread {
	
	public void run()
	{
		while(!MyData.getMyData().isShutDown())
		{
			Message m = MyData.getMyData().getMessageQueue().poll();
			if(m!=null)
			{
				Object inObject = m.getMessage();
				if(inObject instanceof String)
				{
					processStringInput((String) inObject, m.getSenderNodeID());
				}
				else if(inObject instanceof SearchMessage)
				{
					processSearchRequest((SearchMessage)inObject, m.getSenderNodeID());
				}
				else if(inObject instanceof byte[])
				{
					processFileInput((byte[]) inObject, m.getSenderNodeID());
				}
				else 
				{
					System.out.println("Received message of unknown type: "+inObject.getClass());
				}
			}
		}
	}

	private void processSearchRequest(SearchMessage sr, int senderNode) {
		if(SearchData.getSearchData().getSearchTrackerMap().containsKey(sr.getRequest_id())){//then its a response message
			SearchTracker sTracker = SearchData.getSearchData().getSearchTrackerMap().get(sr.getRequest_id());
			if(sTracker.getSMessage().isExpired())//can drop it
				return;
			if(sTracker.getNodeToSendMessageTo() == MyData.getMyData().getMyNodeNum()){
				List<String> existingSearchResults = sTracker.getSMessage().getSearchResults();
				for (String result : sr.getSearchResults()) {
					if(!existingSearchResults.contains(result))
						existingSearchResults.add(result);
				}
				sTracker.decrementExpectedReplies();
			}
			else {
				sTracker.decrementExpectedReplies();
				sTracker.getSMessage().mergeResultsFrom(sr.getSearchResults());
				if(sTracker.getNumOfRepliesExpected() == 0){
					Server neighborToReplyTo = MyData.getMyData().getNeighborNodeNum(sTracker.getNodeToSendMessageTo());
					sTracker.getSMessage().setAllRespliesReceived(true);
					neighborToReplyTo.sendObject(sTracker.getSMessage());
				}
			}
		}
		else {
			ArrayList<String> myResults = SearchHandler.getResultsFromMyNode(sr.getWord());
			if(myResults.size()>0)
				sr.mergeResultsFrom(myResults);
			if(sr.isExpired())//don't respond
				return;
			if(sr.getHopCount() > 1 && MyData.getMyData().getNeighbors().size()>1){
				sr.decrementHopCount();
				SearchData.getSearchData().getSearchTrackerMap().put(sr.getRequest_id(), new SearchTracker(sr, senderNode, MyData.getMyData().getNeighbors().size()-1));
				for (Server neighbor : MyData.getMyData().getNeighbors()) {
					if(neighbor.getNodeConnectedTo() != senderNode)
						neighbor.sendObject(sr);
				}
			}
			else{
				Server neighborToSendReplyTo = MyData.getMyData().getNeighborNodeNum(senderNode);
				if(neighborToSendReplyTo != null)
					neighborToSendReplyTo.sendObject(sr);
			}
		}
	}

	private void processStringInput(String data, int senderID) {
		if(data.contains("CONNECT_TO"))
		{
			String[] fields = data.split("\t");
			if(fields.length < 2)
				System.err.println("Abnormal connect message received: "+data);
			for (int i = 1; i < fields.length; i++) {
				if(!fields[i].isEmpty())
				{
					String[] split = fields[i].split(",");
					if(split.length != 2)
						continue;
					InitiatingServer is = new InitiatingServer(split[1], Integer.parseInt(split[0]), MyData.getMyData().getPort());
					is.connectToServer();
				}
			}
		}
		else if(data.equalsIgnoreCase("LEAVING")){
			Server server = MyData.getMyData().getNeighborNodeNum(senderID);
			if (server!=null){
				server.close();
			}
//			List<Server> neighbors = MyData.getMyData().getNeighbors();
//			for (int i = 0; i < neighbors.size(); i++) {
//				Server server = neighbors.get(i);
//				if(server.getNodeConnectedTo() == senderID){
//					server.close();
//					neighbors.remove(server);
//					break;
//				}
//			}
			
		}
		else if(data.startsWith("REQUEST_FILE")){
			Server requestor = MyData.getMyData().getNeighborNodeNum(senderID);
			String fileName = data.substring(data.indexOf("\t")+1);
			//send index entry
			String indexEntry = getEntryFromFile(fileName);
			if(indexEntry == null){
				System.out.println("WARNING. Null index entry. Cannot process file request.");
				return;
			}
			requestor.sendObject("INDEX_ENTRY\t"+indexEntry);
			
			//send file
			requestor.sendFile(SearchData.getSearchData().getFilesLocation().toString()+File.separator+fileName);
			//remove from neighbor list if necessary. - Test and see. Should be removed.
		}
		else if(data.startsWith("INDEX_ENTRY")){
			String indexEntry = data.substring(data.indexOf("\t")+1);
			writeToIndexFile(indexEntry);
			String[] entries = indexEntry.split("\t");
			String[] tags = entries[1].split(",");
			ArrayList<String> keywords = new ArrayList<String>();
			for (String tag : tags) {
				SearchData.getSearchData().getKeyWordSet().add(tag);
				keywords.add(tag);
			}
			SearchData.getSearchData().getKeyWordSet().add(entries[0]);
			SearchData.getSearchData().getIndexMap().put(entries[0], keywords);
		}
		
	}

	private void writeToIndexFile(String indexEntry) {
		Path path = Paths.get(SearchData.getSearchData().getFilesLocation(), "index.txt");
		try {
			List<String> lines = new ArrayList<String>();
			System.out.println("Adding to index file: "+indexEntry);
			indexEntry = "\n"+indexEntry;
			lines.add(indexEntry);
			Files.write(path, lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getEntryFromFile(String fileName) {
		Path path = Paths.get(SearchData.getSearchData().getFilesLocation(), "index.txt");
		if(!Files.exists(path)){
			System.out.println("WARNING: Index file not found. Unable to get search entry.");
			return null;
		}
		try {
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				if(line.startsWith(fileName+"\t"))
					return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void processFileInput(byte[] input, int senderID) {
		try{
			String fileRequested = SearchData.getSearchData().getFileRequested();
			if(fileRequested == null){
				System.out.println("Received file, but no request initiated... Discarding file.");
				Server peer = MyData.getMyData().getNeighborNodeNum(senderID);
				if(peer.isDisconnectAfterFileTransfer()){
					peer.close();
				}
			}
			FileOutputStream fos = new FileOutputStream(SearchData.getSearchData().getFilesLocation().toString()+File.separator+fileRequested); 
			BufferedOutputStream bos = new BufferedOutputStream(fos); 
			bos.write(input, 0 , input.length); 
			bos.flush(); 
			bos.close();
			System.out.print("Received File "+fileRequested+" Successfully\n> ");
		} catch(IOException ioe){
			System.err.println("Error while writing received bytes to file: "+ioe.getMessage());
		}
		Server peer = MyData.getMyData().getNeighborNodeNum(senderID);
		if(peer == null){
			System.out.println("Unexpected. Peer connection not found in neighbor list.");
		} else if(peer.isDisconnectAfterFileTransfer()){
			peer.close();
			MyData.getMyData().getNeighbors().remove(peer);
		}
		SearchData.getSearchData().setFileRequested(null);
	}
}
