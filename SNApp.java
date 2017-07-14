import java.io.*;
import java.net.*;
//Name, Country, Age, Gender, Username
public class SNApp
{
	public final static char KEY_SEP = (char)30;
	public final static char WHR_SEP = (char)31;
	public final static char FND_SEP = (char)29;

	public static int nodeCount=0;
	public static String columns[] = {"Age","Country","Gender","Name","Username"};

	public static boolean checkIsNum(String str)
	{
		boolean isNum=true;
		try
		{
			double b=Double.parseDouble(str);
		}
		catch(NumberFormatException e)
		{
			isNum = false;
		}
		return isNum;
	}

	public static String operation(String arg1, String op, String arg2)
	{
		if(arg1.equals("null") || arg2.equals("null"))
			return "F";
		double a=0,b=0;
		boolean isNum=checkIsNum(arg1) && checkIsNum(arg2);
		if(isNum)
		{
			a=Double.parseDouble(arg1);
			b=Double.parseDouble(arg2);
		}
		if(op.equals("<"))
		{
			if(!isNum)				//Not a valid comparison operator for strings
				return null;
			return a<b?"T":"F";
		}
		else if(op.equals(">"))
		{
			if(!isNum)
				return null;
			return a>b?"T":"F";
		}
		else if(op.equals("<="))
		{
			if(!isNum)
				return null;
			return a<=b?"T":"F";
		}
		else if(op.equals(">="))
		{
			if(!isNum)
				return null;
			return a>=b?"T":"F";
		}
		else if(op.equals("=="))
		{
			if(!isNum)
				return arg1.equals(arg2)?"T":"F";
			return a==b?"T":"F";
		}
		else if(op.equals("!="))
		{
			if(!isNum)
				return !arg1.equals(arg2)?"T":"F";
			return a!=b?"T":"F";
		}
		else
			return null;
	}
	public static String friendUnion(String l1, String l2)
	{
		String ans = "";
		String ar1[] = l1.split(""+FND_SEP);
		String ar2[] = l2.split(""+FND_SEP);
		int i=1,j=1,n=ar1.length,m=ar2.length;
		int last = 0;
		while(i<n && j<m)		//Sorts
		{
			int a1 = Integer.parseInt(ar1[i]);
			int a2 = Integer.parseInt(ar2[j]);
			if(a1==a2)		//Only unique entries
			{
				ans+=""+FND_SEP+a1;
				i++;
				j++;
			}
			else if(a1<a2)
			{
				ans+=""+FND_SEP+a1;
				i++;
			}
			else
			{
				ans+=""+FND_SEP+a2;
				j++;
			}
		}
		while(i<n)
		{
			int a1 = Integer.parseInt(ar1[i]);
			ans+=""+FND_SEP+a1;
			i++;
		}
		while(j<m)
		{
			int a1 = Integer.parseInt(ar2[j]);
			ans+=""+FND_SEP+a1;
			j++;
		}
		return ans;
	}
	public static String whereClauseFriends(String cond, PrintWriter pw, BufferedReader br)throws IOException
	{					//Evaluate anything with and, or, "<",">","<=",">=","==","!=" and Friend
		String answer = "";
		int i;
		String temp[] = cond.split(" or ");			//Lower precedence
		int n = temp.length;
		String conds[][] = new String[n][];
		for(i=0; i<n; i++)
		{
			conds[i] = temp[i].split(" and ");
		}
		i=1;

		//Check attributes
		for(int j=0; j<n; j++)
		{
			int m = conds[j].length;
			int k=0;
			if(!conds[j][k].equals(""))
			{
				for(; k<m; k++)
				{
					String ops[] = {"<",">","<=",">=","==","!="};
					String op = "",attribute="",value="";
					int pass=0;
					for(int w=0; w<6; w++)		//Separates operator, left-side (as attribute) and right-side (as value)
					{
						temp = conds[j][k].split(ops[w]);
						if(temp.length==2)
						{
							pass++;
							attribute=temp[0].trim();
							value=temp[1].trim();
							op=ops[w];
							if(w==2 || w==3)		//<= and >= will pass for < and > first respectively
							{
								pass--;
							}
						}
					}
					if(pass!=1)
					{
						System.out.println("Error: Wrong number or type of operators / Invalid separation");
						return null;
					}
					
							//Validating attributes
					
					if(conds[j][k].trim().contains("Friend."))
					{
						boolean left = attribute.contains("Friend.");
						boolean right = value.contains("Friend.");
						if(!left)
						{
							pw.println("get "+attribute);
							pw.flush();
							String response = br.readLine();
							if(response.equals("null"))
							{
								System.out.println("Error: Invalid attribute");
								return null;
							}
						}
						if(!right)
						{
							pw.println("get "+value);
							pw.flush();
							String response = br.readLine();
							if(response.equals("null"))		//Not an attribute
							{
								if(!checkIsNum(value))
								{
									if(value.charAt(0)=='"' && value.charAt(value.length()-1)=='"')
									{
										if(value.length()<=2) //No " or "" allowed
										{
											System.out.println("Error: Value not entered");
											return null;
										}
										else if(value.substring(1,value.length()-1).contains("\""))
										{
											System.out.println("Error: Values not in an appropriate format / Inappropriate separation");
											return null;
										}
									}
									else
									{
										System.out.println("Error: Values not in an appropriate format");
										return null;
									}
								}
							}
						}
						if(left)
						{
							pw.println("get "+attribute.substring(7));
							pw.flush();
							String response = br.readLine();
							if(response.equals("null"))
							{
								System.out.println("Error: Invalid attribute");
								return null;
							}
						}
						if(right)
						{
							pw.println("get "+value.substring(7));
							pw.flush();
							String response = br.readLine();
							if(response.equals("null"))
							{
								System.out.println("Error: Invalid attribute");
								return null;
							}
						}
					}
					else
					{
						pw.println("get "+attribute);
						pw.flush();
						String response = br.readLine();
						if(response.equals("null"))
						{
							System.out.println("Error: Invalid attribute");
							return null;
						}

						pw.println("get "+value);
						pw.flush();
						response = br.readLine();
						if(response.equals("null"))
						{
							if(!checkIsNum(value))
							{
								if(value.charAt(0)=='"' && value.charAt(value.length()-1)=='"')
								{
									if(value.length()<=2) //No " or "" allowed
									{
										System.out.println("Error: Value not entered");
										return null;
									}
									else if(value.substring(1,value.length()-1).contains("\""))
									{
										System.out.println("Error: Values not in an appropriate format / Inappropriate separation");
										return null;
									}
								}
								else
								{
									System.out.println("Values not in an appropriate format");
									return null;
								}
							}
						}
					}
				}
			}
		}
		
		
		while(i<=nodeCount)				//Evaluates conditions for each node in ascending order
		{
			pw.println("get "+i);
			pw.flush();
			String friends = br.readLine();
			String friendsFinal = "";
			int j=0,k=0;
			boolean orconds=false;
			while(j<n && !orconds)			//If any or separated condition is true, no need to continue
			{
				String friendsPass = friends;
				int m = conds[j].length;
				boolean andconds=true;
				k=0;
				if(!conds[j][k].equals(""))
				{
					while(k<m && andconds)	//If any and separated condition is false, no need to continue
					{
						String ops[] = {"<",">","<=",">=","==","!="};
						String op = "",attribute="",value="";
						for(int w=0; w<6; w++)		//Separates operator, left-side (as attribute) and right-side (as value)
						{
							temp = conds[j][k].split(ops[w]);
							if(temp.length==2)
							{
								attribute=temp[0].trim();
								value=temp[1].trim();
								op=ops[w];
							}
						}
						
						if(conds[j][k].trim().contains("Friend."))
						{
							String leftArg="",rightArg="";
							boolean left = attribute.contains("Friend.");
							boolean right = value.contains("Friend.");
							if(!left)
							{
								pw.println("get "+i+KEY_SEP+attribute);
								pw.flush();
								leftArg = br.readLine();
							}
							if(!right)
							{
								pw.println("get "+value);
								pw.flush();
								String response = br.readLine();
								if(!response.equals("null") && !checkIsNum(value))		//Right side is an attribute.
								{
									pw.println("get "+i+KEY_SEP+value);
									pw.flush();
									rightArg = br.readLine();
								}
								else
								{
									if(!checkIsNum(value))
									{
										value = value.substring(1,value.length()-1);
									}
									rightArg = value;
								}
							}
							String fnds[] = friendsPass.split(""+FND_SEP);
							int f=1;
							for(;f<fnds.length; f++)
							{
								if(left)
								{
									pw.println("get "+fnds[f]+KEY_SEP+attribute.substring(7));
									pw.flush();
									leftArg = br.readLine();
								}
								if(right)
								{
									pw.println("get "+fnds[f].trim()+KEY_SEP+value.substring(7));
									pw.flush();
									rightArg = br.readLine();
								}
								String ans = operation(leftArg, op, rightArg);
								if(ans==null)
								{
									System.out.println("Error: Invalid operation or types");
									return null;
								}
								else if(ans.equals("F"))
								{
									friendsPass = friendsPass.replaceFirst(FND_SEP+fnds[f],"");
								}
							}
							andconds = andconds && !friendsPass.equals("");
						}
						else
						{
							pw.println("get "+value);
							pw.flush();
							String response = br.readLine();
							if(!response.equals("null") && !checkIsNum(value))
							{
								pw.println("get "+i+KEY_SEP+value);
								pw.flush();
								value = br.readLine();
							}
							else
							{
								if(!checkIsNum(value))
								{
									value = value.substring(1,value.length()-1);
								}
							}

							pw.println("get "+i+KEY_SEP+attribute);
							pw.flush();
							response = br.readLine();
							String ans = operation(response, op, value);
							if(ans==null)
							{
								System.out.println("Error: Invalid operation or types");
								return null;
							}
							andconds = andconds && ans.equals("T");
						}
						k++;
					}
					orconds = andconds || orconds;
				}
				if(orconds)
				{
					friendsFinal = friendUnion(friendsFinal,friendsPass);
				}
				j++;
			}
			if(orconds && !friendsFinal.equals(""))
			{
				answer += ""+WHR_SEP+i+friendsFinal;
			}
			i++;
		}
		return answer;
	}
	public static String whereClause(String cond, PrintWriter pw, BufferedReader br)throws IOException
	{					//Evaluate anything with "and", "or", "<",">","<=",">=","==","!="
		String answer = "";
		int i;
		String temp[] = cond.split(" or ");			//Lower precedence
		int n = temp.length;
		String conds[][] = new String[n][];
		for(i=0; i<n; i++)
		{
			conds[i] = temp[i].split(" and ");
		}
		i=1;
		
		for(int j=0; j<n; j++)
		{
			int m = conds[j].length;
			int k=0;
			if(!conds[j][k].equals(""))
			{
				for(; k<m; k++)
				{
					String ops[] = {"<",">","<=",">=","==","!="};
					String op = "",attribute="",value="";
					int pass=0;
					for(int w=0; w<6; w++)		//Separates operator, left-side (as attribute) and right-side (as value)
					{
						temp = conds[j][k].split(ops[w]);
						if(temp.length==2)
						{
							pass++;
							attribute=temp[0].trim();
							value=temp[1].trim();
							op=ops[w];
							if(w==2 || w==3)
							{		//<= and >= will pass for < and > first respectively
								pass--;
							}
						}
					}
					if(pass!=1)
					{
						System.out.println("Error: Wrong number or type of operators / Invalid separation");
						return null;		//e.g: 4<c<7 or a=8
					}
					pw.println("get "+attribute);
					pw.flush();
					String response = br.readLine();
					if(response.equals("null"))
					{
						System.out.println("Error: Invalid attribute");
						return null;
					}

					pw.println("get "+value);
					pw.flush();
					response = br.readLine();
					if(response.equals("null"))		//Right-side is an attribute
					{
						if(!checkIsNum(value))
						{
							if(value.charAt(0)=='"' && value.charAt(value.length()-1)=='"')
							{
								if(value.length()<=2) //No " or "" allowed
								{
									System.out.println("Error: Value not entered");
									return null;
								}
								else if(value.substring(1,value.length()-1).contains("\""))
								{
									System.out.println("Error: Values not in an appropriate format / Inappropriate separation");
									return null;
								}
							}
							else
							{
								System.out.println("Error: Values not in an appropriate format");
								return null;
							}
						}
					}
				}
			}
		}

		
		
		i=1;
		while(i<=nodeCount)				//Evaluates conditions for each node in ascending order
		{
			int j=0,k=0;
			boolean orconds=false;
			while(j<n && !orconds)			//If any or separated condition is true, no need to continue
			{
				int m = conds[j].length;
				boolean andconds=true;
				k=0;
				if(!conds[j][k].equals(""))
				{
					while(k<m && andconds)	//If any and separated condition is false, no need to continue
					{
						String ops[] = {"<",">","<=",">=","==","!="};
						String op = "",attribute="",value="";
						for(int w=0; w<6; w++)		//Separates operator, left-side (as attribute) and right-side (as value)
						{
							temp = conds[j][k].split(ops[w]);
							if(temp.length==2)
							{
								attribute=temp[0].trim();
								value=temp[1].trim();
								op=ops[w];
							}
						}
						pw.println("get "+value);
						pw.flush();
						String response = br.readLine();
						if(!response.equals("null") && !checkIsNum(value))		//Right-side is an attribute
						{
							pw.println("get "+i+KEY_SEP+value);
							pw.flush();
							value = br.readLine();
						}
						else
						{
							if(!checkIsNum(value))
							{
								value = value.substring(1,value.length()-1);
							}
						}

						pw.println("get "+i+KEY_SEP+attribute.trim());
						pw.flush();
						response = br.readLine();
						String ans = operation(response, op, value);
						if(ans==null)
						{
							System.out.println("Error: Invalid operation or types");
							return null;
						}
						andconds = andconds && ans.equals("T");
						k++;
					}
					orconds = andconds || orconds;
				}
				j++;
			}
			if(orconds)	//Node satisfied condition completely
			{
				answer += ""+WHR_SEP+i;
			}
			i++;
		}
		return answer;
	}
	public static void query(String details, PrintWriter pw, BufferedReader br)throws IOException
	{
		boolean friendQuery;			
		String nodes[];
		String attributes[];
		friendQuery = details.contains("Friend.");
		
		String temp[] = details.split("where");
		
		

					//Determines attributes requested
		String attributesTemp = temp[0].trim();
		if(attributesTemp.equals("*"))
		{
			if(!friendQuery)
			{
				attributes = columns;
			}
			else
			{
				attributes = new String[2*columns.length];
				for(int i=0; i<columns.length; i++)
				{
					attributes[i] = columns[i];
				}
				for(int i=0; i<columns.length; i++)
				{
					attributes[i+columns.length] = "Friend."+columns[i];
				}
			}
		}
		else
		{
			attributes = attributesTemp.split(",");
		}
		
		String friendArgs=null;
		String selfArgs=null;

		for(int i=0; i<attributes.length; i++)
		{
			if(attributes[i].contains("Friend."))
			{
				if(friendArgs==null)
				{
					friendArgs = attributes[i].trim();
				}
				else
				{
					friendArgs += ","+attributes[i].trim();
				}
				pw.println("get "+attributes[i].trim().substring(7));
			}
			else
			{
				if(selfArgs==null)
				{
					selfArgs = attributes[i].trim();
				}
				else
				{
					selfArgs += ","+attributes[i].trim();
				}
				pw.println("get "+attributes[i].trim());
			}
			pw.flush();
			String response = br.readLine();
			if(response.equals("null"))
			{
				System.out.println("Check attributes to be retrieved. Format=\"query <comma separated attribute list> where <condition>\"");
				return;
			}
		}


					//Determines nodes requested
		if(temp.length==1)			//No where condition
		{
			nodes = new String[nodeCount+1];
			nodes[0]="";
			for(int i=1;i<=nodeCount;i++)			//All nodes
			{
				nodes[i] = ""+i;
			}
			if(friendQuery)					//All nodes with neighbours
			{
				for(int i=1;i<=nodeCount;i++)
				{
					pw.println("get "+i);
					pw.flush();
					String response = br.readLine();
					if( !(response.equals("null") || response.equals(""+FND_SEP)) )
					{
						nodes[i] += response;
					}
				}
			}
		}
		else
		{
			String nodeList;
			if(temp.length!=2)
			{
				System.out.println("Error: Invalid where condition. Format=\"select <comma separated attribute list or *> [where <condition>]\"");
				return;
			}
			if(friendQuery)
			{
				nodeList = whereClauseFriends(temp[1].trim(), pw, br);
			}
			else
			{
				nodeList = whereClause(temp[1].trim(), pw, br);
			}
			if(nodeList==null)
			{
				return;
			}
			if(nodeList.equals(""))
			{
				System.out.println("No results to display.");
				return;
			}
			nodes = nodeList.split(""+WHR_SEP);
		}


					//Prints results
		if(friendQuery)
		{
			String selfAttr[]=null;
			String friendAttr[]=null;
			if(selfArgs!=null)
			{
				selfAttr = selfArgs.split(",");
				for(int j=0; j<selfAttr.length; j++)
				{
					System.out.print(selfAttr[j]+"\t");
				}
			}

			if(friendArgs!=null)
			{
				friendAttr = friendArgs.split(",");
				for(int j=0; j<friendAttr.length; j++)
				{
					System.out.print(friendAttr[j]+"\t");
				}
			}

			System.out.println();
			for(int i=1; i<nodes.length; i++)
			{
				String nds[] = nodes[i].split(""+FND_SEP);
				for(int k=1; k<nds.length; k++)
				{
					if(selfAttr!=null)
					{
						for(int j=0; j<selfAttr.length; j++)
						{
							pw.println("get "+nds[0]+KEY_SEP+selfAttr[j].trim());
							pw.flush();
							String response = br.readLine();
							System.out.print( response+"\t" );
						}
					}
					if(friendAttr!=null)
					{
						for(int j=0; j<friendAttr.length; j++)
						{
							pw.println("get "+nds[k]+KEY_SEP+friendAttr[j].substring(7).trim());
							pw.flush();
							String response = br.readLine();
							System.out.print( response+"\t" );
						}
					}
					System.out.println();
				}
			}
		}
		else
		{
			for(int j=0; j<attributes.length; j++)
			{
				System.out.print(attributes[j]+"\t");
			}
			System.out.println();
			for(int i=1; i<nodes.length; i++)
			{
				for(int j=0; j<attributes.length; j++)
				{
					pw.println("get "+nodes[i]+KEY_SEP+attributes[j].trim());
					pw.flush();
					String response = br.readLine();
					System.out.print( response+"\t" );
				}
				System.out.println();
			}
		}
	}
	public static void deleteFriends(int n, PrintWriter pw, BufferedReader br)throws IOException
	{
		pw.println("get "+n);
		pw.flush();
		String friends = br.readLine();
		if(!friends.equals("null"))
		{
			String fnds[] = friends.split(""+FND_SEP);
			String thisN = ""+n;
			for(int i=1; i<fnds.length; i++)
			{
				pw.println("get "+fnds[i]);
				pw.flush();
				String response = br.readLine();
				String newList = response;
				newList = response.replaceFirst(thisN+FND_SEP , "");		//Remove from friend list
				if(newList.equals(response))			//Friend at end
				{
					newList = response.replaceFirst(thisN, "");
					if(newList.length()!=1 && newList.charAt(newList.length()-1)==FND_SEP)
					{
						newList = newList.substring(0,newList.length()-1);	//Remove extra FND_SEP at end
					}
				}
				pw.println("put "+fnds[i]+","+newList);
				pw.flush();
				response = br.readLine();
			}
		}
		if(n!=nodeCount)						//Exchange all
		{
			pw.println("get "+nodeCount);
			pw.flush();
			String response = br.readLine();
			if(!response.equals("null"))
			{
				pw.println("put "+n+","+response);
				pw.flush();
				String temp = br.readLine();
				String frnds[] = response.split(""+FND_SEP);
				for(int i=1; i<frnds.length; i++)
				{
					pw.println("get "+frnds[i]);
					pw.flush();
					response = br.readLine();
					String newList = response;
					newList = replaceFriend(response, nodeCount, n);
					pw.println("put "+frnds[i]+","+newList);
					pw.flush();
					response = br.readLine();
				}
			}
			else
			{
				pw.println("del "+n);
				pw.flush();
				response = br.readLine();
			}
		}
		pw.println("del "+nodeCount);
		pw.flush();
		String response = br.readLine();
	}
	public static void delete(String details, PrintWriter pw, BufferedReader br)throws IOException
	{
		if(nodeCount==0)
		{
			System.out.println("No nodes.");
			return;
		}
		String nodeList = whereClause(details, pw, br);
		if(nodeList==null)
		{
			return;
		}
		if(nodeList.equals(""))
		{
			System.out.println("Does not exist.");
			return;
		}
		String nodes[] = nodeList.split(""+WHR_SEP);
		int n = Integer.parseInt(nodes[1]);
		if(n!=nodeCount)
		{
			for(int j=0; j<columns.length; j++)
			{
				pw.println("get "+nodeCount+KEY_SEP+columns[j]);
				pw.flush();
				String response = br.readLine();
				if(response.equals("null"))
				{
					pw.println("del "+n+KEY_SEP+columns[j]);
					pw.flush();
					response = br.readLine();
				}
				else
				{
					pw.println("put "+n+KEY_SEP+columns[j]+","+response);
					pw.flush();
					response = br.readLine();
				}
			}
		}
		for(int j=0; j<columns.length; j++)
		{
			pw.println("del "+nodeCount+KEY_SEP+columns[j]);
			pw.flush();
			String response = br.readLine();
		}
		deleteFriends(n,pw,br);
		nodeCount--;
	}
	public static String replaceFriend(String frndList, int toRep, int repWith)throws IOException
	{
		boolean found = false;
		String temp[] = frndList.split(""+FND_SEP);
		int friends[] = new int[temp.length-1];
		int i,index=-1;
		for(i=1;i<temp.length;i++)
		{
			friends[i-1] = Integer.parseInt(temp[i]);
		}
		for(i=0;i<friends.length;i++)
		{
			if(friends[i]==toRep)
			{
				index=i;
				friends[i] = repWith;
			}
		}
		if(index==-1)
		{
			return frndList;
		}
		else
		{
			frndList = "";
			i = index;
			while(i>0 && friends[i-1]>friends[i])	//Maintain ascending order
			{
				int t = friends[i];
				friends[i] = friends[i-1];
				friends[i-1] = t;
				i--;
			}
			for(i=0;i<friends.length;i++)
			{
				frndList += FND_SEP + "" + friends[i];
			}
			return frndList;
		}
	}
	public static void addFriend(int to, String friends, PrintWriter pw, BufferedReader br)throws IOException
	{
		if(friends.equals(""))
		{
			System.out.println("No friends to add.");
			return;
		}
		String fr[] = friends.split(""+WHR_SEP);
		int i=1;
		String newList = ""+FND_SEP;			//Current node's friends
		//whereClause returns in ascending order and new nodes always have the largest no (from nodeCount) so ascending order is maintained
		for(;i<fr.length;i++)
		{
			pw.println("get "+fr[i]);
			pw.flush();
			String response = br.readLine();
			String updList=""+FND_SEP+to;		//Neighbour node's updated list of friends
			if(! (response.equals("null")||response.equals(""+FND_SEP)) )	//Neighbour has other friends.
			{
				updList = response+FND_SEP+to;
			}
			pw.println("put "+fr[i]+","+updList);
			pw.flush();
			response = br.readLine();
			newList += fr[i];
			if(i!=fr.length-1)
			{
				newList += FND_SEP;
			}
		}
		pw.println("put "+to+","+newList);
		pw.flush();
		String response = br.readLine();
	}
	
	public static void insert(String details, PrintWriter pw, BufferedReader br)throws IOException
	{
		String ar[] = details.split(",");
		int n = ar.length;
		boolean proceed = true;		//To check validity before inserting any data.
		String friends = null;
		for(int i=0; i<n; i++)
		{
			ar[i] = ar[i].trim();
			if( !(ar[i].length()>6 && ar[i].substring(0,6).equals("Friend")) )
			{
				if(!ar[i].contains("="))
				{
					System.out.println("Error: Value not entered");
					return;
				}
				String tempAr[] = ar[i].split("=",2);
				String key = tempAr[0].trim();
				String value = tempAr[1].trim();
				if(!checkIsNum(value))
				{
					if(!(value.charAt(0)=='"' && value.charAt(value.length()-1)=='"'))
					{
						System.out.println("Error: Values not in an appropriate format.");
						return;
					}
					else if(value.length()<=2)	// Avoids single ". The empty string ("") is also not allowed.
					{
						System.out.println("Error: Value not entered");
						return;
					}
					else if(value.substring(1,value.length()-1).contains("\""))
					{
						System.out.println("Error: Values not in an appropriate format / Inappropriate separation");
						return;
					}
					
				}
				pw.println("get "+key);
				pw.flush();
				String response = br.readLine();
				if(response.equals("null"))	//Catch faults in condition before inserting any data
				{				//Attribute does not exist
					proceed=false;
				}
			}
			else			//Statement has friend condition
			{
				String response = whereClause(ar[i].substring(6).trim(), pw, br);
								//Finds friends that satisfy the friend condition
				if(response==null)
				{
					proceed=false;
				}
				else				//Any no of friend conditions.
				{
					if(friends==null)
						friends = response;
					else
						friends += response;
				}
			}
		}
		if(proceed)
		{
			System.out.println("Valid.");
			nodeCount++;
			for(int i=0; i<n; i++)
			{
				if( !(ar[i].length()>6 && ar[i].substring(0,6).equals("Friend")) )
				{
					String arr[] = ar[i].split("=",2);
					String value = arr[1].trim();
					if(!checkIsNum(value))
					{
						value = value.substring(1,value.length()-1);
					}
					String req = "put "+nodeCount+KEY_SEP+arr[0].trim()+","+value;
					pw.println(req);
					pw.flush();
					String response = br.readLine();
				}
			}
			if(friends!=null)
			{
				addFriend(nodeCount, friends, pw, br);
			}
			
		}
		else
		{
			System.out.println("Error: Invalid request. Check attributes.");
		}
	}
	public static void main(String[] args) throws Exception
	{
		BufferedReader co = new BufferedReader(new FileReader("SNAppconf.txt"));
		String arr[];
		String line = co.readLine();
		while(line!=null)
		{
			arr = line.split("=");
			if(arr[0].equals("count"))
				nodeCount = Integer.parseInt(arr[1]);
			line = co.readLine();
		}
		co.close();

		String request, response;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));		//Standard input

		Socket sock = new Socket("127.0.0.1", 3000);			//Can connect to any port of the key-value store.
		PrintWriter pwrite = new PrintWriter(sock.getOutputStream(), true);
		BufferedReader serverResp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		while(true)
		{
			try
			{
				request = br.readLine();
				if(request.equalsIgnoreCase("quit"))
				{
					pwrite.println(request);
					pwrite.flush();
					break;
				}
				String ar[] = request.split(" ",2);
				if(ar.length == 1)
				{
					if(!ar[0].equals(""))			//Ignores enter
						System.out.println("Error:Invalid statement.");	//No arguments
				}
				else
				{
					if(ar[0].equals("insert"))
					{
						insert(ar[1],pwrite, serverResp);
					}
					else if(ar[0].equals("delete"))
					{
						delete(ar[1],pwrite, serverResp);
					}
					else if(ar[0].equals("select"))
					{
						query(ar[1],pwrite, serverResp);
					}
					else
					{
						System.out.println("Error:Invalid command.");
					}
				}
			}
			catch(ConnectException e)
			{
				System.err.println(e);
			}
		}
		pwrite.close();
		serverResp.close();
		sock.close();

		BufferedWriter cw = new BufferedWriter(new FileWriter("SNAppconf.txt"));
		cw.write("count="+nodeCount+"\n");
		cw.close();
	}
}
