import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.io.*;
public class Broker {
	
	private static final int BUF_SIZE = 255;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		/*
		 * Setting up the UPD broker
		 */
		if (args.length != 1) {
			throw new IllegalArgumentException("Parameter(s): <Port>");
		}
		int servPort = Integer.parseInt(args[0]);
		
		byte[] rbuf = new byte[BUF_SIZE];
		byte[] sbuf = new byte[BUF_SIZE];
		DatagramPacket sendPacket;
		DatagramPacket recvPacket = new DatagramPacket(rbuf, rbuf.length);
		
		DatagramSocket serverSocket = new DatagramSocket(servPort);
		
		HashMap<String,String> addressBook = new HashMap<String,String>(); // Keep track of peers' most recent address
		HashMap<String,PriorityQueue<FileInfo>> fileRecord = new HashMap<String,PriorityQueue<FileInfo>>(); // Keep track peers who have a certain file
		System.out.println("Waiting for clients");
		
		while (true) {
			System.out.println(fileRecord.toString());
			serverSocket.receive(recvPacket);
			ByteArrayInputStream byteStream = new ByteArrayInputStream(rbuf);
		    ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
		    String toRecv = (String)is.readObject();
		    String[] RecvWords = toRecv.split(" ",4);
		    String toSend;
		    if (RecvWords.length != 4) {
		    		System.out.println(RecvWords.toString());
		    		toSend = "Illegal input. Try again!";
		    }
		    else {
		    		String name = RecvWords[0];
		    		int port = Integer.parseInt(RecvWords[1]);
		    		String command = RecvWords[2].toLowerCase();
		    		String filename = RecvWords[3];
		    		addressBook.put(name, recvPacket.getAddress().getHostAddress() + " " + port); // always update peers' most recent address and port
		    		switch (command) {
		    		case ("register"):
		    			toSend = "Successfully registered file " + filename + " at IP: " + recvPacket.getAddress().getHostAddress() + " and Port#: " + port;
		    			if (fileRecord.containsKey(filename)) {
		    				boolean registered = false;
		    				PriorityQueue myQueue = fileRecord.get(filename);
		    				Iterator<FileInfo> iterator = myQueue.iterator();
		    				while (iterator.hasNext()) {
		    					FileInfo fi = iterator.next();
		    					if (fi.Name.equals(name)) {
		    						registered = true;
		    						toSend = "You've already registered with file " + filename;
		    						break;
		    					}
		    				}
		    				if (!registered)
		    					fileRecord.get(filename).add(new FileInfo(name, filename));
		    			}
		    			else {
		    				PriorityQueue<FileInfo> fileList = new PriorityQueue<FileInfo>();
		    				fileList.add(new FileInfo(name, filename));
		    				fileRecord.put(filename, fileList);
		    			}
		    			break;
		    		case ("unregister"):
		    			if (!fileRecord.containsKey(filename)) {
		    				toSend = "You weren't registered with file " + filename;
		    			}
		    			else {
		    				PriorityQueue myQueue = fileRecord.get(filename);
		    				Iterator<FileInfo> iterator = myQueue.iterator();
		    				toSend = "You weren't registered with file " + filename;
		    				while (iterator.hasNext()) {
		    					FileInfo fi = iterator.next();
		    					if (fi.Name.equals(name) && fi.fileName.equals(filename)) {
		    						myQueue.remove(fi);
		    						toSend = "Successfully unregistered file " + filename + " at IP: " + recvPacket.getAddress().getHostAddress() + " and Port#: " + port;
		    						if (myQueue.size() == 0) fileRecord.remove(filename);
		    						break;
		    					}
		    				}	
		    			}
		    			break;
		    		case ("request"):
		    			if (!fileRecord.containsKey(filename)) {
		    				toSend = "No one registered with file " + filename;
		    			}
		    			else {
		    				FileInfo best = fileRecord.get(filename).peek();
		    				String[] addressInfo = addressBook.get(best.Name).split(" ");
		    				toSend = addressInfo[0] + " " + addressInfo[1] + " " + best.toString();
		    			}
		    			break;
		    		case ("rate"):
		    			String[] ratingInfo = filename.split(" ",4);
		    			String peerName = ratingInfo[1];
		    			int rate = Integer.parseInt(ratingInfo[2]);
		    			String rateFile = ratingInfo[3];
		    			if (fileRecord.containsKey(rateFile)) {
		    				PriorityQueue myQueue = fileRecord.get(rateFile);
		    				Iterator<FileInfo> iterator = myQueue.iterator();
		    				while (iterator.hasNext()) {
		    					FileInfo fi = iterator.next();
		    					if (fi.Name.equals(peerName)) {
		    						fi.rating = (fi.rating * fi.ratingNums + rate) / (++fi.ratingNums);
		    						break;
		    					}
		    				}	
		    			}
		    		default:
		    			toSend = "Illegal input. Try again!";
		    		}
		    		if (command.equals("rate")) continue;
		    }
		    sbuf = toSend.getBytes();
		    sendPacket = new DatagramPacket(sbuf, sbuf.length, recvPacket.getAddress(), recvPacket.getPort());
		    serverSocket.send(sendPacket);
		}
	}

}
