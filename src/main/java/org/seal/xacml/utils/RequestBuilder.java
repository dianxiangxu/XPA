package org.seal.xacml.utils;

import java.util.ArrayList;
import java.util.List;

import org.seal.xacml.Attr;

public class RequestBuilder {
	public static String buildRequest(List<Attr> attrs) {
		StringBuilder request = new StringBuilder();
		request.append(getRequestHead());
		List<String> list = new ArrayList<String>();
		for (int j = 0; j < attrs.size(); j++) {
			if(list.contains(attrs.get(j).getCategory().toString())){
				continue;
			} else {
				list.add(attrs.get(j).getCategory().toString());
				request.append(getCategoryHead(attrs.get(j)));
			}
			for (int k = j; k < attrs.size(); k++) {
				if (attrs.get(k).getCategory().equals(attrs.get(j).getCategory())) {
					request.append(getRequest(attrs.get(k), attrs.get(k).getDomain().get(0)));
				}
			}
			request.append(getCategoryEnd());
		}
		request.append(getRequestEnd());
		return request.toString();
	}
	
	public static String buildIDRequest(List<Attr> attrs) {
		StringBuilder request = new StringBuilder();
		request.append(getRequestHead());
		List<String> list = new ArrayList<String>();
		for (int j = 0; j < attrs.size(); j++) {
			if(list.contains(attrs.get(j).getCategory().toString())){
				continue;
			} else {
				list.add(attrs.get(j).getCategory().toString());
				//if(j != attrs.size() - 1) {
				if(j ==0) {
					request.append(getIDCategoryHead());
						
				} else {
					request.append(getCategoryHead(attrs.get(j)));
					
				}
			}
			for (int k = j; k < attrs.size(); k++) {
				if (attrs.get(k).getCategory().equals(attrs.get(j).getCategory())) {
					request.append(getRequest(attrs.get(k), attrs.get(k).getDomain().get(0)));
				}
			}
			request.append(getCategoryEnd());
		}
		request.append(getRequestEnd());
		return request.toString();
	}
	
	public static String buildIDRequest(List<Attr> attrs,List<Attr> lAttrs) {
		StringBuilder request = new StringBuilder();
		request.append(getRequestHead());
		List<String> list = new ArrayList<String>();
		boolean injected = false;
		for (int j = 0; j < attrs.size(); j++) {
			if(list.contains(attrs.get(j).getCategory().toString())){
				continue;
			} else {
				list.add(attrs.get(j).getCategory().toString());
				//if(j != attrs.size() - 1) {
				if(!injected) {
					if(lAttrs.contains(attrs.get(j))) {
						request.append(getIDCategoryHead());
						injected = true;	
					} else
						request.append(getCategoryHead(attrs.get(j)));

				} else {
					request.append(getCategoryHead(attrs.get(j)));
					
				}
			}
			for (int k = j; k < attrs.size(); k++) {
				if (attrs.get(k).getCategory().equals(attrs.get(j).getCategory())) {
					request.append(getRequest(attrs.get(k), attrs.get(k).getDomain().get(0)));
				}
			}
			request.append(getCategoryEnd());
		}
		request.append(getRequestEnd());
		return request.toString();
	}
	
	public static String buildAllIDRequest(List<Attr> attrs) {
		StringBuilder request = new StringBuilder();
		request.append(getRequestHead());
		List<String> list = new ArrayList<String>();
		for (int j = 0; j < attrs.size(); j++) {
			//if(list.contains(attrs.get(j).getCategory().toString())){
			//	continue;
			//} else {
				list.add(attrs.get(j).getCategory().toString());
				//if(j != attrs.size() - 1) {
				//	request.append(getCategoryHead(attrs.get(j)));
				//} else {
					request.append(getIDCategoryHead());
				//}
			//}
			for (int k = j; k < attrs.size(); k++) {
				if (attrs.get(k).getCategory().equals(attrs.get(j).getCategory())) {
					request.append(getRequest(attrs.get(k), attrs.get(k).getDomain().get(0)));
					//request.append(getIDRequest(attrs.get(k), "Indeterminate"));
				}
			}
			request.append(getCategoryEnd());
		}
		request.append(getRequestEnd());
		return request.toString();
	}
	
	public static List<StringBuilder> buildAllIDRequest2(List<Attr> attrs) {
		List<StringBuilder> requests = new ArrayList<StringBuilder>();
		
		List<String> list = new ArrayList<String>();
		for (int j = 0; j < attrs.size(); j++) {
			if(list.contains(attrs.get(j).getCategory().toString())){
				continue;
			} else {
				list.add(attrs.get(j).getCategory().toString());
				requests.add(new StringBuilder(getRequestHead()));
				
			}
		}
		boolean ind = false;
		int counter = 0;
		int count = list.size();
		list = new ArrayList<String>();
		
		for (int j = 0; j < attrs.size(); j++) {
			if(list.contains(attrs.get(j).getCategory().toString())){
				continue;
			} else {
				for(int index = 0;index<count;index++) {
				StringBuilder request = requests.get(index);
				
				list.add(attrs.get(j).getCategory().toString());
				//if(j != attrs.size() - 1) {
				//	request.append(getCategoryHead(attrs.get(j)));
				//} else {
					if(index==counter) {
						request.append(getIDCategoryHead());
					}else {
						request.append(getCategoryHead(attrs.get(j)));
					}
					//}
			
				for (int k = j; k < attrs.size(); k++) {
					if (attrs.get(k).getCategory().equals(attrs.get(j).getCategory())) {
						request.append(getRequest(attrs.get(k), attrs.get(k).getDomain().get(0)));
						//request.append(getIDRequest(attrs.get(k), "Indeterminate"));
					}
				}
				request.append(getCategoryEnd());
				
				}
				counter ++;
				
			}
			
		}
		for(StringBuilder sb:requests) {
			sb.append(getRequestEnd());
			
		}
		return requests;
	}
	
	private static String getRequestHead() {
		return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n";
	}

	private static String getRequestEnd() {
		return "</Request>";
	}

	private static String getCategoryHead(Attr attr) {
		return "<Attributes Category=\"" + attr.getCategory() + "\">\n";
	}
	
	private static String getIDCategoryHead() {
		return "<Attributes Category=\"" + MiscUtil.randomAttribute() + "\">\n";
	}

	private static String getCategoryEnd() {
		return "</Attributes>\n";
	}
	
	private static String getRequest(Attr attr, String value) {
		StringBuilder req = new StringBuilder("<Attribute AttributeId=");
		req.append("\"" + attr.getName() + "\" IncludeInResult = \"false\">\n");
		req.append("<AttributeValue DataType=");
		req.append("\"" + attr.getDataType() + "\">" + value + "</AttributeValue>\n");
		req.append("</Attribute>\n");
		return req.toString();
	}
	private static String getIDRequest(Attr attr, String value) {
		StringBuilder req = new StringBuilder("<Attribute AttributeId=");
		req.append("\"" + attr.getName() + "\" IncludeInResult = \"false\">\n");
		req.append("<AttributeValue DataType=");
		req.append("\"" + attr.getDataType() + "\">" + value + "</AttributeValue>\n");
		req.append("</Attribute>\n");
		return req.toString();
	}
}
