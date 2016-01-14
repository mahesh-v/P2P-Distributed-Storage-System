package sockets;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import data.MyData;


public class AcceptingServer extends Thread {
	private int standardPort;
	ServerSocket listenerSocket = null;
	
	public AcceptingServer(int standardPort)
	{
		this.standardPort = standardPort;
	}
	
	public void run(){
		listen();
	}

	public void listen()
	{
		Socket socket = null;
		Server server = null;
		try {
			listenerSocket = new ServerSocket(standardPort);
			System.out.println("Accepting at port: "+standardPort);
			while (!MyData.getMyData().isShutDown()){
				socket = listenerSocket.accept();
				if(socket == null)
					continue;
				server = new Server(socket);
				server.start();
			}
		} catch(SocketException se){
			System.out.println("\nClosing socket at port "+standardPort);
		} catch (IOException e) {
			System.err.println("\nIOException when opening ServerSocket at port "+standardPort+": "+e.getMessage());
		} finally {
			try {
				this.close();
				if(socket != null)
					socket.close();
			} catch (IOException e) {}
		}
	}

	public void close() {
		try {
			if(listenerSocket!= null)
				listenerSocket.close();
		} catch (IOException e) {}
	}
	
}
