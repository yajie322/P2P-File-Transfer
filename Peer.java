import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Peer {
	private static final int BUF_SIZE = 255;
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length != 2) {
		      throw new IllegalArgumentException("Parameter(s): <Server> <Port>");
		}
		InetAddress serverAddress = InetAddress.getByName(args[0]); // Server Address
		int servPort = Integer.parseInt(args[1]);
		
		byte[] sbuf = new byte[BUF_SIZE];
		byte[] rbuf = new byte[BUF_SIZE];
		String toSend;
		String toRecv;
		String peerInput;
		
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramPacket sendPacket;
		DatagramPacket recvPacket;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); // Read from user
		
		System.out.println("Please enter your name: ");
		String name = inFromUser.readLine();
		System.out.println("Please enter a port number you wish to use for other peer to contact you: ");
		boolean inputIsInt = false;
		int myServerPort = 10001;
		while (!inputIsInt) {
			try {
				myServerPort = Integer.parseInt(inFromUser.readLine());
				inputIsInt = true;
			} catch (NumberFormatException e) {
				System.out.println("Please enter a valid port number: ");
			}
		}
		PeerThread peerThread = new PeerThread(myServerPort);
		Thread T = new Thread(peerThread);
		T.setDaemon(true);
		T.start();
		
		while (true) {
			System.out.println("What do you wish to do? Type 'register/unregister/request + file name' ");
			peerInput = inFromUser.readLine();
			if (peerInput.equalsIgnoreCase("bye")) break;
			String[] inputWords = peerInput.split(" ",2);
			String command = inputWords[0].toLowerCase();
			if (inputWords.length != 2 || !(command.equals("register") || command.equals("unregister") || command.equals("request"))) {
				System.out.println("Illegal input. Try again! " + inputWords.toString());
				continue;
			}
			if (command.equals("register")) {
				File f = new File(inputWords[1]);
				if (!f.isFile()) {
					System.out.println("You do not have file " + inputWords[1]);
					continue;
				}
			}
			toSend = name + " " + myServerPort + " " + peerInput;
			
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BUF_SIZE);
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(toSend);
			os.flush();
			sbuf = byteStream.toByteArray();
			sendPacket = new DatagramPacket(sbuf, sbuf.length, serverAddress, servPort);
			clientSocket.send(sendPacket);
			os.close();
			
			recvPacket = new DatagramPacket(rbuf,rbuf.length);
		    clientSocket.receive(recvPacket);
		    toRecv = new String(recvPacket.getData(),0,recvPacket.getLength());
		    System.out.println(toRecv);
		    
		    if (command.equals("request") && !toRecv.startsWith("No one registered with file")) { // then connect to the peer who has the file
		    		String[] RecvWords = toRecv.split(" ", 5);
		    		InetAddress peerAddress = InetAddress.getByName(RecvWords[0]);
		    		int peerPort = Integer.parseInt(RecvWords[1]);
		    		String filename = RecvWords[4];
		    		String peerName = RecvWords[2];
		    		Socket peerSock = new Socket(peerAddress, peerPort);
		    		OutputStream sendStream = peerSock.getOutputStream();
		    		ObjectOutputStream objectSendStream = new ObjectOutputStream(sendStream);
		    		objectSendStream.writeObject("request " + filename);
		    		InputStream recvStream = peerSock.getInputStream();
		    		ObjectInputStream objectRecvStream = new ObjectInputStream(recvStream);
		    		String response = (String)objectRecvStream.readObject();
		    		if (response.equals("File doesn't exist here.")) {
		    			System.out.println(response);
		    		}
		    		else {
		    			Scanner scanner = new Scanner(response);
		    			try {
		    				BufferedWriter bw = new BufferedWriter(new FileWriter("copy of " + filename));
		    				while (scanner.hasNext()) {
		    					bw.write(scanner.nextLine());
		    					bw.newLine();
		    				}
		    				bw.flush();
		    				bw.close();
		    				scanner.close();
		    			} catch (Exception e) {}
		    		}
		    		System.out.println("File is received. Please rate this file by entering an integer from 0 - 10: ");
		    		toSend = name + " " + myServerPort + " " + "rate " + " " + peerName + " " + askForRating(inFromUser) + " " + filename;
		    		byteStream = new ByteArrayOutputStream(BUF_SIZE);
				os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
				os.flush();
				os.writeObject(toSend);
				os.flush();
				sbuf = byteStream.toByteArray();
				sendPacket = new DatagramPacket(sbuf, sbuf.length, serverAddress, servPort);
				clientSocket.send(sendPacket);
				os.close();
		    }
			
		}
	}
	
	public static String askForRating(BufferedReader inFromUser) throws IOException {
		String peerInput = inFromUser.readLine();
		try {
			int temp = Integer.parseInt(peerInput);
			if (temp > 10 || temp < 0) {
				return askForRating(inFromUser);
			}
		} catch (NumberFormatException e) {
			System.out.println("Wrong input. Please rate this file by entering an integer from 0 - 10: ");
			return askForRating(inFromUser);
		}
		return peerInput;
	}

}
