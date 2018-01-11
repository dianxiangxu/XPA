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
				if(j != attrs.size() - 1) {
					request.append(getCategoryHead(attrs.get(j)));
				} else {
					request.append(getIDCategoryHead());
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
}
