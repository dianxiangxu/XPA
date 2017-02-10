package org.seal.gui;

public  class ResultConverter {

	public static String ConvertResult(String num){
		if(num.equals(0)){
			return "Permit";
		}else if(num.equals("1")){
			return "Deny";
		}else if(num.equals("2")){
			return "Indterminate";
		}else if(num.equals("3")){
			return "NotApplicable";
		}else if(num.equals("4")){
			return "ID";
		}else if(num.equals("5")){
			return "IP";
		}else if(num.equals("6")){
			return "IDP";
		}
		return "";
	}
	
	public static String ConvertResult(int num){
		if(num==0){
			return "Permit";
		}else if(num==1){
			return "Deny";
		}else if(num == 2){
			return "Indterminate";
		}else if(num ==3 ){
			return "NotApplicable";
		}else if(num ==4){
			return "ID";
		}else if(num==5){
			return "IP";
		}else if(num == 6){
			return "IDP";
		}
		return "";
	}
}
