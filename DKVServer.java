import java.io.*;                   
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
public class DKVServer
{
	private HashMap<String, String> memory = new HashMap<String, String>();
	public static int hashRef[] = new int[720];
	public static int no = 0;
	public int myNo = 0;
	private String electNodeNm = "";
	private int port = 0;
	private int noReq = 0;
	private boolean isMaster = false;
	private String fileName = "";
	private ServerSocket serverSocket;
	private Socket serSock;
	private PrintWriter pwrite;
	private BufferedReader receiveRead;

	public void terminate()throws IOException
	{
		saveData();
		System.out.println("No of valid requests processed by server no:"+myNo+":"+noReq);
		if(serSock!=null)
		{
			pwrite.close();
			receiveRead.close();
			serSock.close();
		}
		if(serverSocket!=null)
			serverSocket.close();
	}

	public void saveData()throws IOException		//Writes data back to the file
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for(String key : memory.keySet())
		{
			bw.write(key+"\t"+memory.get(key)+"\n");
		}
		bw.close();
	}

	private int hashVal(String key)
	{
		int hash=0;
		int n=key.length();
		for(int i=0; i<n; i++)
		{
			hash += (int) key.charAt(i);
		}
		hash = hash % 720;
		return hash;
	}

	public DKVServer()
	{
		no++;
		myNo = no;
	}
	public String getNdNm()
	{
		return electNodeNm;
	}

			//Server creates an ephemeral sequential znode for leader election
	public void contestForLeader(ZooKeeper zk)throws KeeperException, InterruptedException
	{
		electNodeNm = zk.create("/app/n_", Integer.toString(myNo).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("Server no:"+myNo+" Node:"+electNodeNm);
	}

			//Actions performed by master
	public boolean callMaster(ZooKeeper zk)throws KeeperException, InterruptedException, IOException
	{
		this.isMaster = true;
		System.out.println("Master declared : "+myNo);
		BufferedReader br = new BufferedReader(new FileReader("DKVconfig.txt"));
		String ar[] = new String[2];
		ar[1]="3000";			//Default port.
		String arr[];
		int K=1;			//Default number of servers.
		String line = br.readLine();
		while(line!=null)
		{
			arr = line.split("=");
			if(arr[0].equals("ports"))
				ar = arr;
			if(arr[0].equals("K"))
				K = Integer.parseInt(arr[1]);
			line = br.readLine();
		}

		String str[] = ar[1].split(",");
		
		if(str.length!=K)
		{
			System.out.println("Error in configuration file: Wrong number of ports provided.");
			return false;
		}

		int hash = 720/K;
		int range = hash;
		List<String> children = zk.getChildren("/app",false);
		for(int i=0; i<children.size(); i++)	//Leader sends details to all servers through their ephemeral znodes
		{
			int port = Integer.parseInt(str[i]);
			for(int j=range-hash; j<range; j++)		//Sets hash value data structure
				hashRef[j] = port;
			String s = str[i] + ",server"+(i+1)+".txt";
			String path = "/app/"+children.get(i);
			zk.setData(path, s.getBytes(), zk.exists(path,true).getVersion());	//Writes data into znodes of all servers
			range += hash;
		}
		return true;
	}

	public void serverSetup(ZooKeeper zk)throws KeeperException, InterruptedException, IOException
	{
		byte b[] = zk.getData(electNodeNm, new Watcher()
				{
					public void process(WatchedEvent we)
					{
						return;
					}
				}, null);
		String data[] = (new String(b, "UTF-8")).split(",");
		this.port = Integer.parseInt(data[0]);
		this.fileName = data[1];				//Retrieves required data from ephemeral znode
		System.out.println("Server no:"+myNo+" Port No:"+port);


									//Reads data from file
		BufferedReader br = new BufferedReader(new FileReader(this.fileName));
		String st = br.readLine();
		while(st!=null)
		{
			String pair[] = st.split("\t");
			memory.put(pair[0],pair[1]);
			st = br.readLine();
		}
	}

	private boolean validate(String request)
	{
		String parts[] = request.split(" ",2);
		if(parts.length!=2)
			return false;
		String command = parts[0];
		if(command.equalsIgnoreCase("get")||command.equalsIgnoreCase("del"))
		{
			String[] keyarr=parts[1].split(",");	//del <key>	OR	get <key>
			if(keyarr.length>1)
				return false;
			return true;
		}
		else if(command.equalsIgnoreCase("put"))
		{
			String[] keyval=parts[1].split(",");	//put <key>,<val>
			if(keyval.length!=2)
				return false;
			return true;
		}
		return false;
	}

	private String forwardReq(int port, String request)throws IOException
	{
		try			//Act as client to another server (to forward data)
		{
			Socket sock = new Socket("127.0.0.1", port);
			PrintWriter pwrite = new PrintWriter(sock.getOutputStream(), true);
			BufferedReader serverResp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println(myNo+":Forwarded Request\n");
			long startTime = System.nanoTime();
			pwrite.println(request);
			pwrite.flush();
			String ans =  serverResp.readLine();
			pwrite.println("quit");		//Session with other server must end normally
			pwrite.flush();
			System.out.println(myNo+":Received Forwarded Response");
			long endTime = System.nanoTime();
			serverResp.close();
			pwrite.close();
			sock.close();
			return ans;
		}
		catch(ConnectException e)
		{
			System.err.println(e);
			return "An error occured.";
		}
	}

	private String process(String request)throws IOException
	{
		String parts[] = request.split(" ",2);
		String command = parts[0];
		String[] keyarr = parts[1].split(",");
		int corPort = hashRef[hashVal(keyarr[0])];
		if(corPort==this.port)
		{
			System.out.println(myNo+":This server");
			noReq++;
			if(command.equalsIgnoreCase("get"))
			{
				return memory.get(keyarr[0]);
			}
			else if(command.equalsIgnoreCase("del"))
			{
				memory.remove(keyarr[0]);
				return "DEL SUCCESSFUL";
			}
			else
			{
				memory.put(keyarr[0],keyarr[1]);
				System.out.println(memory.get(keyarr[0]));
				return "PUT SUCCESSFUL";
			}
		}
		else
		{
			System.out.println(myNo+":Another server");
			noReq++;
			return forwardReq(corPort,request);
		}
	}

	public void run()throws IOException
	{
		serverSocket = new ServerSocket(this.port);
		try
		{
			serSock = serverSocket.accept();
			System.out.println(myNo+":Accepted Client");

			pwrite = new PrintWriter(serSock.getOutputStream(), true);
			receiveRead = new BufferedReader(new InputStreamReader(serSock.getInputStream()));

			while(true)
			{
				System.out.println(myNo+":Waiting for Request");
				String request = receiveRead.readLine();
				if(request!=null)
				{
					if(request.equals("quit"))
					{
						serSock.close();
						pwrite.close();
						receiveRead.close();
						System.out.println(myNo+":Waiting to Accept Client");
						serSock = serverSocket.accept();
						System.out.println(myNo+":Accepted Client");
						pwrite = new PrintWriter(serSock.getOutputStream(), true);
						receiveRead = new BufferedReader(new InputStreamReader(serSock.getInputStream()));
					}
					else
					{
						System.out.println(myNo+":Validating & Processing Request");
						boolean valid = validate(request);
						System.out.println(myNo+":Validated Request");
						if(valid)
						{
							String response = process(request);
							pwrite.println(response);
							pwrite.flush();
						}
						else
						{
							pwrite.println("Invalid request.");
							pwrite.flush();
						}
						System.out.println(myNo+":Responded");
					}
				}
				else		//if request==null
				{
					serSock.close();
					serSock = serverSocket.accept();
					pwrite = new PrintWriter(serSock.getOutputStream(), true);
					receiveRead = new BufferedReader(new InputStreamReader(serSock.getInputStream()));
				}
			}
		}
		catch(SocketException e)
		{
			if(serSock!=null)
			{
				pwrite.close();
				receiveRead.close();
				serSock.close();
			}
			if(serverSocket!=null)
				serverSocket.close();
		}
		catch(Exception e){System.err.println(e);}
	}
}
