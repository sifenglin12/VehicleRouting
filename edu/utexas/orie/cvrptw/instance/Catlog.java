package edu.utexas.orie.cvrptw.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public enum Catlog {
	grocery(3), refrigerated(2), frozen(1), salvage(0);
	
	private int value;
	
	private Catlog(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	private static ArrayList<String> groceryTypes;
	private static ArrayList<String> frozenTypes;
	
	/**
	 * check if the two catlogs need separation
	 * @param catlogOne
	 * @param catlogTwo
	 */
	public static boolean needSeparation(Catlog catlogOne, Catlog catlogTwo){
		
		if(catlogOne.equals(grocery)){
			return catlogTwo.equals(refrigerated) || catlogTwo.equals(frozen);
		}
		else if(catlogTwo.equals(grocery)){
			return catlogOne.equals(refrigerated) || catlogOne.equals(frozen);
		}
		
		return false;
		
	}
	
	/**
	 * read the product type
	 * @throws IOException 
	 */
	public static void readProductType(String productTypeFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(productTypeFile));
		String line = in.readLine();
		
		groceryTypes = new ArrayList<>();
		frozenTypes = new ArrayList<>();
		
		while(line!=null){
			String entries[] = line.split(":");
			if("Grocery".equals(entries[0])){
				for(String type : entries[1].split(", ")){
					groceryTypes.add(type.trim());
				}
			}
			else if("Frozen".equals(entries[0])){
				for(String type : entries[1].split(", ")){
					frozenTypes.add(type.trim());
				}
			}
			
			line = in.readLine();
		}
		
		in.close();
	}
	
	/**
	 * get the corresponding catlog, given the order type
	 * @param orderType
	 * @return
	 */
	public static Catlog getDeliveryCatlog(String orderType){
		if(groceryTypes.contains(orderType)){
			return grocery;
		}
		else if(frozenTypes.contains(orderType)){
			return frozen;
		}
		else{
			return refrigerated;
		}
	}
	
	
	
}
