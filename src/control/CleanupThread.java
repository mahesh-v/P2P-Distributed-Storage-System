package control;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sockets.Server;
import data.MyData;
import data.SearchData;
import data.SearchTracker;

public class CleanupThread extends Thread {
	
	public void run(){
		Set<String> keysToDelete = new HashSet<String>();
		while(!MyData.getMyData().isShutDown()){
			ConcurrentHashMap<String, SearchTracker> searchTrackerMap = SearchData.getSearchData().getSearchTrackerMap();
			for (String key : searchTrackerMap.keySet()) {
				SearchTracker sTracker = searchTrackerMap.get(key);
				if(sTracker == null)
					System.out.println("This is weird. SearchTracker was null for "+key);
				if(sTracker.getSMessage().isExpired()&&sTracker.getNodeToSendMessageTo()!= MyData.getMyData().getMyNodeNum()){
					Server nodeToSendTo = MyData.getMyData().getNeighborNodeNum(sTracker.getNodeToSendMessageTo());
					if(nodeToSendTo!= null)
						nodeToSendTo.sendObject(sTracker.getSMessage());
					System.out.print("Dropping request "+key+" (nodes are not to maintain state for too long)\n> ");
					keysToDelete.add(key);
				}
			}
			for (String key : keysToDelete) {
				searchTrackerMap.remove(key);
			}
			keysToDelete.clear();
		}
	}
}
