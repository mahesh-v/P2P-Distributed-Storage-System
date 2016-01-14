package control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sockets.InitiatingServer;
import sockets.Server;
import data.MyData;
import data.SearchData;

public class CLIController {

	public void startNetworkOperations() {
		populateIndexMap();
		CleanupThread cuT = new CleanupThread();
		cuT.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Initiating the server");
		while(!MyData.getMyData().isShutDown()){
			System.out.print("> ");
			String line = null;
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.err.println("Error while reading line from console. Leaving network...");
				return;
			}
			if(line.equalsIgnoreCase("QUIT"))
				return;
			else if(line.equalsIgnoreCase("NEIGHBORS")){
				System.out.println("My neighbors are: ");
				for (Server neighbor : MyData.getMyData().getNeighbors()) {
					System.out.println(neighbor.getNodeConnectedTo()+" at "+neighbor.getNodeAddress());
				}
			}
			else if(line.startsWith("SEARCH")||line.startsWith("search")){
				List<String> searchResults = SearchHandler.handleInitialSearchRequest(line.substring(line.indexOf("\t")+1));
				SearchData.getSearchData().getMyLatestSearchResult().clear();
				SearchData.getSearchData().getMyLatestSearchResult().addAll(searchResults);
				if(searchResults!= null && searchResults.size()>0){
					System.out.println("Results(searchword, filename, location):");
					for (int i = 0; i < searchResults.size(); i++) {
						System.out.println((i+1)+". "+searchResults.get(i));
					}
				}
				else {
					System.out.println("No results found.");
				}
			}
			else if(line.startsWith("GET")||line.startsWith("get")){
				String interest = line.substring(line.toLowerCase().indexOf("get")+4);
				String fileName = null;
				try{
					int interestedInt = Integer.parseInt(interest.trim())-1;
					String interestedLine = SearchData.getSearchData().getMyLatestSearchResult().get(interestedInt);
					if(interestedLine == null){
						System.out.println("No prior search with given index. Please select a valid option after a successful search.");
						continue;
					}
					String[] values = interestedLine.split(",");
					fileName = values[1].trim();
					int nodeNum = Integer.parseInt(values[2].substring(values[2].indexOf("node")+5, values[2].indexOf(")")).trim());
					if(nodeNum == MyData.getMyData().getMyNodeNum()){
						System.out.println("Selected option already exists in current node.");
						continue;
					}
					String destinationAddress = getAddressFromFile("networkNode.txt", nodeNum);
					System.out.println("Initiating GET request for file "+fileName+" from node"+nodeNum+" addressed at "+destinationAddress);
					Server peer = MyData.getMyData().getNeighborNodeNum(nodeNum);
					if(peer == null){
						InitiatingServer ias = new InitiatingServer(destinationAddress, nodeNum, MyData.getMyData().getPort());
						peer = ias.connectToServer();
						peer.setDisconnectAfterFileTransfer(true);
					}
					SearchData.getSearchData().setFileRequested(fileName);
					peer.sendObject("REQUEST_FILE\t"+fileName);
					System.out.println("Request sent for file: "+fileName);
				} catch(NumberFormatException nfe){
					System.out.println("Please choose the index of file. Expected: GET <number>");
				}
			}
			else if(line.equalsIgnoreCase("index")){
				HashMap<String, ArrayList<String>> indexMap = SearchData.getSearchData().getIndexMap();
				System.out.println("The files and keywords I have are: ");
				for (String fileName : indexMap.keySet()) {
					System.out.print(fileName+"\t");
					ArrayList<String> keywords = indexMap.get(fileName);
					for (String keyword : keywords) {
						System.out.print(keyword+",");
					}
					System.out.println();
				}
			}
		}
	}

	private String getAddressFromFile(String networksNodeFileName, int nodeNum) {
		try {
			if(Files.notExists(Paths.get(networksNodeFileName)))
				return null;
			List<String> lines = Files.readAllLines(Paths.get(networksNodeFileName));
			if(lines.size() == 0)
				return null;
			String line = "";
			for (String string : lines) {
				if(string.startsWith(nodeNum+"\t"))
					line=string;
			}
			String[] fields = line.split("\t");
			if(fields.length != 2)
				return null;
			return fields[1];
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void populateIndexMap() {
		try {
			Path path = Paths.get(SearchData.getSearchData().getFilesLocation(), "index.txt");
			if(!Files.exists(path)){
				System.out.println("WARNING: Index file not found. Unable to accept any search requests.");
				return;
			}
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				if(line.trim().isEmpty())
					continue;
				String[] words = line.split("\t");
				ArrayList<String> keyWordList = new ArrayList<String>();
				String[] keyWords = words[1].split(",");
				for (String keyWord : keyWords) {
					keyWordList.add(keyWord.toLowerCase());
					SearchData.getSearchData().getKeyWordSet().add(keyWord.toLowerCase());
				}
				SearchData.getSearchData().getKeyWordSet().add(words[0].toLowerCase());
				SearchData.getSearchData().getIndexMap().put(words[0].toLowerCase(), keyWordList);
			}
			System.out.println("Index map populated");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
