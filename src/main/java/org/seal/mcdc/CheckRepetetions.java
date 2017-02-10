package org.seal.mcdc;

import java.util.ArrayList;

public class CheckRepetetions {

	public static String or    = "|";
	public static String and   = "&";
	public static String zero  = "0";
	int lastPos  = -1;
	int firstPos = -1;
	
	public void deleteRepetetions(ArrayList<String> postfix)
	{
		int firstOR = -1, secondOR = -1, varPos = 0,startPos = 0;
		String variable = "";
				
		secondOR = ORPosition(postfix,startPos);
		
		for(int i=0;i<postfix.size()-1;i++)
		{
			//System.out.println(postfix.get(i));
		  if(postfix.get(i).equals(or))
		  {
			  firstOR  = secondOR;
			  secondOR = ORPosition(postfix,firstOR+1);
			  
		  }	  
		  if(!postfix.get(i).equals(and)&&!postfix.get(i).equals(or)&&!postfix.get(i).equals(zero))	
		   for(int j=i+1;j<postfix.size();j++)
		   {
			  //System.out.println(postfix.get(j)); 
			  if(postfix.get(i).equals(postfix.get(j)))
			  {
				  variable = postfix.get(i);
				  postfix.remove(i);
				  postfix.add(firstOR+1,variable);
				  varPos = j;
				  firstLastPosition(postfix,varPos);
				  changePositions(postfix, variable, secondOR);
				  
			  }//IF	  
			  
		    }//FOR J
		  		  
		}//FOR I
		
		for(int i=0;i<postfix.size();i++)
		{
			System.out.println(postfix.get(i));
			
		}
			
	}//deleteRepetetions
	
	public int  ORPosition(ArrayList<String> postfix,int startPos)
	{
		
		for(int i=startPos;i<postfix.size();i++)
		 if (postfix.get(i).equals(or)) 		
		  return i;
		
		return startPos;
	}//ORPosition
	
	public void firstLastPosition(ArrayList<String> postfix,int varPos)
	{
		boolean exists = false;
		
		for(int i=varPos;i<postfix.size();i++)
		{
		  if (postfix.get(i).equals(or)) 		
		  {
			  exists  = true;
			  lastPos = i-1;
			  break;
		  }
		}
		if(!exists)
		{
			lastPos = postfix.size()-1;
		}	
		
		for(int i=varPos;i>=0;i--)
		{
		  if (postfix.get(i).equals(or)) 		
		  {
			  exists=true;
			  firstPos=i;
			  break;
		  }
		}
		
	}//firstLastPosition
	
	public void changePositions(ArrayList<String> postfix,String variable,int secondOR)
	{
		ArrayList<String> tempAL = new ArrayList<String>();
		
		for(int i=firstPos;i<=lastPos;i++)
		{
			//if(!postfix.get(i).equals("&"))
			for(int j=0;j<postfix.size();j++)
			{
				System.out.print(postfix.get(j));
				
			}
			System.out.println();
			System.out.println("pc: "+postfix.get(i));
			if(postfix.get(i).equals(variable))
				tempAL.add(zero);
			else 
				tempAL.add(postfix.get(i));
		}
		for(int j=0;j<tempAL.size();j++)
		{
			System.out.print(tempAL.get(j));
			
		}
		for(int i=firstPos;i<=lastPos;i++)
		{
			postfix.remove(firstPos);
		}
		
		for(int i=0;i<tempAL.size();i++)
		{  
			if(secondOR==postfix.size())
				postfix.add(tempAL.get(i));
			else	
			postfix.add(secondOR+1,tempAL.get(i));
		}
	 }
	
}//CLASS

