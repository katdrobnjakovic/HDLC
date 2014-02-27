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


	public static void main(String args[])
	{
		
		// sockets and other variables declaration
		// maximum number of clients connected: 10                    ASSUMPTION

		ServerSocket serverSocket = null;
		Socket[] clientSockets;
		clientSockets = new Socket[10];
		PrintWriter[] printWriter;
		printWriter = new PrintWriter[10];
		BufferedReader[] bufferedReader;
		bufferedReader = new BufferedReader[10];

		int[] ns; // send sequence number
		ns = new int[10];

		int[] nr; // receive sequence number
		nr = new int[10];

		String inputLine = null;
		String outputLine = null;

		//get port number from the command line
		int nPort = 4444; // default port number        

		String flag = "01111110";
		String[] address;
		address = new String[10];
		int[] clientID;
		clientID = new int[10];

		String control = null;
		String information = "";

		boolean bListening = true;

		String[] sMessages; // frame buffer
		sMessages = new String[20];
		int nMsg = 0;        

		
		boolean bAlive = false;

		String response = null; // control field of the input
		
		// initialize some var's to handle the array of clients
		int clientCount = 0;
		int i = 0;       

		// create server socket
		try {
			serverSocket = new ServerSocket(nPort);

		// this variable defines how many clients are connected
		int nClient = 0;

		// set timeout on the socket so the program does not hang up
			serverSocket.setSoTimeout(1000);

			// main server loop
			while (bListening){

				try {  
					
					// trying to listen to the socket to accept clients
					// if there is nobody to connect, exception will be thrown - set by setSoTimeout()
					clientSockets[clientCount]=serverSocket.accept();

					// connection got accepted
					if (clientSockets[clientCount]!=null){

						System.out.println("Connection from " + clientSockets[clientCount].getInetAddress() + " accepted.");

						System.out.println("accepted client");
						printWriter[clientCount] = new PrintWriter(clientSockets[clientCount].getOutputStream(),true);
						bufferedReader[clientCount] = new BufferedReader(new InputStreamReader(clientSockets[clientCount].getInputStream()));

						clientID[clientCount] = clientCount+1;		

						address[clientCount] = "00000000"+Integer.toBinaryString(clientID[clientCount]);
						int len = address[clientCount].length();					
						address[clientCount] = address[clientCount].substring(len-8);				

						System.out.println("client address: " + address[clientCount]);

						// send client address to the new client
						printWriter[clientCount].println(address[clientCount]);


						// ===========================================================
						// insert codes here to send SNRM message
						//        			        			
						System.out.println("Sent SNRM to station " + clientID[clientCount]);            		
						// ===============================================================

						// Receive UA message
						inputLine = bufferedReader[clientCount].readLine();
						response = inputLine.substring(16, 24);

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

				}

				for (i=0;i<clientCount;i++) {

					// ==============================================================
					// insert codes here to send “RR,*,P” msg     		

					System.out.println("Sent < RR,*,P > to station " + clientID[i]);
					// ==============================================================


					// recv response from the client
					inputLine = bufferedReader[i].readLine();

					if(inputLine != null) {		

						// get control field of the response frame
						response = inputLine.substring(16, 24);

						if(response.substring(0,4).equals("1000")) {
							// recv “RR,*,F”, no data to send from B
							System.out.println("Receive RR, *, F from station " + clientID[i]);
						}
						else if(response.substring(0, 1).equals("0")) {
							// ==============================================================
							// insert codes here to handle the frame “I, *, *” received


							//if the frame is to the primary station; consume it        					

							//if the frame is to the secondary station; buffer the frame to send


							// ==============================================================
						}
					}
				}

				// ==============================================================
				// insert codes here to send frames in the buffer       	

				// send I frame

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

}