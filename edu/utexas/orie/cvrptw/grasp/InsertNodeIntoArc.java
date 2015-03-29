package edu.utexas.orie.cvrptw.grasp;

import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * this function is used to implement the insert a node into an arc in local search
 * @author SifengLin
 *
 */
public class InsertNodeIntoArc{

	OrderNode nodeToInsert;
	Route routeInserted;
	Arc arcInserted;
	
	/**
	 * the new time to leave the warehouse after inserting the node
	 */
	double newOriginTime;
	
	/**
	 * the additional wait cost incurred after inserting the node
	 */
	double costChangeInsertedRoute;
	
	public InsertNodeIntoArc(OrderNode nodeToInsert, Route routeInserted,
			Arc arcInserted, double newOriginTime,
			double costChangeInsertedRoute) {
		super();
		this.nodeToInsert = nodeToInsert;
		this.routeInserted = routeInserted;
		this.arcInserted = arcInserted;
		this.newOriginTime = newOriginTime;
		this.costChangeInsertedRoute = costChangeInsertedRoute;
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
	 * @return the newOriginTime
	 */
	public double getNewOriginTime() {
		return newOriginTime;
	}

	/**
	 * @return the costChangeInsertedRoute
	 */
	public double getCostChangeInsertedRoute() {
		return costChangeInsertedRoute;
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

	/**
	 * @param newOriginTime the newOriginTime to set
	 */
	public void setNewOriginTime(double newOriginTime) {
		this.newOriginTime = newOriginTime;
	}

	/**
	 * @param costChangeInsertedRoute the costChangeInsertedRoute to set
	 */
	public void setCostChangeInsertedRoute(double costChangeInsertedRoute) {
		this.costChangeInsertedRoute = costChangeInsertedRoute;
	}
	
	
	
}
