/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Jennifer Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package ca.mcgill.sable.soot.launching;

import java.util.*;
//import ca.mcgill.sable.soot.util.*;

/**
 * @author jlhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
public class SootSavedConfiguration {

	private HashMap config;
	private String name;
	private String saved;
	private ArrayList saveArray;
	private ArrayList runArray;
	private HashMap eclipseDefs;

	
	private static final String SPACE = " ";
	private static final String COLON = ":";
	private static final String DASH = "--";
	
	/**
	 * Constructor for SootSavedConfiguration.
	 */
	public SootSavedConfiguration(String name, HashMap config) {
		setName(name);
		setConfig(config);
	}
	
	/**
	 * Constructor for SootSavedConfiguration.
	 */
	public SootSavedConfiguration(String name, String [] saveArray) {
		setName(name);
		
		setSaveArray(new ArrayList());
		for (int i = 0; i < saveArray.length; i++){
			getSaveArray().add(saveArray[i]);
		}
	}
	
	
	/**
	 * Constructor for SootSavedConfiguration.
	 */
	public SootSavedConfiguration(String name, String saved) {
		setName(name);
		setSaved(saved);
	}
	
	// same as before (removes defs from HashMap)
	private void removeEclipseDefs(){
		if (getEclipseDefs() == null) return;
		//System.out.println("removing eclipse defs");
		Iterator it = getEclipseDefs().keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			//System.out.println("trying to remove: "+key);
			//System.out.println("test to remove: "+key);
			if (getConfig().containsKey(key)) {
				String needsToMatch = "";
				Object val = getEclipseDefs().get(key);
				if (val instanceof String){
					needsToMatch = (String)val;
				}
				else if (val instanceof ArrayList){
					Iterator it2 = ((ArrayList)val).iterator();
					while (it2.hasNext()){
						if (needsToMatch.equals("")){
							needsToMatch = (String)it2.next();
						}
						else {
							needsToMatch = needsToMatch + "\r\n" + (String)it2.next();
						}
					}
				}
				/*if (getConfig().get(key).equals(val)){
					System.out.println("removing: "+key);
					getConfig().remove(key);
				}*/
				//System.out.println("needs to match: "+needsToMatch);
				if (getConfig().get(key).equals(needsToMatch) || getConfig().get(key).equals(val)) {
					//System.out.println("removing: "+key);
					getConfig().remove(key);
				}
			}
			else {
				getConfig().put(key, getOppositeVal(getEclipseDefs().get(key)) );
			}
		}
		//Iterator temp = getConfig().entrySet().iterator();
		//while (temp.hasNext()) {
		//	System.out.println("Testing : "+temp.next().toString());
		//}
	}
	
	private Object getOppositeVal(Object val) {
		if (val instanceof Boolean) {
			if (((Boolean)val).booleanValue()) return new Boolean(false);
			else return new Boolean(true);
		}
		else {
			return "";
		}
	}
	
	// will use addEclipseDefs to Array instead
	private void addEclipseDefs() {
		//System.out.println("adding eclipse defs");
		if (getEclipseDefs() == null) return;
		Iterator it = getEclipseDefs().keySet().iterator();
		//System.out.println("before adding saved is: "+getSaved());
		StringBuffer tempSaved = new StringBuffer(getSaved());
		while (it.hasNext()) {
			String key = (String)it.next();
			//System.out.println("key in add Eclipse Defs: "+key);
			//System.out.println("going to add : "+key);
			if (getSaved().indexOf((DASH+key)) != -1) {
				// already there don't add (implies user changed val)	
			}
			else {
				Object val = getEclipseDefs().get(key);
				if (val instanceof String){
				
					String res = (String)val;
					tempSaved.append(SPACE);
					tempSaved.append(DASH);
					tempSaved.append(key);
					tempSaved.append(SPACE);
					tempSaved.append(val);
					tempSaved.append(SPACE);
				}
				else {
					Iterator it2 = ((ArrayList)val).iterator();
					while (it2.hasNext()){
						String nextVal = (String)it2.next();
						tempSaved.append(SPACE);
						tempSaved.append(DASH);
						tempSaved.append(key);
						tempSaved.append(SPACE);
						tempSaved.append(nextVal);
						tempSaved.append(SPACE);
					}
				}
				
			}
		}
		setSaved(tempSaved.toString());
	}
	
	// will use this one in future and not addEclipseDefs
	private void addEclipseDefsToArray() {
		//System.out.println("adding eclipse defs to array");
		if (getEclipseDefs() == null) return;
		Iterator it = getEclipseDefs().keySet().iterator();
		//System.out.println("before adding saved is: "+getSaved());
		//StringBuffer tempSaved = new StringBuffer(getSaved());
		//ArrayList tempSaved = new ArrayList();
		//tempSaved.addAll(getSaveArray());
		while (it.hasNext()) {
			String key = (String)it.next();
			//System.out.println("going to add : "+key);
			if (getSaveArray().contains(DASH+key)) {
				// already there don't add (implies user changed val)	
			}
			else {
				Object val = getEclipseDefs().get(key);
				//System.out.println("val: "+val);
				if (val instanceof String){

					String res = (String)val;
					getSaveArray().add(DASH+key);
					getSaveArray().add(res);
					
				}
				else if (val instanceof Boolean){
					getSaveArray().add(DASH+key);
					getSaveArray().add(val.toString());
				}
				/*else {
					Iterator it2 = ((ArrayList)val).iterator();
					while (it2.hasNext()){
						String nextVal = (String)it2.next();
						getSaveArray().add(DASH+key);
						getSaveArray().add(nextVal);
					}
				}*/
				// add it to end
				/*getSaveArray().add(DASH+key);
				getSaveArray().add(getEclipseDefs().get(key).toString());
				*/
				//tempSaved.append(SPACE);
				//tempSaved.append(DASH);
				//tempSaved.append(key);
				//tempSaved.append(SPACE);
				//tempSaved.append(getEclipseDefs().get(key).toString());
				//tempSaved.append(SPACE);
				//System.out.println("added : "+key);
			}
		}
		//setSaved(tempSaved.toString());
	
	}
	
	public HashMap toHashMapFromArray(){
		addEclipseDefsToArray();
		HashMap config = new HashMap();
		BitSet bits = new BitSet(getSaveArray().size());
		for (int i = 0; i < getSaveArray().size(); i++){
			if (((String)getSaveArray().get(i)).indexOf("--") != -1){
				bits.set(i);
			}
		}
		int counter = 0;
		
		//System.out.println(getSaveArray().size());
		while (counter < getSaveArray().size()){
			//System.out.println(getSaveArray().get(counter)+" "+bits.get(counter));
			if ((bits.get(counter+2)) || ((counter+2) >= getSaveArray().size())){
				// non phase opt
				// if key is already in map val = val + \n\r newVal
				String key = ((String)getSaveArray().get(counter)).substring(2);
				String val = (String)getSaveArray().get(counter+1);
				if (config.get(key) != null){
					String tempVal = (String)config.get(key);
					tempVal = tempVal + "\r\n" + val;
					config.put(key, tempVal); 
				}
				else {
					config.put(key,val);
				}
				counter = counter + 2;
			}
			else if ((bits.get(counter+3)) || ((counter+3) >= getSaveArray().size())){
				// phase opt
				String key = getSaveArray().get(counter)+SPACE+getSaveArray().get(counter+1);
				StringTokenizer valTemp = new StringTokenizer((String)getSaveArray().get(counter+2), ":");
				key = key+SPACE+valTemp.nextToken();
				String val = valTemp.nextToken();
				config.put(key.substring(2), val);
				counter = counter + 3;
			}
			//System.out.println(counter);
		}
		
		return config;
		
	}
	
	// goes from save String to HashMap
	public HashMap toHashMap() {
		
		// first add eclipse defs
		addEclipseDefs();
		HashMap config = new HashMap();
		String temp = getSaved();
		temp = temp.replaceAll("--", "&&");
		//System.
		StringTokenizer st = new StringTokenizer(temp, "&&");
		while (st.hasMoreTokens()) {
			StringTokenizer next = new StringTokenizer((String)st.nextToken());
			switch (next.countTokens()) {
				case 2: {
					// simple key value 
					/*String simpleKey = next.nextToken();
					String simpleVal = next.nextToken();
					if ((simpleVal.equals("true")) || (simpleVal.equals("false"))) {
						config.put(simpleKey, new Boolean(simpleVal));
					}*/
					//else {
					config.put(next.nextToken(), next.nextToken());
					//}
					break;	
				}
				case 3: {
					// phase options
					String key = next.nextToken()+SPACE+next.nextToken();
					StringTokenizer valTemp = new StringTokenizer(next.nextToken(), ":");
					key = key+SPACE+valTemp.nextToken();
					String val = valTemp.nextToken();
					config.put(key, val);
					break;
				}
				default: {
					//unhandled
					break;
				}
			}
		}
		return config;
	}
	
	// goes from save Array to run Array -
	//will use this and not toRunString in future 
	public ArrayList toRunArray() {
		addEclipseDefsToArray();
		if (getRunArray() == null){
			setRunArray(new ArrayList());
		}
	
		Iterator it = getSaveArray().iterator();
		String lastKey = "";
		while (it.hasNext()){
			String test = (String)it.next();
			String spliter = "\r\n";
			if (test.indexOf("\r\n") != -1){
				spliter = "\r\n";
			}
			else if (test.indexOf('\n') != -1){
				spliter = "\n";
			}
			//else if (test.indexOf(' ') != -1){
			//	System.out.println("test with space: "+test);
			//	spliter = " ";
			//}
			//System.out.println("char at 2: "+test.charAt(2));
			//System.out.println("test in toRunArray: "+test);
			if (test.equals("true")){
				// don't send 
			}
			else if (test.equals("false")){
				// don't send and also don't send key 
				int index = getRunArray().size() - 1;
				getRunArray().remove(index);
			}
			else if (test.indexOf(spliter) != -1){
				//System.out.println("test has newline");
				String [] tokens = test.split(spliter);
				getRunArray().add(tokens[0]);
				
				//System.out.println("added "+tokens[0]);
				for (int i = 1; i < tokens.length; i++){
				//StringTokenizer st = new StringTokenizer(test, "\r\n");
				//getRunArray().add(st.nextToken());
				//while (st.hasMoreTokens()){
					getRunArray().add(lastKey);
					getRunArray().add(tokens[i]);
					//System.out.println("added "+tokens[i]);
				}
			}
			/*else if (test.indexOf("\n") != -1){
				String [] tokens = test.split("\n");
				getRunArray().add(tokens[0]);
				for (int i = 1; i < tokens.length; i++){
					getRunArray().add(lastKey);
					getRunArray().add(tokens[i]);
				}
			}*/
			else {
				getRunArray().add(test);
			}
			lastKey = test;
		}
		return getRunArray();	
	}
	
	// goes from save String to run String
	public String toRunString() {
		
		// first add eclipse defs
		addEclipseDefs();
		StringBuffer toRun = new StringBuffer(); 
		String temp = getSaved();
		temp = temp.replaceAll("--", "&&");
		//System.
		StringTokenizer st = new StringTokenizer(temp, "&&");
		while (st.hasMoreTokens()) {
			StringTokenizer next = new StringTokenizer((String)st.nextToken());
			switch (next.countTokens()) {
				case 2: {
					String key = next.nextToken();
					//System.out.print("key: "+key);
					String val = next.nextToken();
					val = val.trim();
					//System.out.println("value: /"+val+"/");
					
					// if true its a boolean and want to send
					if (val.equals("true")) {
						toRun.append(DASH);
						toRun.append(key);
						toRun.append(SPACE);
					}
					// if false its a boolean but don't want to send
					else if (val.equals("false")) {
					}
					// non boolean
					else {
						toRun.append(DASH);
						toRun.append(key);
						toRun.append(SPACE);
						toRun.append(val);
						toRun.append(SPACE);
					}
					break;	
				}
				case 3: {
					// phase options
					String key = next.nextToken()+SPACE+next.nextToken()+SPACE+next.nextToken();
					///StringTokenizer valTemp = new StringTokenizer(next.nextToken(), ":");
					//System.out.println("valTemp: "+valTemp.toString());
					//key = key+SPACE+valTemp.nextToken();
					//System.out.print("key: "+key);
					//String val = valTemp.nextToken();
					//System.out.println("value: "+val);
					toRun.append(DASH);
					toRun.append(key);
					toRun.append(SPACE);
					//toRun.append(val);
					//toRun.append(SPACE);
					break;
				}
				default: {
					//unhandled
					break;
				}
			}
		}
		return toRun.toString();
	}
	
	// goes from HashMap to ArrayList -> will use this and
	// not toSaveString in future
	public ArrayList toSaveArray() {
		if (getSaveArray() == null) {
			setSaveArray(new ArrayList());
		}
		
		removeEclipseDefs();
		Iterator keysIt = getConfig().keySet().iterator();
		while (keysIt.hasNext()) {
			String key = (String)keysIt.next();
			StringTokenizer st = new StringTokenizer(key);
			//System.out.println("about to find val");
			Object val = getConfig().get(key);
			System.out.println("val: "+val);
			
			//System.out.println("found val");
			switch(st.countTokens()) {
				case 1: {
					String aliasName = st.nextToken();
					if (aliasName.equals("sootMainClass")) continue;
					//System.out.println("adding: "+aliasName);
					if (val instanceof String) {
						String test = (String)val;
						if ((test == null) |(test.length() == 0)) { System.out.println("continuing" ); continue;}
					}
					getSaveArray().add(DASH+aliasName);
					//toSave.append(DASH);
					//String aliasName = st.nextToken();
					//toSave.append(aliasName);
					//toSave.append(SPACE);
					if (val instanceof Boolean) {
						getSaveArray().add(val.toString());	
					}	
					else if (val instanceof String) {
						String test = (String)val;
						String spliter = "\r\n";
						if (test.indexOf("\r\n") != -1){
							spliter = "\r\n";
						}
						else if (test.indexOf('\n') != -1){
							spliter = "\n";
						}
						//System.out.println("test in toSaveArray: "+test);
						//if (test.equals("true")){
							// don't send 
						//}
						if (test.indexOf(spliter) != -1){
							//System.out.println("test has newline");
							String [] tokens = test.split(spliter);
							getSaveArray().add(tokens[0]);
				
							for (int i = 1; i < tokens.length; i++){
								//StringTokenizer st = new StringTokenizer(test, "\r\n");
								//getRunArray().add(st.nextToken());
								//while (st.hasMoreTokens()){
									getSaveArray().add(DASH+aliasName);
									getSaveArray().add(tokens[i]);
									//System.out.println("added "+tokens[i]);
							}
						}
						else {
						/*if (((String)val).indexOf("\r\n") != -1){
							StringTokenizer listOptTokenizer = new StringTokenizer((String)val, "\r\n");
							while (listOptTokenizer.hasMoreTokens()){
								String next = listOptTokenizer.nextToken();
								getSaveArray().add(next);
								if (listOptTokenizer.hasMoreTokens()){
									getSaveArray().add(DASH+aliasName);
									//toSave.append(aliasName);
									//toSave.append(SPACE);		
								}
							}
							System.out.println("LISTOPT: String contains newline");
						}
						else {*/
							//System.out.println("added val: "+val);
							getSaveArray().add(val);
						}
						//}
					}			
					//toSave.append(SPACE);
					break;
				}
				case 3: {
					getSaveArray().add(DASH+st.nextToken());
					//toSave.append(st.nextToken());
					//toSave.append(SPACE);
					getSaveArray().add(st.nextToken());
					//toSave.append(SPACE);
					String realVal = st.nextToken()+COLON;
					//getSaveArray().add(st.nextToken());
					//toSave.append(COLON);
					if (val instanceof Boolean) {
						realVal = realVal + val.toString();	
					}	
					else if (val instanceof String) {
						realVal = realVal + val;
					}			
					getSaveArray().add(realVal);
					break;
				}
				default: {
					//unhandled non option
					break;
				}
			}
		}
		
		return getSaveArray();
	}
	
	// goeas from HashMap to String - will use toSaveArray in future
	public String toSaveString() {
		
		// first remove eclipse defs
		removeEclipseDefs();
		//System.out.println("removed eclipse defs");
		StringBuffer toSave = new StringBuffer();
		Iterator keysIt = getConfig().keySet().iterator();
		while (keysIt.hasNext()) {
			String key = (String)keysIt.next();
			StringTokenizer st = new StringTokenizer(key);
			//System.out.println("about to find val");
			Object val = getConfig().get(key);
			//System.out.println("found val");
			switch(st.countTokens()) {
				case 1: {
					toSave.append(DASH);
					String aliasName = st.nextToken();
					toSave.append(aliasName);
					toSave.append(SPACE);
					if (val instanceof Boolean) {
						toSave.append(val.toString());	
					}	
					else if (val instanceof String) {
						if (((String)val).indexOf("\n") != -1){
							StringTokenizer listOptTokenizer = new StringTokenizer((String)val, "\n");
							while (listOptTokenizer.hasMoreTokens()){
								String next = listOptTokenizer.nextToken();
								toSave.append(next);
								if (listOptTokenizer.hasMoreTokens()){
									toSave.append(DASH);
									toSave.append(aliasName);
									toSave.append(SPACE);		
								}
							}
							//System.out.println("LISTOPT: String contains newline");
						}
						else {
							toSave.append(val);
						}
					}			
					toSave.append(SPACE);
					break;
				}
				case 3: {
					toSave.append(DASH);
					toSave.append(st.nextToken());
					toSave.append(SPACE);
					toSave.append(st.nextToken());
					toSave.append(SPACE);
					toSave.append(st.nextToken());
					toSave.append(COLON);
					if (val instanceof Boolean) {
						toSave.append(val.toString());	
					}	
					else if (val instanceof String) {
						toSave.append(val);
					}			
					toSave.append(SPACE);
					break;
				}
				default: {
					//unhandled non option
					break;
				}
			}
			
			
		}
		setSaved(toSave.toString());
		//System.out.println("about to return toSave string: "+getSaved());
		return toSave.toString();
	}
	
	/*public String toSaveString() {
		
		Iterator it = getConfig().entrySet().iterator();
		System.out.println(getConfig().size());
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		StringBuffer toSave = new StringBuffer();
		
		
		Iterator keysIt = getConfig().keySet().iterator();
		//Iterator valsIt = getConfig().values().iterator();
		
		while (keysIt.hasNext()) {// && valsIt.hasNext()) {
			Object keyTemp = keysIt.next();
			//System.out.println(keyTemp+" is a "+keyTemp.getClass().toString());
			if (keyTemp instanceof String) {
				String key = (String)keyTemp;
				System.out.println("key is: "+key);
				Object valTemp = getConfig().get(key);
				String separator = SootCmdFormat.SPACE;
				String result = "";
				if (valTemp instanceof SootCmdFormat) {
					if (((SootCmdFormat)valTemp).getVal() instanceof String) {
						result = (String)((SootCmdFormat)valTemp).getVal();
						System.out.println("String result: "+result);
						//separator = ((SootCmdFormat)valTemp).getSeparator();
				
						//if ((result != null) && (result.length() != 0)) {
							/*toSave.append(DASH);
							toSave.append(key);
							toSave.append(separator);
							toSave.append(result);
							toSave.append(SPACE);	
						//}
					}
					else if (((SootCmdFormat)valTemp).getVal() instanceof Boolean) {
						result = ((Boolean)((SootCmdFormat)valTemp).getVal()).toString();
						System.out.println("bool result: "+result);
						//separator = ((SootCmdFormat)valTemp).getSeparator();
						/*if (separator.equals(SootCmdFormat.SPACE) && ((Boolean)((SootCmdFormat)valTemp).getVal()).booleanValue()) {
							System.out.println("bool opts that are true: "+key);
							toSave.append(DASH);
							toSave.append(key);
							toSave.append(separator);
						}
						/*else if (separator.equals(SootCmdFormat.SPACE) && !((Boolean)((SootCmdFormat)valTemp).getVal()).booleanValue()) {
							System.out.println("bool opts that are false: "+key);
						}*/
						/*else {
							toSave.append(DASH);
							toSave.append(key);
							toSave.append(separator);
							toSave.append(result);
							toSave.append(SPACE);
						}*/
					//}
					/*else {
						result = "";		
						System.out.println("no result: "+result);
					}
					
					separator = ((SootCmdFormat)valTemp).getSeparator();
				
				
					toSave.append(DASH);
					toSave.append(key);
					toSave.append(separator);
					toSave.append(result);
					toSave.append(SPACE);	
				}
			}
				
		}
		
		return toSave.toString();
	}*/

	/**
	 * Returns the config.
	 * @return HashMap
	 */
	public HashMap getConfig() {
		return config;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the config.
	 * @param config The config to set
	 */
	public void setConfig(HashMap config) {
		this.config = config;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the saved.
	 * @return String
	 */
	public String getSaved() {
		return saved;
	}

	/**
	 * Sets the saved.
	 * @param saved The saved to set
	 */
	public void setSaved(String saved) {
		this.saved = saved;
	}

	

	/**
	 * Returns the eclipseDefs.
	 * @return HashMap
	 */
	public HashMap getEclipseDefs() {
		return eclipseDefs;
	}

	/**
	 * Sets the eclipseDefs.
	 * @param eclipseDefs The eclipseDefs to set
	 */
	public void setEclipseDefs(HashMap eclipseDefs) {
		this.eclipseDefs = eclipseDefs;
	}

	/**
	 * @return
	 */
	public ArrayList getSaveArray() {
		return saveArray;
	}

	/**
	 * @param list
	 */
	public void setSaveArray(ArrayList list) {
		saveArray = list;
	}

	/**
	 * @return
	 */
	public ArrayList getRunArray() {
		return runArray;
	}

	/**
	 * @param list
	 */
	public void setRunArray(ArrayList list) {
		runArray = list;
	}

}