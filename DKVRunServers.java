import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
public class DKVRunServers
{
	public static void main(String[] args)throws InterruptedException, IOException, KeeperException
	{
		BufferedReader br = new BufferedReader(new FileReader("DKVconfig.txt"));
		String line = br.readLine();
		int K=1;					//No of servers
		while(line!=null)
		{
			String arr[] = line.split("=");
			if(arr[0].equals("K"))
				K = Integer.parseInt(arr[1]);
			line = br.readLine();
		}

		final ZooKeeper zk = new ZooKeeper("localhost", 10000, new Watcher()
				{
					public void process(WatchedEvent we)
					{
						return;
					}
				});
		
		if(zk.exists("/app",true)==null)
		{
			zk.create("/app", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
							//First time the program runs
		}

		DKVServer server[] = new DKVServer[K];
		Thread serCo[] = new Thread[K];
		for(int i=0; i<K; i++)
		{
			server[i] = new DKVServer();
		}
				//Creates K servers


		for(int i=0; i<K; i++)
		{
			final DKVServer s = server[i];
			serCo[i] = new Thread(new Runnable()
			{
				public void run()
				{try{s.contestForLeader(zk);}catch(Exception e){}}
			});
		}
		try			//All servers contest to become the leader
		{
			for(int i=0; i<K; i++)
			{
				serCo[i].start();
			}
			for(int i=0; i<K; i++)
			{
				serCo[i].join();
			}
		}
		catch(Exception e){}



		List<String> children = zk.getChildren("/app",false);			//Find the leader
		String leader = children.get(0);
		int min = Integer.parseInt(leader.substring(7));
		for(int i = 0; i < children.size(); i++)
		{
			String candidate = children.get(i);
			int mi = Integer.parseInt(candidate.substring(7));
			if(mi<min)
			{
				min = mi;
				leader = candidate;
			}
		}
		leader = "/app/"+leader;
		byte b[] = zk.getData(leader, false, null);
		int ldNo = Integer.parseInt(new String(b, "UTF-8"));
		
		boolean proceed = server[ldNo-1].callMaster(zk);						//Leader performs its functions

		if(!proceed)
		{
			System.exit(0);
		}


		for(int i=0; i<K; i++)
		{
			final DKVServer s = server[i];
			serCo[i] = new Thread(new Runnable()
			{
				public void run()
				{try{s.serverSetup(zk);}catch(Exception e){}}
			});
		}
		try							//Servers setup
		{
			for(int i=0; i<K; i++)
			{
				serCo[i].start();
			}
			for(int i=0; i<K; i++)
			{
				serCo[i].join();
			}
		}
		catch(Exception e){}

		zk.close();


		final int J = K;

		final DKVServer ser[] = server;

		Thread term = new Thread(new Runnable()
			{
				public void run()			//Thread to terminate program
				{
					try
					{
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						String str = br.readLine();
						while(!str.equals("quit"))
						{
							str = br.readLine();
						}
						for(int i=0;i<J;i++)
							ser[i].terminate();
					}
					catch(Exception e){}
				}
			});

		for(int i=0; i<K; i++)
		{
			final DKVServer s = server[i];
			serCo[i] = new Thread(new Runnable()
			{
				public void run()
				{try{s.run();}catch(Exception e){}}
			});
		}
		try						//All servers and terminating thread run
		{
			for(int i=0; i<K; i++)
			{
				serCo[i].start();
			}
			term.start();
			for(int i=0; i<K; i++)
			{
				serCo[i].join();
			}
			term.join();
		}
		catch(Exception e){}
	}
}
