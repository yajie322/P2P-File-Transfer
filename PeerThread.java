import java.net.*;
import java.util.Scanner;
import java.io.*;

public class PeerThread implements Runnable {
	private int servPort;
	
	public PeerThread(int port) {
		this.servPort = port;
	}
	
	public void run() {
		try {
			ServerSocket servSocket = new ServerSocket(servPort);
			
			while (true) {
				Socket clntSocket = servSocket.accept();
				InputStream is = clntSocket.getInputStream();  
			    ObjectInputStream ois = new ObjectInputStream(is);  
				String toRecv = (String)ois.readObject();
				String toSend;
				if (toRecv.startsWith("request")) {
					toSend = getContents(toRecv.substring(8));
					if (toSend == null) {
						toSend = "File doesn't exist here.";
					}
				}
				else {
					toSend = "Invalid input.";
				}
				OutputStream os = clntSocket.getOutputStream();  
			    ObjectOutputStream oos = new ObjectOutputStream(os);
			    oos.writeObject(toSend);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getContents(String fileName) {
		String line;
		StringBuffer sb = new StringBuffer();
		try {
			File f = new File(fileName);
			if (f.isFile()) {
				Scanner inFile = new Scanner(f);
				while (inFile.hasNext()) {
					line = inFile.nextLine();
					sb.append(line);
					sb.append("\n");
				}
				inFile.close();
				return sb.toString();
			}
		} catch (Exception e) {
		}
		return null;
	}
}
