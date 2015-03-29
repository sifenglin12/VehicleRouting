package edu.utexas.orie.cvrptw.output;

import java.util.Collection;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.grasp.Route;
import edu.utexas.orie.cvrptw.grasp.WarehouseLoadingCapacity;
import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;

public class VRPSolution {
	InstanceCVRPTW instance;
	LinkedList<Route> routes;
	WarehouseLoadingCapacity capacity;
	
	public VRPSolution(InstanceCVRPTW instance, Collection<Route> routes,
			WarehouseLoadingCapacity capacity) {
		super();
		this.instance = instance;
		this.routes = new LinkedList<>(routes);
		this.capacity = capacity;
	}
	
	public double getWaitCost(){
		return Route.getTotalTravelCost(routes);
	}
	
	public double getTotalCost(){
		return Route.getTotalCost(routes);
	}
	
	public double getTotalTravelCost(){
		return Route.getTotalTravelCost(routes);
	}
	
	/**
	 * @return the instance
	 */
	public InstanceCVRPTW getInstance() {
		return instance;
	}

	/**
	 * @return the routes
	 */
	public LinkedList<Route> getRoutes() {
		return routes;
	}

	/**
	 * @return the capacity
	 */
	public WarehouseLoadingCapacity getCapacity() {
		return capacity;
	}

	/**
	 * @param instance the instance to set
	 */
	public void setInstance(InstanceCVRPTW instance) {
		this.instance = instance;
	}

	/**
	 * @param routes the routes to set
	 */
	public void setRoutes(LinkedList<Route> routes) {
		this.routes = routes;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(WarehouseLoadingCapacity capacity) {
		this.capacity = capacity;
	}
	
	
}
