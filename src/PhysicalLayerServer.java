import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class PhysicalLayerServer 
{
	
	public static void main(String args[])
	{
		ServerSocket server = null; 
		Socket clientSocket = null; 
		DataInputStream input = null;
		DataOutputStream output = null;
		
		try 
		{
			server = new ServerSocket(4444);
			System.out.println("Server running on port 4444");

			//accepts the socket connection
			clientSocket = server.accept();
			
			//while the socket is open allow an input and output stream
			while (true) {
				input = new DataInputStream(clientSocket.getInputStream());
				output = new DataOutputStream(clientSocket.getOutputStream());
			}
			
		} catch (IOException e)
		{
			// print stack trace if the above try throws an input output exception
			e.printStackTrace();
		}
		
		try
		{
			//cleanup - close the input and output streams and the server connection
			input.close();
			output.close();
			clientSocket.close();
			server.close();
		} catch (IOException e) {
			//print stack trace if the above try throws an input output exception
			e.printStackTrace();
		}
	}
	
	/**
	 * This method handles message from the secondary stations. 
	 * @param msg, the message received from the secondary station. 
	 */
	public static void handleMessageFromSecondaryStation(String msg) 
	{
		
	}
	
}