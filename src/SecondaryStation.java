import java.net.*;
import java.util.LinkedList;
import java.io.*;

public class SecondaryStation {

	//CLASS VARIABLES:
	private static Socket clientSocket = null;
	private static DataOutputStream os = null;
	private static DataInputStream is = null;

	private static String flag = "01111110";
	private static int windowSize = 8;



	public static void main(String[] args) {



		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String id = null;
		String response = null; // control field of the socket input
		int nr = 0; //receive sequence number

		String answer = null; // input using keyboard

		try {
			clientSocket = new Socket("localhost", 2732);
			os =  new DataOutputStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		} 
		catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} 
		catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: hostname");
		}

		if (clientSocket != null && os != null && is != null) {			

			try {				

				String responseLine;				
				responseLine = is.readUTF();

				//receive client address from the primary station
				id = responseLine;
				System.out.println("client address: " + id);

				responseLine = is.readUTF();
				response = responseLine.substring(16, 24);

				// recv SNRM msg
				if(response.equals("11000001") || response.equals("11001001")) {

					//===========================================================
					//Send the UA msg		
					System.out.println("SNRM received");
					String controlUA = "11000110";
					String msg = flag + "00000000" + controlUA + "" + flag; // return message is start + primaryAddress + UA control code + (Info = 0) + end
					os.writeUTF(msg);
					//===========================================================
					System.out.println("sent UA msg");
				}

				//Receive and send data msgs
				while (true) {
					responseLine = is.readUTF();
					response = responseLine.substring(16, 24);

					System.out.println("recv msg -- control " + response);				

					//Received RR:
					if(response.substring(0,5).equals("10001")) {

						//Enter data msg using keyboard 
						System.out.println("Is there any message to send? (y/n)");
						answer = in.readLine();						

						if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")) 
						{
							System.out.println("Please enter the destination address using 8-bits binary string (e.g. 00000001):");
							String recipient = in.readLine();

							System.out.println("Please enter the message to send?");
							answer = in.readLine();

							//===========================================================
							//Insert code here to send an I msg;
							sendMessage(recipient, answer);
							//===========================================================

						}				
						else 
						{
							//===========================================================
							//Insert code here to send RR
							System.out.println("Sending RR.");
							String controlRR = "10000";
							String msg = flag + "00000000" + controlRR + "000" + flag;//TODO ACK
							os.writeUTF(msg);

							//===========================================================
						}
					}

					//Receive an I frame
					if(response.substring(0,1).equals("0")) {
						String data = responseLine.substring(24, responseLine.length()-8);
						System.out.println("");
						System.out.println("Received data: " + data);						

						nr = Integer.parseInt(response.substring(1,4), 2) + 1;
						System.out.println("nr: " + nr);
					}
				}
			} 
			catch (UnknownHostException e) {
				System.err.println("Trying to connect to unknown host: " + e);
			} 
			catch (IOException e) {
				System.err.println("IOException: " + e);
			}	
		}
	}

	private static void sendMessage(String recipient, String answer) {
		boolean allReceived = false;
		String sendControl = "00000000";
		try {
			//Initialize the sliding window:
			LinkedList<String> window = new LinkedList<String>();
			LinkedList<Integer> windowFrameNums = new LinkedList<Integer>(); //Keep track for the ns and nr values
			LinkedList<Integer> acksReceived = new LinkedList<Integer>();// 1 = ack was received

			int middleWall = 0; //Index to the message that is to be sent next in the window
			int nextAddedToWindowIndex = 0; //Index to the last added string to the window
			String tempAnswer = new String(answer);

			//Initialize messageChunks:
			int numChunks = (int) Math.ceil( ((double) tempAnswer.length()) / 64.0 );
			String [] messageChunks = new String [numChunks];

			//Store groups of 64 chars in messageChunks
			for(int i = 0; i < numChunks; i++)
			{
				if(tempAnswer.length() > 64)
				{
					messageChunks[i] = tempAnswer.substring( i * 64, (i * 64) + 64 );
					tempAnswer = tempAnswer.substring( (i * 64) + 64, tempAnswer.length());

				}
				else //remaining group of less than 64 bytes/characters
				{
					messageChunks[i] = new String(tempAnswer);
				}
			}

			//Fill the window initially:
			for(int i = 0; i < numChunks && i < windowSize; i++)
			{
				windowFrameNums.addLast( nextAddedToWindowIndex % 8);
				window.addLast( messageChunks[ nextAddedToWindowIndex++ ] );
				acksReceived.addLast(0);
			}

			//Send the message until all are ACK'd:
			while(!allReceived)//each string in messageChunks
			{
				//Are there still unsent messages in the window? Send one:
				if( ! (window.size() == 0) && ! (middleWall == window.size()) )//There are messages to be sent AND not all in window have been sent
				{
					String ns =  Integer.toBinaryString( windowFrameNums.get(middleWall) );//get num of frame sending
					String nr = Integer.toBinaryString( windowFrameNums.getFirst());//get the left wall

					//Pad the ns with more 0s to get 3 in total
					while( ns.length() < 3 )
					{
						ns = "0" + ns;
					}

					//Pad the nr with more 0s to get 3 in total
					while( nr.length() < 3 )
					{
						nr = "0" + nr;
					}

					sendControl = "0"+ns+"0"+nr;
					String msg = flag  + recipient + sendControl + window.get(middleWall++) + flag;
					os.writeUTF(msg);
				}

				//Are any ACKs or SREJs received?
				clientSocket.setSoTimeout(1000);
				String incomingMsg ="";
				try{
					incomingMsg = is.readUTF();
				} catch (Exception e)
				{
					//No incoming ACK/SREJ
				}
				clientSocket.setSoTimeout(0);
				if(! incomingMsg.equals(""))
				{
					String incomingControl = incomingMsg.substring(16, 24);

					// recv ACK msg
					if(incomingControl.startsWith("1000")) //RR = ACK
					{					
						int nr = Integer.parseInt(incomingControl.substring(5, 8), 2);
						int nrIndex = windowFrameNums.indexOf(nr);
						acksReceived.add(nrIndex, 1);

						//Can the left wall be moved?
						for(int i2 = 0; i2 < acksReceived.size(); i2++)//Every frame currently in the window
						{
							if(acksReceived.getFirst() == 1)//
							{
								//shift ACKs array:
								acksReceived.removeFirst();
								acksReceived.addLast(0);
								//shift window:
								window.removeFirst();
								windowFrameNums.removeFirst();
								if(nextAddedToWindowIndex < messageChunks.length)//There are more messages to be sent
								{
									windowFrameNums.addLast(nextAddedToWindowIndex % 8);
									window.addLast(messageChunks[nextAddedToWindowIndex++]);									
								}
								//Update middle:
								if(window.size() != 0)//There are still messages in the window, not all have been sent and recceived
								{
									middleWall--;
								}
								else
								{
									allReceived = true; 
									break;
								}
							}
							else //as soon as there is a frame not ACK'd stop moving the window over.
							{
								break;
							}
						}
					}

					if(incomingControl.startsWith("1011")) //SREJ
					{
						int nr = Integer.parseInt(incomingControl.substring(5, 8), 2);
						int i = windowFrameNums.indexOf(nr);
						String msg = flag  + recipient + sendControl + window.get(i) + flag;
						os.writeUTF(msg);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}// end of class SecondaryStation
