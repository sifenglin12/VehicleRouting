/**
 * 
 */
package edu.utexas.orie.cvrptw.instance;

import java.util.Date;

import edu.utexas.orie.cvrptw.output.TimeProcessor;

/**
 * @author Sifeng Lin
 *
 */
public class Warehouse extends Location {
	
	protected Date pullTimeStart;
	protected Date pullTimeEnd;
	
	/**
	 * @return the pullTimeStart
	 */
	public Date getPullTimeStart() {
		return pullTimeStart;
	}

	/**
	 * @return the pullTimeEnd
	 */
	public Date getPullTimeEnd() {
		return pullTimeEnd;
	}

	/**
	 * @return the pullTimeStart
	 */
	public double getPullTimeStartHour() {
		return TimeProcessor.getTimeInHour(pullTimeStart);
	}
	
	/**
	 * @return the pullTimeEnd
	 */
	public double getPullTimeEndHour() {
		return TimeProcessor.getTimeInHour(pullTimeEnd);
	}

	/**
	 * @param pullTimeStart the pullTimeStart to set
	 */
	public void setPullTimeStart(Date pullTimeStart) {
		this.pullTimeStart = pullTimeStart;
	}

	/**
	 * @param pullTimeEnd the pullTimeEnd to set
	 */
	public void setPullTimeEnd(Date pullTimeEnd) {
		this.pullTimeEnd = pullTimeEnd;
	}
	
	@Override
	public String toString() {
		return "WH[" + name +"]";
	}
	
	/**
	 * get the total number of warehouse slots available
	 * @return
	 */
	public int getNumWarehouseSlots(){
		return (int) Math.ceil(TimeProcessor.getDifferenceHour(pullTimeEnd, pullTimeStart)/0.5);
	}
}
