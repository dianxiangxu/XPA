package org.seal.faultlocalization;

public class PolicyElementCoefficient implements Comparable<PolicyElementCoefficient> {
	private double coefficient;
	private int elementIndex;
	private int rank;
	
	public PolicyElementCoefficient(double coefficient, int elementIndex){
		this.coefficient = coefficient;
		this.elementIndex = elementIndex;
	}
	
 	public int compareTo(PolicyElementCoefficient other){
 		return Double.compare(other.coefficient, this.coefficient);
// 		if (approximateEqual(this.coefficient, other.coefficient))
// 			return 0;
// 		else  // to sort in reverse order
// 			return this.coefficient > other.coefficient? -1: 1;
  	}
 	
 	public double getCoefficient(){
 		return coefficient;
 	}
 	
 	public int getElementIndex(){
 		return elementIndex;
 	}
 	
 	public int getRank(){
 		return rank;
 	}
 	
 	public void setRank(int rank){
 		this.rank = rank;
 	}
 	
	public static boolean approximateEqual(double a, double b){
		return Math.abs(a-b)<0.0000000001;
	}

	public boolean approximateEqual(PolicyElementCoefficient other){
		return approximateEqual(coefficient, other.coefficient);
	}

}
