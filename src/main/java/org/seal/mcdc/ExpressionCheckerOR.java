package org.seal.mcdc;
//package mcdc;
public class ExpressionCheckerOR {

	
	public int checkOperands(String operand1,String operand2,int operatorValue)
	{
		switch(operatorValue)
		{
		 case 1://System.out.println("case 1:"); 
			    if(operand1.equals(operand2)) //<<
		        {
			      return 2;
		        }
			   
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
		         {
		        	if(Double.parseDouble(operand1)==Double.parseDouble(operand2))
			    	   return 2;
			    	if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
		    	    {
		    		   return 2;
		    	    }
		    	    else return 1;
		         }	   
		         break;
		         
		 case 2:if(operand1.equals(operand2)) //<<=
                {
		          return 1;
	            }
		        if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
	            {
	    	      if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
	    	      {
	    		    return 2;
	    	      }
	    	      else return 1;
	            }	   
	            break;
		 case 3:                                //<> 
			    if(operand1.equals(operand2)) 
			    {
		          return 4;//special
	            }
		        if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
	            {
	    	      if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
	    	      {
	    		    return 3;
	    	      }
	    	      else return 0;
	            }	   
	            break;
		 case 4: if(operand1.equals(operand2)) //<>=
                 {
	               return 3;
                 }
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
 	               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
 	               {
 		              return 3;
 	               }
 	               else return 0;
                 }	   
                 break; 
		 case 5: if(operand1.equals(operand2)) //<==
                    return 5;
		       
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
   	               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
   	               {
   		             return 2;
   	               }
   	               else return 0;
                  }	   
                  break;
		 case 6: if(operand1.equals(operand2)) //<!=
                 {
	               return 1;
                 }
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
 	               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
 	               {
 		             return 3;
 	               }
 	               else return 1;
                 }	   
                 break;
		 case 7: if(operand1.equals(operand2)) //<=<
                 {
	                return 2;
	             }
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
 	                if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
 	                {
 		              return 2;
 	                }
 	                else return 1;
                 }	   
                 break;
		 case 8:if(operand1.equals(operand2)) //<=<=
                {
	              return 2;
                }
		        if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                {
 	               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
 	               {
 		             return 2;
 	               }
 	               else return 1;
                }	   
                break;
                
		 case 9: if(operand1.equals(operand2)) //<=>
                 {
	               return 3;//true
                 }
		         if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
 	               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
 	               {
 		              return 3;
 	               }
 	               else return 0;
                 }	   
                 break;
		 case 10: if(operand1.equals(operand2)) //<=>=
                  {
                   return 3;//true
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                      if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                      {
	                    return 0;
                      }
                      else return 0;
                  }	   
                  break;
		 case 11: if(operand1.equals(operand2)) //<= ==
                  {
                    return 2;
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
	                   return 2;
                     }
                     else return 0;
                  }	   
                  break;
                
		 case 12: if(operand1.equals(operand2)) //<=!=
                  {
                    return 3;//true
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
	                   return 3;
                    }
                    else return 1;
                  }	   
                  break;
           ////////////END OF <=       
        
		 case 13: //><
			     if(operand1.equals(operand2)) 
			     {
                   return 4;//special
                 }
	             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                      return 0;
                    }
                    else return 3;
                 }	   
                 break;
		 	 
		 case 14: if(operand1.equals(operand2)) //><=
                  {
                    return 3;//true
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 0;
                     }
                     else return 3;
                  }	   
                  break;
		 case 15: if(operand1.equals(operand2)) //>==
                  {
                     return 6;// special
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                       return 0;
                    }
                    else return 2;
                  }	   
                  break;
		 case 16: if(operand1.equals(operand2)) //>!=
                  {
                     return 1;
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 1;
                     }
                     else return 3;
                  }	   
                  break;        
		 case 17: //>>
			     if(operand1.equals(operand2)) 
                 {
                   return 2;
                 }
	             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
                   if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                   {
                     return 1;
                   }
                   else return 2;
                 }	   
                 break;
		 case 18: if(operand1.equals(operand2)) //>>=
                  {
                     return 1;
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 1;
                     }
                     else return 2;
                  }	   
                  break;        
        ///////////////END OF >
		 case 19://>=<
			     if(operand1.equals(operand2)) 
                 {
                    return 3;//true
                 }
	             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
                   if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                   {
                     return 0;
                   }
                   else return 3;
                 }	   
                 break;
		 case 20: if(operand1.equals(operand2)) //>=<=
                  {
                    return 3;//true
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 0;
                     }
                     else return 3;
                  }	   
                  break; 
                
		 case 21: if(operand1.equals(operand2)) //>= ==
                  {
                     return 2;
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 0;
                     }
                     else return 2;
                  }	   
                  break;        
                 
		 case 22: if(operand1.equals(operand2)) //>=!=
                  {
                     return 3;
                  }
		          if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 1;
                     }
                     else return 3;
                  }	   
                  break;  
		 case 23: //>=>
			      if(operand1.equals(operand2))
                  {
                    return 2;
                  }
	              if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 1;
                     }
                     else return 2;
                  }	   
                  break;
		 case 24: //System.out.println("case 24:");
			      if(operand1.equals(operand2)) //>=>=
                  {
                    return 2;
                  }
			      if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                      return 1;
                    }
                    else return 2;
                  }	   
                  break;  
         //////////// END OF >=
		 case 25: //System.out.println("case 25");
			      if(operand1.equals(operand2)) //== <
                  {
                    return 5;//special
                  }
			      if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?")) 
			      {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 0;
                     }
                     else return 1;
                  }	   
                  break; 
		 case 26: //System.out.println("case 26");
	              if(operand1.equals(operand2)) //== <=
                  {
                     return 1;
                  }
	              if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 0;
                     }
                     else return 1;
                  }	   
                  break;
                  
		 case 27: //System.out.println("case 27");
			     if(operand1.equals(operand2)) //== ==
                 {
                   return 2;
                 }
	             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
                   if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                   {
                    return 0;
                   }
                   else return 0;
                 }	   
             break;        
       
		 case 28: //System.out.println("case 28");
                  if(operand1.equals(operand2)) //== !=
                  {
                     return 3; //true
                  }
                  if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                	if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                      return 1;
                    }
                    else return 1;
                  }	   
                  break; 
                  
                         
		 case 29://== >
			 //System.out.println("case 29");
             if(operand1.equals(operand2)) 
             {
               return 6; //special >=
             }
             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
             {
               if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
               {
                 return 1;
               }
               else return 0;
             }	   
             break;
		 case 30: //System.out.println("case 30");
	              if(operand1.equals(operand2)) //== >=
                  {
                    return 1;
                  }
	              if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                      return 1;
                    }
                    else return 0;
                  }	   
                  break; 
         //// END OF ==    
                  
		 case 31: //System.out.println("case 31");
	             if(operand1.equals(operand2)) //!= <
                 {
                    return 2;
                 }
	             if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                 {
                   if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                   {
                      return 2;
                   }
                   else return 3;
                 }	   
                 break; 
		 case 32: //System.out.println("case 32");
	              if(operand1.equals(operand2)) //!= <=
                  {
                    return 3;
                  }
	              if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                       return 2;
                    }
                    else return 3;
                  }	   
                  break;
		 case 33: //System.out.println("case 33");
                  if(operand1.equals(operand2)) //!= ==
                  {
                      return 3;
                  }
                  if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     return 2;
                  }	   
                  break; 
		 case 34: //System.out.println("case 34");
                  if(operand1.equals(operand2)) //!= !=
                  {
                    return 2;
                  }
                  if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     return 3;
                  }	 
		 case 35: //System.out.println("case 35");
                  if(operand1.equals(operand2)) //!= >
                  {
                     return 2;
                  }
                  if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                     if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                     {
                        return 3;
                     }
                     else return 2;
                  }	   
                  break;           
                    
		 case 36: //System.out.println("case 36");
                  if(operand1.equals(operand2)) //!= >=
                  {
                    return 3;
                  }
                  if(operand1.matches("(-)?[\\d]+(.[\\d]+)?")&& operand2.matches("(-)?[\\d]+(.[\\d]+)?"))
                  {
                    if(Double.parseDouble(operand1)>Double.parseDouble(operand2))
                    {
                       return 3;
                    }
                    else return 2;
                  }	   
                  break;  
		 default: return 10;
		}
		//System.out.println("zero");
		return 0;
	}
}

