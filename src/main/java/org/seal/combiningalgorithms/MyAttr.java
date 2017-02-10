package org.seal.combiningalgorithms;

import java.util.ArrayList;

public class MyAttr {
	private String name;
	private String category;
	private String dataType;
	private ArrayList<String> domain;
	

	
	public MyAttr(String name, String category, String dataType) {
		this.name = name;
		this.category = category;
		this.dataType = dataType;
		domain = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	/*
	public void addValue(String value) {
		for (String v : domain) {
			if (v.equals(value)) {
				return;
			}
		}
		this.domain.add(value);
	}
	*/
	
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
		// System.out.println(domain);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void clearDomain() {
		this.domain.clear();
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
