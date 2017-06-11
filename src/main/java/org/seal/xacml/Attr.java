package org.seal.xacml;

import java.util.ArrayList;

import org.wso2.balana.attr.xacml3.AttributeDesignator;

public class Attr {
	private String name;
	private String category;
	private String dataType;
	private ArrayList<String> domain;
	
	public Attr(AttributeDesignator attr){
		this.name = attr.getId().toString();
		this.category = attr.getCategory().toString();
		this.dataType = attr.getType().toString();
		this.domain = new ArrayList<String>();
	}
	
	public Attr(String name, String category, String dataType) {
		this.name = name;
		this.category = category;
		this.dataType = dataType;
		domain = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}
	
	public void addValue(String value){
		this.domain.add(value);
	}

	public ArrayList<String> getDomain() {
		return this.domain;
	}

	public String getCategory() {
		return this.category;
	}

	public String getDataType() {
		return this.dataType;
	}
	
	public void setDomain(String domain) {
		this.domain.clear();
		this.domain.add(domain);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void clearDomain() {
		this.domain.clear();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		Attr attr = (Attr)obj;
		if(attr.getName().equals(this.name) && attr.getCategory().equals(this.category) && attr.getDataType().equals(this.dataType)){
			return true;
		} else{
			return false;
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(name);
		buffer.append("\n");
		buffer.append(category);
		buffer.append("\n");
		buffer.append(dataType);
		buffer.append("\n");
		for (String value : domain) {
			buffer.append(value);
			buffer.append(", ");
		}
		return buffer.toString();
	}
}
