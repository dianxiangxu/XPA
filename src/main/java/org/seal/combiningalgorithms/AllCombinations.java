package org.seal.combiningalgorithms;
/*
 * Author: Dianxiang Xu @ Dakota State University
 */


import java.util.ArrayList;

public class AllCombinations {
	ArrayList<int[]> combos = new ArrayList<int[]>();

	public  ArrayList<int[]> getCombinations(int[] sizes){
		
		long count = 0;
		int[] indices = new int[sizes.length];
		for (int column=0; column<indices.length; column++){
			indices[column]=0;
		}
		int pointer = sizes.length-1;
		do {
			count++;
			//printCombination(count, indices);
			returnCombination(count, indices);

			pointer = sizes.length-1;
			if (indices[pointer]<sizes[pointer]-1)
				indices[pointer]+=1;
			else {
				while (pointer>=0 && indices[pointer]>=sizes[pointer]-1){
					indices[pointer]=0;
					pointer-=1;
				} 
				if (pointer>=0 && indices[pointer]<sizes[pointer]-1)
					indices[pointer]+=1;
			}
		} while (pointer>=0);
		
		return combos;
	}
	
	private int getCombinationSize(int[] sizes){
		int numberOfCombinations =1;
		for (int i=0; i<sizes.length; i++)
			numberOfCombinations*=sizes[i];
		return numberOfCombinations;
	}
	
	public void printCombination(long count, int[] combination){
		System.out.print("\n"+count+": ");
		for (int column=0; column<combination.length; column++)
			System.out.print(" "+combination[column]);
	}
	
	public void returnCombination(long count, int[] combination){
		int[] test1 = new int[combination.length];
		for (int column=0; column<combination.length; column++)
		{	
			test1[column] = combination[column];			            
		}
		combos.add(test1);
	}
	/*
   public static void main(String[] args) {
//	   int[] sizes = {16, 16, 16, 16, 16, 16, 16};
//	   int[] sizes = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
//	   int[] sizes = {3, 3, 5, 5, 5, 5};
//	   int[] sizes = {5, 5};
	   int[] sizes = {3, 3, 3, 3};
	   AllCombinations ac = new AllCombinations();
		System.out.print("Total combinations: "+ac.getCombinationSize(sizes));
		ac.getCombinations(sizes);
		for(int[] a : ac.getCombinations(sizes)){
		ac.printCombination(30,a);
		}		
   }
   */
}
