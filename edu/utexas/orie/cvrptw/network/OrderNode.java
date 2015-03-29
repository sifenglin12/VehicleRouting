package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.Catlog;
import edu.utexas.orie.cvrptw.instance.Order;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.instance.Store;

public class OrderNode extends Node {
	Order order;
	
	public OrderNode(Order order) {
		super();
		this.order = order;
		timeWindowStart = order.getStore().getEarliestTimeHour();
		timeWindowEnd = order.getStore().getLatestTimeHour();
	}

	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Order order) {
		this.order = order;
	}

	@Override
	public boolean isDeliveryNode() {
		return !Catlog.salvage.equals(order.getCatlog());
	}

	@Override
	public boolean isPickUpNode() {
		return Catlog.salvage.equals(order.getCatlog());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + order + "]";
	}

	@Override
	public double getWeight() {
		return order.getWeight();
	}

	@Override
	public double getCube() {
		return order.getCube();
	}

	@Override
	public boolean isElibileSuccessorOf(Node node) {
		return false;
	}

	@Override
	public boolean isElibilePredecessorOf(Node node) {
		return false;
	}
	
	/**
	 * get the latest end time in the store
	 * @return
	 */
	public double getLatestEndTime(){
		double additionalTime = 0;
		Store store = order.getStore();
		for(Order od : store.getDeliveryOrders()){
			if(od.getCatlog().getValue() > order.getCatlog().getValue()){
				additionalTime = additionalTime + od.getCube()/Parameter.getLoadRate();
			}
		}
		
		return additionalTime + order.getStore().getLatestTimeHour();
	}
	
	/**
	 * get the cumulative time in this store, if @preNode is given
	 * @return
	 */
	public double getCumulativeTimeInStore(Arc arc){
		
		if(arc.getToNode()!=this || !arc.isWithinSameStore()){
			return 0;
		}
		
		return 10000;
		
//		OrderNode preNode = (OrderNode) arc.getFromNode();
//		double additionalTime = 0;
//		Store store = order.getStore();
//		for(Order od : store.getDeliveryOrders()){
//			if(od.getCatlog().getValue() >= preNode.getOrder().getCatlog().getValue()){
//				additionalTime = additionalTime + od.getCube()/Parameter.getLoadRate();
//			}
//		}
//		
//		return additionalTime;
	}
	
	
}
