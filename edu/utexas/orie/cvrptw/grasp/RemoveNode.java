package edu.utexas.orie.cvrptw.grasp;

import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * this function removes a node from a route, calculates its cost, and corresponding new orign time
 * @author SifengLin
 *
 */
public class RemoveNode {
	
	OrderNode nodeToRemove;
	int nodeLocation;  //the location of the arc that the node serves as an end node
	Route route;
	
	double newOriginTime;
	double costChange;
	
	public RemoveNode(OrderNode nodeToRemove, Route route,
			double newOriginTime, double newCost, int nodeLocation) {
		super();
		this.nodeToRemove = nodeToRemove;
		this.route = route;
		this.newOriginTime = newOriginTime;
		this.costChange = newCost;
		this.nodeLocation = nodeLocation;
	}

	/**
	 * @return the nodeToRemove
	 */
	public OrderNode getNodeToRemove() {
		return nodeToRemove;
	}

	/**
	 * @return the route
	 */
	public Route getRoute() {
		return route;
	}

	/**
	 * @return the newOriginTime
	 */
	public double getNewOriginTime() {
		return newOriginTime;
	}

	/**
	 * @return the newCost
	 */
	public double getCostChange() {
		return costChange;
	}

	/**
	 * @param nodeToRemove the nodeToRemove to set
	 */
	public void setNodeToRemove(OrderNode nodeToRemove) {
		this.nodeToRemove = nodeToRemove;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(Route route) {
		this.route = route;
	}

	/**
	 * @param newOriginTime the newOriginTime to set
	 */
	public void setNewOriginTime(double newOriginTime) {
		this.newOriginTime = newOriginTime;
	}

	/**
	 * @param newCost the newCost to set
	 */
	public void setNewCost(double newCost) {
		this.costChange = newCost;
	}

	/**
	 * @return the nodeLocation
	 */
	public int getNodeLocation() {
		return nodeLocation;
	}

	/**
	 * @param nodeLocation the nodeLocation to set
	 */
	public void setNodeLocation(int nodeLocation) {
		this.nodeLocation = nodeLocation;
	}

	/**
	 * @param costChange the costChange to set
	 */
	public void setCostChange(double costChange) {
		this.costChange = costChange;
	}
	
	
	
}
