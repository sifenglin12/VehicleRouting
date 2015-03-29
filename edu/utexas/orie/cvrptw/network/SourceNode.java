package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.instance.Warehouse;
import edu.utexas.orie.cvrptw.output.StringProcessor;

public class SourceNode extends Node {
	
	Warehouse warehouse;
	
	public SourceNode(Warehouse warehouse) {
		super();
		this.warehouse = warehouse;
		timeWindowEnd = warehouse.getPullTimeEndHour() + Parameter.getTotalTimeLimitPerRouteHour(); 
	}

	/**
	 * @return the warehouse
	 */
	public Warehouse getWarehouse() {
		return warehouse;
	}

	/**
	 * @param warehouse the warehouse to set
	 */
	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}

	@Override
	public boolean isDeliveryNode() {
		return false;
	}

	@Override
	public boolean isPickUpNode() {
		return false;
	}

	@Override
	public double getWeight() {
		return 0;
	}

	@Override
	public double getCube() {
		return 0;
	}

	@Override
	public boolean isElibileSuccessorOf(Node node) {
		return false;
	}

	@Override
	public boolean isElibilePredecessorOf(Node node) {
		return true;
	}

	@Override
	public String toString() {
		return "["+StringProcessor.getString("SourceNode", 10)+"]";
	}
	
	
}
