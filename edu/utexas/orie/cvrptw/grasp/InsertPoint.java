package edu.utexas.orie.cvrptw.grasp;

import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * This object packs the function that inserts a node into a path at specific arc
 * @author SifengLin
 *
 */
public class InsertPoint {
	
	OrderNode nodeToInsert;
	Route routeInserted;
	Arc arcInserted;
	
	/**
	 * the new time to leave the warehouse after inserting the node
	 */
	double newOriginTime;
	
	/**
	 * opportunity cost incurred after inserting the node
	 */
	double opportunityCost;
	
	public InsertPoint(OrderNode nodeToInsert, Route routeInserted,
			Arc arcInserted, double newOriginTime, double opportunityCost) {
		super();
		this.nodeToInsert = nodeToInsert;
		this.routeInserted = routeInserted;
		this.arcInserted = arcInserted;
		this.newOriginTime = newOriginTime;
		this.opportunityCost = opportunityCost;
	}
	
	/**
	 * update this insert point to the corresponding route
	 */
	public void update(WarehouseLoadingCapacity capacity){
		routeInserted.updateInsert(this, capacity);
	}
	
	/**
	 * @return the additionalCost
	 */
	public double getOpportunityCost() {
		return opportunityCost;
	}

	/**
	 * @param additionalCost the additionalCost to set
	 */
	public void setOpportunityCost(double opportunityCost) {
		this.opportunityCost = opportunityCost;
	}

	/**
	 * @return the newOriginTime
	 */
	public double getNewOriginTime() {
		return newOriginTime;
	}

	/**
	 * @param newOriginTime the newOriginTime to set
	 */
	public void setNewOriginTime(double newOriginTime) {
		this.newOriginTime = newOriginTime;
	}

	/**
	 * @return the nodeToInsert
	 */
	public OrderNode getNodeToInsert() {
		return nodeToInsert;
	}

	/**
	 * @return the routeInserted
	 */
	public Route getRouteInserted() {
		return routeInserted;
	}

	/**
	 * @return the arcInserted
	 */
	public Arc getArcInserted() {
		return arcInserted;
	}

	/**
	 * @param nodeToInsert the nodeToInsert to set
	 */
	public void setNodeToInsert(OrderNode nodeToInsert) {
		this.nodeToInsert = nodeToInsert;
	}

	/**
	 * @param routeInserted the routeInserted to set
	 */
	public void setRouteInserted(Route routeInserted) {
		this.routeInserted = routeInserted;
	}

	/**
	 * @param arcInserted the arcInserted to set
	 */
	public void setArcInserted(Arc arcInserted) {
		this.arcInserted = arcInserted;
	}
	
}
