package org.seal.combiningalgorithms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Call_Z3str {

	/*
	 * public static void main(String args[]) { Call_Z3str cz = new
	 * Call_Z3str(); MyAttr myattr = new MyAttr("name", "category", "integer");
	 * myattr.addFunction("greater"); myattr.addValue("10");
	 * myattr.addFunction("less"); myattr.addValue("20");
	 * 
	 * cz.buildZ3Input(myattr);
	 * 
	 * cz.buildZ3Output(); try { System.out.println(cz.getValue(false)); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace();
	 * } }
	 */
	public void buildZ3Input(String input, HashMap nameMap, HashMap typeMap) {
		StringBuffer sb = new StringBuffer();
		String[] lines = input.split("\n");
		for (String s : lines) {
			if (!s.trim().equals("")) {
				StringBuffer subsb = new StringBuffer();
				subsb.append(s);
				subsb.insert(0, "(assert ");
				subsb.append(")" + "\n");
				sb.append(subsb);
			}
		}
		Iterator iter = nameMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String name = entry.getValue().toString();
			String type = typeMap.get(entry.getValue().toString()).toString();
			sb.insert(0, "(declare-variable " + name + " " + type + ")\n");
		}
		sb.append("(check-sat)" + "\n");
		sb.append("(get-model)" + "\n");
		//PolicyX.printTrack2(sb.toString() + "\n-----------------\n");

		try {
			FileWriter fw = new FileWriter("./Z3_input");
			fw.write(sb.toString());
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void buildZ3Output() {
		Runtime run = Runtime.getRuntime();
		try {
			//replace with path to your z3-str directory
			Process p = run
					.exec("Z3-str/Z3-str.py -f ./Z3_input");
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			StringBuffer tmpTrack = new StringBuffer();
			String lineStr;
			FileWriter fw = new FileWriter("./Z3_output");
			while ((lineStr = inBr.readLine()) != null) {
				fw.write(lineStr + "\n");
				tmpTrack.append(lineStr + "\n");
			}
			//PolicyX.printTrack2(tmpTrack.toString() + "\n??????????????????????????\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkConflict() {
		FileReader fr = null;
		try {
			fr = new FileReader("./Z3_output");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String s;
		try {
			if ((s = br.readLine()) != null) {
				if (s.equals("* v-ok")) {
					fr.close();
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

    public void getValue(ArrayList<MyAttr> collector, HashMap nameMap)
            throws IOException {
   
        FileReader fr = new FileReader("./Z3_output");
        BufferedReader br = new BufferedReader(fr);
        String s;
        while ((s = br.readLine()) != null) {
            String[] data = s.split(" ");
            int preValueIndex = 0;
            while (preValueIndex<data.length && !data[preValueIndex].equals("->")) {
                preValueIndex++;
            }
            if (data.length > preValueIndex+1) {
                Iterator iter = nameMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    if (data[0].equals(entry.getValue())) {
                        for (MyAttr myattr : collector) {
                            if (myattr.getName().equals(entry.getKey())) {
                                String value = "";
                                for (int l = preValueIndex+1; l < data.length -1 ; l++) {
                                    value = value + data[l].toString() + " ";
                                }
                                value = value + data[data.length-1].toString();
                                value = value.replaceAll("\"", "");

                                myattr.setDomain(value);
                            }
                        }
                    }
                }
            }
        }
        fr.close();

        // value should be added after checking type
        for (MyAttr myattr : collector) {
            if (myattr.getDomain().isEmpty()) {
                myattr.addValue("0");
            }
        }
           
    }
    
	
	private void printTrack(String request){
		try{
		FileWriter fw = new FileWriter("./outtrack.txt", true);
		fw.write(request);
		fw.close();
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
