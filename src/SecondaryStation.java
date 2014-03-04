import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

class SecondaryStation 
{
	private static String host = "localhost";
	private static int port = 4444;

	public static void main(String args[])
	{
		Socket clientSocket = null;
		//PrintStream printWriter = null;
		DataInputStream inputStream = null;
		DataOutputStream outputStream = null;

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		String id = null;
		String information = null;
		String control = null;
		String flag = "01111110";
		String address = null;  
		String response = null; // control field of the socket input
		int ns = -1; // send sequence number
		int nr = 0; //receive sequence number

		String answer = null; // input using keyboard

		//Initialization:
		//Try to open a socket on port 4444
		//Try to open input and output streams
		try {
			clientSocket = new Socket(host, port);
			inputStream = new DataInputStream(clientSocket.getInputStream());
			outputStream = new DataOutputStream(clientSocket.getOutputStream());
		} 
		catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} 
		catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: hostname");
		}

		if (clientSocket != null && outputStream != null && inputStream != null) {			

			try {				

				String responseLine;				
				responseLine = inputStream.readUTF();

				//receive client address from the primary station
				id = responseLine;
				System.out.println("client address: " + id);

				responseLine = inputStream.readUTF();
				//response = responseLine.substring(16, 24);//Control Section
				
				String msg = "";
				
				//receive SNRM Message TODO check whether SNRM codes are correct
				checkIfResetMsg(responseLine, outputStream, msg);// insert codes here to send the UA msg	


				// main loop; recv and send data msgs
				while (true) {
					responseLine = inputStream.readUTF();
					response = responseLine.substring(16, 24);

					System.out.println("recv msg -- control " + response);				

					// recv ??RR,*,P?? msg
					if(response.substring(0,5).equals("10001")) {

						// enter data msg using keyboard 
						System.out.println("Is there any message to send? (y/n)");
						answer = bufferedReader.readLine();						

						if(answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")) {
							System.out.println("Please enter the destination address using 8-bits binary string (e.g. 00000001):");
							address = bufferedReader.readLine();

							System.out.println("Please enter the message to send?");
							answer = bufferedReader.readLine();

							//===========================================================
							// insert codes here to send an I msg;
							sendIMsg();
							//===========================================================

						}				
						else {
							//===========================================================
							// insert codes here to send ??RR,*,F??

							//===========================================================
						}
					}

					// receive an I frame
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

	private static void sendIMsg() {
		// TODO Auto-generated method stub

	}

	//Checks whether the incoming message is reset code and requires 
	private static void checkIfResetMsg(String response, DataOutputStream outputStream, String message) {

		try {
			// recv SNRM msg
			if(response.equals("11000001") || response.equals("11001001")) //RESET codes
			{
				String msg ="";
				//===========================================================
				
				outputStream.writeUTF("");//TODO

				//===========================================================
				System.out.println("sent UA msg");

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//Checks whether the currently received frame is to be rejected or processed (introduces error) 
	//Returns true if the current frame should be accepted and processed, returns false if the current frame should be ignored.
	boolean accept()
	{
		
		
	return true;//change	
	}
	
	
}