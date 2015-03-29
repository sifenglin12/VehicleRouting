package edu.utexas.orie.cvrptw.grasp;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.OrderNode;

public class ReplaceNode {
	
	/**
	 * the node that would replace the other node
	 */
	OrderNode nodeToReplace;
	
	/**
	 * the node to be replaced by the other node
	 */
	OrderNode replacedNode;
	
	/**
	 * the location of node to be replaced by the other node
	 */
	int replacedNodeLocation;
	
	/**
	 * the route the replacedNode is in
	 */
	Route replacedRoute;
	
	/**
	 * the new origin time
	 */
	double newOriginTime;
	
	double costIncrease;

	public ReplaceNode(OrderNode nodeToReplace, int replacedNodeLocation,
			Route replaceRoute, double newOriginTime, double costIncrease) {
		super();
		this.nodeToReplace = nodeToReplace;
		this.replacedNodeLocation = replacedNodeLocation;
		this.replacedNode = (OrderNode) replaceRoute.getArcs().get(replacedNodeLocation).getToNode();
		this.replacedRoute = replaceRoute;
		this.newOriginTime = newOriginTime;
		this.costIncrease = costIncrease;
		
	}

	/**
	 * @return the nodeToReplace
	 */
	public OrderNode getNodeToReplace() {
		return nodeToReplace;
	}

	/**
	 * @return the replacedNodeLocation
	 */
	public int getReplacedNodeLocation() {
		return replacedNodeLocation;
	}

	/**
	 * @return the replaceRoute
	 */
	public Route getReplacedRoute() {
		return replacedRoute;
	}

	/**
	 * @return the newOriginTime
	 */
	public double getNewOriginTime() {
		return newOriginTime;
	}

	/**
	 * @return the costIncrease
	 */
	public double getCostIncrease() {
		return costIncrease;
	}

	/**
	 * @param nodeToReplace the nodeToReplace to set
	 */
	public void setNodeToReplace(OrderNode nodeToReplace) {
		this.nodeToReplace = nodeToReplace;
	}

	/**
	 * @param replacedNodeLocation the replacedNodeLocation to set
	 */
	public void setReplacedNodeLocation(int replacedNodeLocation) {
		this.replacedNodeLocation = replacedNodeLocation;
	}

	/**
	 * @param replaceRoute the replaceRoute to set
	 */
	public void setReplaceRoute(Route replaceRoute) {
		this.replacedRoute = replaceRoute;
	}

	/**
	 * @param newOriginTime the newOriginTime to set
	 */
	public void setNewOriginTime(double newOriginTime) {
		this.newOriginTime = newOriginTime;
	}

	/**
	 * @param costIncrease the costIncrease to set
	 */
	public void setCostIncrease(double costIncrease) {
		this.costIncrease = costIncrease;
	}

	/**
	 * @return the replacedNode
	 */
	public OrderNode getReplacedNode() {
		return replacedNode;
	}

	/**
	 * @param replacedNode the replacedNode to set
	 */
	public void setReplacedNode(OrderNode replacedNode) {
		this.replacedNode = replacedNode;
	}
	
	
	
}
