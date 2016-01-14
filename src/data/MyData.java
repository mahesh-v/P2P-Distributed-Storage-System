package data;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;

import sockets.Server;

public class MyData {
	private static final MyData myData = new MyData();
	private boolean shutDown;
	private int myNodeNum;
	private SynchronousQueue<Message> messageQueue = new SynchronousQueue<Message>();
	private CopyOnWriteArrayList<Server> connectionList = new CopyOnWriteArrayList<Server>();
	private String networksNodeFileName;
	private int port;
	
	private MyData(){}
	
	public static MyData getMyData() {
		return myData;
	}

	public boolean isShutDown() {
		return shutDown;
	}

	public void setShutDown(boolean shutDown) {
		this.shutDown = shutDown;
	}

	public SynchronousQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	public int getMyNodeNum() {
		return myNodeNum;
	}

	public void setMyNodeNum(int myNodeNum) {
		this.myNodeNum = myNodeNum;
	}

	public List<Server> getNeighbors() {
		return Collections.synchronizedList(connectionList);
	}

	public Server getNeighborNodeNum(int nodeNum) {
		for (Server server : Collections.synchronizedList(connectionList)) {
			if(server.getNodeConnectedTo() == nodeNum)
				return server;
		}
		return null;
	}

	public String getNetworksNodeFileName() {
		return networksNodeFileName;
	}

	public void setNetworksNodeFileName(String networksNodeFileName) {
		this.networksNodeFileName = networksNodeFileName;
	}

	public void removeNeighbor(int senderID) {
		synchronized (getNeighbors()) {
			int mark = -1;
			for (int i = getNeighbors().size()-1; i >= 0; i--) {
				if(getNeighbors().get(i).getNodeConnectedTo() == senderID){
					mark = i;
					break;
				}
			}
			if(mark!=-1){
				getNeighbors().remove(mark);
			}
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
