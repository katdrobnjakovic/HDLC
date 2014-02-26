import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class SecondaryStation 
{
	public static void main(String args[])
	{
		//Declaration of variables for socket programming
		Socket client = null;
		DataInputStream input = null;
		DataOutputStream output = null;

		try {
			//connect to server local host on port 4444
			client = new Socket("localhost", 4444); 

			//input and output streams for the socket
			input = new DataInputStream(client.getInputStream());
			output = new DataOutputStream(client.getOutputStream());

			while(true)
			{
				//while the socket is open, read from the console
				BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
				String message = fromConsole.readLine();

				//send the message from the console to the server
				PhysicalLayerServer.handleMessageFromSecondaryStation(message);


				//cleanup - close the input and output streams and close the client connection
				input.close();
				output.close();
				client.close();
			}
		} catch (IOException e) {
			//prints stack trace if the above code throws an input output exception
			e.printStackTrace();
		}
	}
	
	/**
	 * This method handles the message from the secondary stations. 
	 * @param msg, the message received from the secondary station.
	 */
	public void handleMessageFromSecondayStation(String msg)
	{
		
	}
	
	/**
	 * This method handles the message from the physical layer server. 
	 * @param msg, the message received from the physical layer server. 
	 */
	public void handleMessageFromPhysicalLayerServer(String msg)
	{
		
	}
}