package edu.utexas.orie.cvrptw.instance;

import java.util.ArrayList;
import java.util.Date;

import edu.utexas.orie.cvrptw.output.*;


public class Store extends Location{
	
	public Store(String name, Date startTime, Date endTime, Date earlyTime,
			int information) {
		super();
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.earlyTime = earlyTime;
		this.information = information;
		this.deliveryOrders = new ArrayList<>();
		this.salvageOrders = new ArrayList<>();
	}
	
	/**
	 * corresponds to the start time entry in the input file.  It is given with respect to the start loading time
	 */
	Date startTime;  
	
	/**
	 * corresponds to the end time entry in the input file
	 */
	Date endTime;
	
	/**
	 * corresponds to the early time entry in the input file
	 */
	Date earlyTime;

	/**
	 * the first column in Windows table
	 */
	int information;

	/**
	 * the deliver order
	 */
	ArrayList<Order> deliveryOrders;
	
	/**
	 * the salvage order
	 */
	ArrayList<Order>salvageOrders;
	
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	
	/**
	 * get the earliest time, compared to the earliest starting time: 13:00
	 * @return
	 */
	public double getEarliestTimeHour(){
		return Math.max(TimeProcessor.getTimeInHour(startTime), 0);
	}

	/**
	 * get the latest time, compared to the earliest starting time: 13:00
	 * @return
	 */
	public double getLatestTimeHour(){
		return TimeProcessor.getTimeInHour(endTime);
	}
	
	/**
	 * @return the earlyTime
	 */
	public Date getEarlyTime() {
		return earlyTime;
	}

	/**
	 * @return the information
	 */
	public int getInformation() {
		return information;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param earlyTime the earlyTime to set
	 */
	public void setEarlyTime(Date earlyTime) {
		this.earlyTime = earlyTime;
	}

	/**
	 * @param information the information to set
	 */
	public void setInformation(int information) {
		this.information = information;
	}
	
	/**
	 * add a delivery order to the store
	 * @param order
	 */
	public void addDeliverOrder(int index, Store store, Catlog catlog, double cube, double weight, String routeGroup){
		
		boolean catlogAdded = false;
		
		for(Order order : deliveryOrders){
			if(order.getCatlog().equals(catlog)){
				order.setCube(order.getCube() + cube);
				order.setWeight(order.getWeight() + weight);
				catlogAdded = true;
			}
		}
		
		if(!catlogAdded){
			Order order = new Order(index, store, catlog, cube, weight, routeGroup);
			deliveryOrders.add(order);
		}
	}

	/**
	 * add a salvage order to the store
	 * @param order
	 */
	public void addSalvageOrder(Order order){
		salvageOrders.add(order);
	}

	/**
	 * @return the deliveryOrders
	 */
	public ArrayList<Order> getDeliveryOrders() {
		return deliveryOrders;
	}

	/**
	 * @return the salvageOrders
	 */
	public ArrayList<Order> getSalvageOrders() {
		return salvageOrders;
	}

	/**
	 * @param deliveryOrders the deliveryOrders to set
	 */
	public void setDeliveryOrders(ArrayList<Order> deliveryOrders) {
		this.deliveryOrders = deliveryOrders;
	}

	/**
	 * @param salvageOrders the salvageOrders to set
	 */
	public void setSalvageOrders(ArrayList<Order> salvageOrders) {
		this.salvageOrders = salvageOrders;
	}
	
	@Override
	public String toString() {
		return "ST[" + name +"]";
	}
	
	/**
	 * get the first Delivery order: grocery > refrigerated > frozen
	 * @return
	 */
	public Order getFirstDeliveryOrder(){
		
		for(Order order : deliveryOrders){
			if(Catlog.grocery.equals(order.getCatlog())){
				return order;
			}
		}
		for(Order order : deliveryOrders){
			if(Catlog.refrigerated.equals(order.getCatlog())){
				return order;
			}
		}
		for(Order order : deliveryOrders){
			if(Catlog.frozen.equals(order.getCatlog())){
				return order;
			}
		}
		
		return null;
	}
	
}
