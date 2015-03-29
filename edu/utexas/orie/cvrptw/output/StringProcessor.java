package edu.utexas.orie.cvrptw.output;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StringProcessor {
	/**
	 * get the string of a specific length, 
	 * with blank in the left if the length is not enough
	 * the tail is truncated if it is too long.
	 * @return
	 */
	public static String getString(double value, int length){
		DecimalFormat df =  new DecimalFormat("#.####");
		String str=df.format(value)+"";
		return getString(str, length);
	}
	
	public static String getString(int value, int length){
		String str=value+"";
		return getString(str, length);
	}
	
	public static String getString(String str, int length){
		length = length -1;
		if(str.length()>length){
			str=str.substring(0, length);
		}
		for(int i=str.length(); i<length; i++){
			str=str+" ";
		}
		return str+" ";
	}
	
	public static boolean isNumeric(String str){
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe){
			return false;
		}
		
		return true;
	}
	
	public static String getDate(){
		
		String date = "";
		GregorianCalendar now=new GregorianCalendar();
		date += now.get(Calendar.YEAR);
		date += ".";
		
		
		if(now.get(Calendar.MONTH)+1<10){
			date = date +"0";
		}
		date += (now.get(Calendar.MONTH)+1);
		date += ".";
		
		
		if(now.get(Calendar.DATE)<10){
			date = date +"0";
		}
		date += now.get(Calendar.DATE);
		date += ".";

			
		if(now.get(Calendar.HOUR_OF_DAY)<10){
			date = date +"0";	
		}
		date += now.get(Calendar.HOUR_OF_DAY);
		date += ".";
		
		if(now.get(Calendar.MINUTE)<10){
			date = date +"0";
		}
		date += now.get(Calendar.MINUTE);
		
		if(now.get(Calendar.SECOND)<10){
			date = date +"0";
		}
		date += now.get(Calendar.SECOND);

		return date;
		
	}
	
	/**
	 * given a time string, get the time information out of it
	 * @param timeString
	 * @return the date format
	 */
	public static Date getTime(String timeString){
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatter.parse(timeString);
		} catch (ParseException e) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				return formatter.parse(timeString);
			} catch (ParseException e1) {
				
				try{
					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
					return formatter.parse(timeString);
					
				} catch (ParseException e2) {
					e1.printStackTrace();
				}
				
			}
		}
		
		return null;
	}
	
}
