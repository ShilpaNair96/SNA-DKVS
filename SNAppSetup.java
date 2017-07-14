import java.io.*;
import java.net.*;
public class SNAppSetup
{
	public static String columns[] = {"Age","Country","Gender","Name","Username"};
	public static int colVal[] = {1,2,3,4,5};
	public static void main(String[] args) throws Exception
	{
		String request, response;
		Socket sock = new Socket("127.0.0.1", 3000);
		PrintWriter pwrite = new PrintWriter(sock.getOutputStream(), true);
		BufferedReader serverResp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		try
		{
			for(int i=0; i<columns.length; i++)	//Columns are stored so that they may be validated easily
			{
				pwrite.println("put "+columns[i]+","+colVal[i]);
				pwrite.flush();
				serverResp.readLine();
			}
			pwrite.println("quit");
			pwrite.flush();
		}
		catch(ConnectException e)
		{
			System.err.println(e);
		}
		pwrite.close();
		serverResp.close();
		sock.close();
	}
}
