package org.seal.mcdc;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class TestCaseGenerator {

	  private int arrayCount=0, rowCount=-1;
	  private DefaultTableModel tempTestCases;
	  private DefaultTableModel MCDCTestCases; 
	  
	  public TestCaseGenerator(){
		  tempTestCases = new DefaultTableModel();
		  MCDCTestCases= new DefaultTableModel();
	  }
	  
	  public DefaultTableModel generateTestCases(ArrayList<String> postfix)  {
			  
		  int i=0,arrayNumber=0;
		  String firstVar,secondVar,operator;
		  
		  ArrayList<String> stack = new ArrayList<String>();
		  DefaultTableModel testCases1 = new DefaultTableModel();
		  DefaultTableModel testCases2 = new DefaultTableModel();
		 	   
		  if(postfix.size()<2)
		  {
			 return MCDCTestCases=copyArray();
		  }	  
		  
             for (i=0; i< postfix.size(); i++){
			  if((postfix.get(i)).equals("&")||(postfix.get(i)).equals("|")) {
				 operator=(postfix.get(i)).toString();
				 firstVar=stack.get(stack.size()-1);
				 stack.remove(stack.size()-1);
				  
				 if (firstVar.matches("[\\d]+")) {
				    arrayNumber=Integer.parseInt(firstVar);
					testCases1=copyArray(arrayNumber);
				  }
				  else {
					  testCases1=copyArray();
			      }	  
				  secondVar=stack.get(stack.size()-1);
				  stack.remove(stack.size()-1);
				  if (secondVar.matches("[\\d]+")) {
					  arrayNumber=Integer.parseInt(secondVar);
					  testCases2=copyArray(arrayNumber);
				  }
				  else {
					  testCases2=copyArray();
				  }
				 
				  generateCase(testCases2,testCases1,operator);
				  stack.add(Integer.toString(arrayCount));
				  arrayCount++;
			  }
			  else {
				  stack.add((postfix.get(i)).toString());
				  
			  }	  
			}	 
			arrayCount--;
			
			for(int col=0;col<tempTestCases.getColumnCount()-1;col++)
			{
				MCDCTestCases.addColumn("col"+col);
			}
		  copyMCDCTable(arrayCount);
		  //display();
		  return MCDCTestCases;
		  
	  }	
	  
// *****************FUNCTIONS***************
	  
	  DefaultTableModel  copyArray() {
    	  DefaultTableModel testCases = new DefaultTableModel();
    	  List<String> list = new ArrayList<String>();
    	  
    	  testCases.addColumn("col0");
    	  testCases.addColumn("col1");
    	  list.add("T");
    	  list.add("T");
    	  testCases.addRow(list.toArray());
    	  list.clear();
    	  list.add("F");
    	  list.add("F");
    	  testCases.addRow(list.toArray());
    	    	 
    	  return testCases;
    	  
	  }//COPY_ARRAY

      DefaultTableModel copyArray(int arrayNumber) {
    	  
    	  DefaultTableModel testCases = new DefaultTableModel();
    	  List<String> list = new ArrayList<String>();
    	  int colCount=0;
    	  for(int row=0;row<tempTestCases.getRowCount();row++){
    		  if(String.valueOf(arrayNumber).equals(String.valueOf((tempTestCases.getValueAt(row,0))))){
    		    for(int col=0;col<tempTestCases.getColumnCount();col++){
    			  if(tempTestCases.getValueAt(row,col)!=null){
    					colCount++;
    			}
    		  }
    		  break;	
    		}
    	  }
    	   for(int colName=0;colName<colCount-1;colName++)
    		  testCases.addColumn("Col " + colName);
    	   	  
    	  for(int row=0;row<tempTestCases.getRowCount();row++){
    		  if(String.valueOf(arrayNumber).equals(String.valueOf((tempTestCases.getValueAt(row,0)))) ){
      		  list.clear();	
      		  for(int col=1;col<tempTestCases.getColumnCount();col++){
      			if(tempTestCases.getValueAt(row,col)!=null){
      			   list.add(String.valueOf(tempTestCases.getValueAt(row,col)));
      			}
      		  }
      		  testCases.addRow(list.toArray());
      		}
      	  }
    	 
    	return testCases;
  		         
  	   }//COPY_ARRAY
      
      private void copyMCDCTable(int arrayCount)
      {
    	  int row,col,rowNumber=0;
    	  List<String> list = new ArrayList<String>();
    	   	  
    	  for(row=0;row<tempTestCases.getRowCount();row++){
      		  if(String.valueOf(arrayCount).equals(String.valueOf((tempTestCases.getValueAt(row,0))))){
      		        rowNumber=row;
      		        break;	
      		  }
      	  }
    	  for(row=rowNumber;row<tempTestCases.getRowCount();row++)
    	  {  
    		 list.clear();
    		 for(col=1;col<tempTestCases.getColumnCount();col++) 
    		 {
    			 list.add(String.valueOf(tempTestCases.getValueAt(row,col)));
    		 }	 
    		 MCDCTestCases.addRow(list.toArray()); 
    	  }
    	  
      }
	  
      private void generateCase(DefaultTableModel tc2,DefaultTableModel tc1,String operator) {
   		 
   		 List<String> list = new ArrayList<String>();
   		
   	      for(int row1=0;row1<tc2.getRowCount();row1++) {
   	    	for(int row2=0;row2<tc1.getRowCount();row2++) {
          	  list.clear();
              list.add(Integer.toString(arrayCount));
              if((tc2.getValueAt(row1,tc2.getColumnCount()-1)=="T")&&(tc1.getValueAt(row2,tc1.getColumnCount()-1)=="F"))
              {
            	  list=testCases(tc2,tc1,row1, row2,list);
         	      if(operator.equals("|"))
         	    	  list.add("T");
         	      else list.add("F");
              }
              else if((tc2.getValueAt(row1,tc2.getColumnCount()-1)=="F")&&(tc1.getValueAt(row2,tc1.getColumnCount()-1)=="T"))
              {
            	  list=testCases(tc2,tc1,row1, row2,list);
          	      if(operator.equals("|"))
       	    	     list.add("T");
       	          else list.add("F");
              }
              else if ((tc2.getValueAt(row1,tc2.getColumnCount()-1)=="F")&&(tc1.getValueAt(row2,tc1.getColumnCount()-1)=="F")&& (operator.equals("|"))){  
            	  list=testCases(tc2,tc1,row1, row2,list);
           	      list.add("F");
   	           
              }
              else if ((tc2.getValueAt(row1,tc2.getColumnCount()-1)=="T")&&(tc1.getValueAt(row2,tc1.getColumnCount()-1)=="T")&& (operator.equals("&"))){ 
            	  list=testCases(tc2,tc1,row1, row2,list);
   	              list.add("T");
              }
   	         
   	       
   	         if (list.size() > tempTestCases.getColumnCount()) {
   	           for (int colName=tempTestCases.getColumnCount(); colName<list.size(); colName++) {
   	              tempTestCases.addColumn("Col " + colName);
   	           }
   	         }
   	        if(list.size()>1)
   	         tempTestCases.addRow(list.toArray());}  	
   	  }
   	     	       	      							
   	}//GENERATECASES
  
  private List<String> testCases(DefaultTableModel tc2,DefaultTableModel tc1,int row1,int row2,List<String> list)
  {
	  for(int col1=0; col1<tc2.getColumnCount()-1; col1++){
	         list.add(String.valueOf(tc2.getValueAt(row1,col1)));
	  }
	  for(int col2=0; col2<tc1.getColumnCount()-1; col2++){
	         list.add(String.valueOf(tc1.getValueAt(row2,col2)));
	  } 
	  return list;
  }
  
  /*private void display()
  {
	  JTable t = new JTable(MCDCTestCases);
	 //JTable t = new JTable(tempTestCases);
	  JScrollPane jsp = new JScrollPane(t);

	  // creates the results window
	  JFrame dispFrm = new JFrame();
	  dispFrm.setTitle("test3");
	  dispFrm.getContentPane().add(jsp);
	  dispFrm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	  // display the result window
	  dispFrm.pack();
	  dispFrm.setVisible(true);
  }*/
       
}//CLASS

