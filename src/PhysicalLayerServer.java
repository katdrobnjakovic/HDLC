import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class PhysicalLayerServer 
{
	static int clientCount = 0;
	static int[] clientID = new int[10];
	static DataInputStream[] inputStream = new DataInputStream[10];
	static DataOutputStream[] outputStream = new DataOutputStream [10]; 
	static String inputLine = null;
	static String[] address = new String[10];
	static String response = null; // control field of the input
	static String control = null;
	static PrintWriter[] printWriter = new PrintWriter[10];
	
	public static void main(String args[])
	{

		// sockets and other variables declaration
		// maximum number of clients connected: 10                    ASSUMPTION

		ServerSocket serverSocket = null;
		Socket[] clientSockets;
		clientSockets = new Socket[10];
	
	


		int[] ns; // send sequence number
		ns = new int[10];

		int[] nr; // receive sequence number
		nr = new int[10];

	
		String outputLine = null;

		//get port number from the command line
		int nPort = 2732; // default port number        

		String flag = "01111110";

	

		
		String information = "";

		boolean bListening = true;

		String[] sMessages; // frame buffer
		sMessages = new String[20];
		int nMsg = 0;        


		boolean bAlive = false;


		// initialize some var's to handle the array of clients
	
		int i = 0;       

		// create server socket
		try {
			
			serverSocket = new ServerSocket(nPort);
			serverSocket.setSoTimeout(10000);
			// this variable defines how many clients are connected
			int nClient = 0;

			// set timeout on the socket so the program does not hang up
			//serverSocket.setSoTimeout(1000);

			// main server loop
			while (bListening){

				try {  

					// trying to listen to the socket to accept clients
					// if there is nobody to connect, exception will be thrown - set by setSoTimeout()
					clientSockets[clientCount]=serverSocket.accept();
					System.out.println("Found Client");
					// connection got accepted
					if (clientSockets[clientCount]!=null){

						System.out.println("Connection from " + clientSockets[clientCount].getInetAddress() + " accepted.");

						System.out.println("accepted client");
						//printWriter[clientCount] = new PrintWriter(clientSockets[clientCount].getOutputStream());
						outputStream[clientCount] = new DataOutputStream(clientSockets[clientCount].getOutputStream());
						inputStream = new DataInputStream [10];
						inputStream[clientCount] = new DataInputStream(clientSockets[clientCount].getInputStream());

						clientID[clientCount] = clientCount+1;		

						address[clientCount] = "00000000"+Integer.toBinaryString(clientID[clientCount]);
						int len = address[clientCount].length();					
						address[clientCount] = address[clientCount].substring(len-8);				

						System.out.println("client address: " + address[clientCount]);

						// send client address to the new client
						outputStream[clientCount].writeUTF(address[clientCount]);
						//printWriter[clientCount].println(address[clientCount]);


						// ===========================================================
						// insert codes here to send SNRM message
						//   
						outputStream[clientCount].writeUTF(flag + "00000000" + "11000001"+flag);
			
						System.out.println("Sent SNRM to station " + clientID[clientCount]);  
					
						// ===============================================================

						// Receive UA message
						//receiveUA();
						inputLine = inputStream[clientCount].readUTF(); 
						String response = inputLine.substring(16, 24);

						if(response.equals("11000110") || response.equals("11001110")) {
							System.out.println("Received UA from station " + clientID[clientCount]);
						}
						else {
							System.out.println("UA error -- station " + clientID[clientCount]);
						}  

						// initialize ns and nr
						ns[clientCount] = -1;
						nr[clientCount] = 0;

						// increment count of clients
						clientCount++;
						nClient = clientCount;
						bAlive = true;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				for (i=0;i<clientCount;i++) {

					// ==============================================================
					// insert codes here to send RR,*,P msg
					System.out.println("Sent < RR,*,P > to station" + clientID[i]);
					outputStream[i].writeUTF(flag + "00000001" + "10001" + "000" + flag);
					// ==============================================================
					
					// recv response from the client
					inputLine = inputStream[i].readUTF();

					if(inputLine != null) {		

						// get control field of the response frame
						response = inputLine.substring(16, 24);

						if(response.substring(0,4).equals("1000")) {
							// recv RR,*,F, no data to send from B
							System.out.println("Receive RR, *, F from station " + clientID[i]);
						}
						else if(response.substring(0, 1).equals("0")) {
							// ==============================================================
							handleFrame();// insert codes here to handle the frame I, *, * received


							//Check destination: 					
							if(checkIfToPrimary(inputLine))//if the frame is to the primary station; consume it 
							{
								//TODO: PROCESS THE FRAME
								//prints the information part of the frame
								System.out.println(inputLine.substring(24, inputLine.length()-24).toString()); 
							}
							else{//if the frame is to the secondary station; buffer the frame to send
								if(address[clientCount].equals(inputLine.substring(8, 15)))
								{
									outputStream[clientCount].writeUTF(inputLine);
								}
								
							}


							// ==============================================================
						}
					}
				}

				// ==============================================================
				// insert codes here to send frames in the buffer   
				outputStream[clientCount].writeUTF(inputLine);

				// send I frame
				if(control.substring( 0, 1).equals("0"))
				{
					outputStream[clientCount].writeUTF(inputLine);
				}

				// ==============================================================

				//
				// stop server automatically when all clients disconnected:
				if (!bAlive && clientCount > 0){// no active clients
					System.out.println("All clients are disconnected - stopping");
					bListening = false;
				}

			}// end of while loop
		}   catch (Exception e1) {
			e1.printStackTrace();
		}

	}
	
	private static void receiveUA() {
		String inputLine = null;
		try {
			inputLine = inputStream[clientCount].readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String response = inputLine.substring(16, 24);

		if(response.equals("11000110") || response.equals("11001110")) {
			System.out.println("Received UA from station " + clientID[clientCount]);
		}
		else {
			System.out.println("UA error -- station " + clientID[clientCount]);
		}  
	}
	
	//handles I frames
	private static void handleFrame() {
		int nr = 0; 
		String data = inputLine.substring(24, inputLine.length()-8);
		System.out.println("");
		System.out.println("Received data: " + data);						

		nr = Integer.parseInt(control.substring(1,4), 2) + 1;
		System.out.println("nr: " + nr);

	}

	private static boolean checkIfToPrimary(String inputLine) {
		if(inputLine.substring(8, 15).equalsIgnoreCase("00000000"))
		{
			return true;
		}
		return false;
	}


}