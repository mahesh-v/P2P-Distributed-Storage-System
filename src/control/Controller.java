package control;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import sockets.AcceptingServer;
import sockets.InitiatingServer;
import sockets.Server;
import data.DestinationNode;
import data.MyData;
import data.SearchData;
import data.SearchReport;


public class Controller {
	private static final int STANDARD_PORT = 5051;
	private static final String NETWORK_NODE_TXT = "networkNode.txt";
	private static final int DEFAULT_TIMEOUT_PER_HOP = 500;

	public static void main(String[] args) throws UnknownHostException {
		int nodeNum = args.length>0?Integer.parseInt(args[0]):0;
		String localAddress = InetAddress.getLocalHost().getHostName();
		MyData.getMyData().setShutDown(false);
		MyData.getMyData().setMyNodeNum(nodeNum);
		MyData.getMyData().setNetworksNodeFileName(NETWORK_NODE_TXT);
		MyData.getMyData().setPort(STANDARD_PORT);
		SearchData.getSearchData().setFilesLocation(Paths.get("Files","Node"+nodeNum));
		SearchData.getSearchData().setDefaultTimeoutPerHop(DEFAULT_TIMEOUT_PER_HOP);
		MessageProcessor mp = new MessageProcessor();
		mp.start();
		AcceptingServer as = new AcceptingServer(STANDARD_PORT);
		as.start();
		DestinationNode destNode = getNodeToConnectToFromFile(NETWORK_NODE_TXT);
		if(destNode!= null){
			InitiatingServer is = new InitiatingServer(destNode.getDestAddress(), destNode.getDestNodeNum(), STANDARD_PORT);
			is.connectToServer();
		}
		writeToFile(NETWORK_NODE_TXT, nodeNum, localAddress);
		
		CLIController cli = new CLIController();
		cli.startNetworkOperations();
		
		connectNeighbors();
		as.close();
		closeNeighborConnections();
		MyData.getMyData().setShutDown(true);
		removeFromFile(NETWORK_NODE_TXT, MyData.getMyData().getMyNodeNum());
		displayReport();
		cleanUpThreads();
	}

	private static void connectNeighbors() {
		List<Server> neighbors = MyData.getMyData().getNeighbors();
		if(neighbors.size() == 0)
			return;
		Server firstNeighbor = neighbors.get(0);
		String nodesToConnect = "";
		for (int i = 1; i < neighbors.size(); i++) {
			Server neighbor = neighbors.get(i);
			nodesToConnect = nodesToConnect+neighbor.getNodeConnectedTo()+","+neighbor.getNodeAddress()+"\t";
			neighbor.sendObject("LEAVING");
		}
		neighbors = null;//free up this memory
		if(!nodesToConnect.equals("")){
			System.out.println(firstNeighbor.getNodeConnectedTo()+" CONNECT_TO\t"+nodesToConnect);
			firstNeighbor.sendObject("CONNECT_TO\t"+nodesToConnect);
		}
		try {
			Thread.sleep(150);//let the neighbors connect before leaving...
		} catch (InterruptedException e) {}
		firstNeighbor.sendObject("LEAVING");
	}

	private static void closeNeighborConnections() {
		List<Server> list = MyData.getMyData().getNeighbors();
		List<Integer> neighborsToClose = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			Server server = list.get(i);
			if(server!= null)
				neighborsToClose.add(server.getNodeConnectedTo());
		}
		for (Integer i : neighborsToClose) {
			MyData.getMyData().removeNeighbor(i);
		}
	}
	
	private static void removeFromFile(String fileName, int nodeNum) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(fileName));
			List<String> new_lines = new ArrayList<String>();
			for (String line : lines) {
				if(!line.contains(nodeNum+"\t"))
					new_lines.add(line);
			}
			Files.write(Paths.get(fileName), new_lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void cleanUpThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread thread : threadSet) {
			if(thread.isAlive()&&thread.getName().startsWith("ThreadTo"))
				thread.stop();
		}
	}

	private static DestinationNode getNodeToConnectToFromFile(String fileName) {
		try {
			if(Files.notExists(Paths.get(fileName)))
				return null;
			List<String> lines = Files.readAllLines(Paths.get(fileName));
			if(lines.size() == 0)
				return null;
			int randNum = ThreadLocalRandom.current().nextInt(lines.size());
			String line = lines.get(randNum);
			String[] fields = line.split("\t");
			if(fields.length != 2)
				return null;
			DestinationNode dn = new DestinationNode();
			dn.setDestNodeNum(Integer.parseInt(fields[0]));
			dn.setDestAddress(fields[1]);
			return dn;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void writeToFile(String fileName, int nodeNum, String localAddress) {
		String content = nodeNum+"\t"+localAddress;
		List<String> lines = new ArrayList<String>();
		lines.add(content);
		try {
			Files.write(Paths.get(fileName), lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void displayReport()
	{
		System.out.println("My Search Report(only successful searches): \r\n");
		HashMap<String, SearchReport> reportMap = SearchData.getSearchData().getReportMap();
		if(reportMap.isEmpty())
			System.out.println("No search items from this node.");
		for (String key : reportMap.keySet()) {
			System.out.print("Search phrase = \""+reportMap.get(key).getSearchWord()+"\",\tHop count used = "+reportMap.get(key).getHopCount()+",\tTime taken = "+reportMap.get(key).getTimeElapsed()+"ms\r\n");
		}
	}
}
