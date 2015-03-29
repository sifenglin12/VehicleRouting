package edu.utexas.orie.cvrptw.output;

import java.util.Date;

public class TimeProcessor {
	
	private static Date pullTimeStart = StringProcessor.getTime("2012-09-10 02:00:00");
	
	/**
	 * get the difference in hours between the two date
	 * dateOne - dateTwo
	 * @param dateOne
	 * @param dateTwo
	 * @return
	 */
	public static double getDifferenceHour(Date dateOne, Date dateTwo){
		long diff = dateOne.getTime() - dateTwo.getTime();
		return diff*1./1000./3600.;
	}
	
	public static double getTimeInHour(Date date){
		return getDifferenceHour(date, pullTimeStart);
	}

	/**
	 * @param pullTimeStart the pullTimeStart to set
	 */
	public static void setPullTimeStart(Date pullTimeStart) {
		TimeProcessor.pullTimeStart = pullTimeStart;
	}
	
	
}
