package sockets;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

import data.Message;
import data.MyData;


public class Server extends Thread {
	private Socket socket;
	private ObjectOutputStream ooStream;
	private ObjectInputStream oiStream;
	private int nodeConnectedTo;
	private boolean disconnectAfterFileTransfer;
	private boolean closed;
	
	public Server(Socket socket){
		this.socket = socket;
		closed=false;
	}
	
	public void run(){
		readSocketData();
	}
	
	public void sendFile(String fileName){
		try {
			Message m = new Message();
			m.setTimeStamp(LocalDateTime.now());
			m.setSenderNodeID(MyData.getMyData().getMyNodeNum());
			
			File transferFile = new File (fileName); 
			byte [] bytearray = new byte [(int)transferFile.length()];
			FileInputStream fin = new FileInputStream(transferFile); 
			BufferedInputStream bin = new BufferedInputStream(fin); 
			bin.read(bytearray,0,bytearray.length); 
			bin.close();
			
			m.setMessage(bytearray);
			
			if(ooStream != null){
				ooStream.writeObject(m);
				ooStream.flush();
				if(disconnectAfterFileTransfer)
					close();
			}
			else {
				System.out.println("Did not send: "+fileName+" to "+getNodeConnectedTo());
			}
		} catch (IOException e) {
			System.err.println("IOException while sending file("+fileName+") to "+getNodeConnectedTo()+": "+e.getMessage());
		}
	}
	
	public void sendObject(Object object)
	{
		try {
			Message m = new Message();
			m.setTimeStamp(LocalDateTime.now());
			m.setSenderNodeID(MyData.getMyData().getMyNodeNum());
			m.setMessage(object);
			if(ooStream != null){
				ooStream.writeObject(m);
				ooStream.flush();
			}
			else {
				System.out.println("Did not send: "+object+" to "+getNodeConnectedTo());
			}
		} catch (IOException e) {
			System.err.println("IOException while sending object("+object+") to "+getNodeConnectedTo()+": "+e.getMessage());
		}
	}

	protected void readSocketData() {
		try {
			if(ooStream == null)
				ooStream = new ObjectOutputStream(socket.getOutputStream());
			if(oiStream == null)
				oiStream = new ObjectInputStream(socket.getInputStream());
			while(!MyData.getMyData().isShutDown())
			{
				try {
					Object object = oiStream.readObject();
					if(object!= null && object instanceof Message)
					{
						Message m =(Message) object;
						Object inObject = m.getMessage();
						if(inObject instanceof String && ((String) inObject).contains("CONNECT_ME"))
							processConnectRequest((String)inObject);
						else
							MyData.getMyData().getMessageQueue().put(m);
					}
				} catch (ClassNotFoundException e) {
					System.err.println("ClassNotFoundException when reading object from stream: "+e.getMessage());
				} catch (InterruptedException e) {
					if(!MyData.getMyData().isShutDown())
						System.err.println("ServerThread connected to node"+nodeConnectedTo+" interrupted: "+e.getMessage());
				}				
			}
		} catch (IOException e) {
			System.err.println("Connection closed with "+nodeConnectedTo);
		} 
		finally {
			close();
		}
	}

	public void close() {
		try {
			if(ooStream != null)
				ooStream.close();
			if(oiStream != null)
				oiStream.close();
			if(socket!= null)
				socket.close();
			if(!disconnectAfterFileTransfer&&!closed)
				System.out.print("Disconnected from node"+nodeConnectedTo+"\n> ");
			closed=true;
			MyData.getMyData().removeNeighbor(nodeConnectedTo);
		} catch (IOException e) {}
	}
	
	private void processConnectRequest(String data) {
		if(data.contains("CONNECT_ME"))
		{
			String[] fields = data.split("\t");
			if(fields.length != 2)
				System.err.println("Abnormal connect message received: "+data);
			int nodeNum = Integer.parseInt(fields[1]);
			this.setNodeConnectedTo(nodeNum);
			this.setName("ThreadTo"+nodeNum);
			MyData.getMyData().getNeighbors().add(this);
			System.out.print("Connection established with node"+nodeNum+"\n> ");
		}
	}

	public void setNodeConnectedTo(int nodeNum){
		this.nodeConnectedTo = nodeNum;
	}
	
	public int getNodeConnectedTo(){
		return this.nodeConnectedTo;
	}
	
	public String getNodeAddress(){
		return this.socket.getInetAddress().getHostName();
	}

	public boolean isDisconnectAfterFileTransfer() {
		return disconnectAfterFileTransfer;
	}

	public void setDisconnectAfterFileTransfer(boolean disconnectAfterFileTransfer) {
		this.disconnectAfterFileTransfer = disconnectAfterFileTransfer;
	}
}
