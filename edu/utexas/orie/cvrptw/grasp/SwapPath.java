package edu.utexas.orie.cvrptw.grasp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * This object contains the swap path for the local search
 * @author SifengLin
 *
 */
public class SwapPath {
	ArrayList<ReplaceNode> replaceNodeList;  //the list of @replaceNodes
	Set<Route> changedRoutes;  //list of routes whose nodes would be replaced
	
	/**
	 * note that the cost does not include the cost to remove the first node
	 */
	double cost;
	//I need to make sure that the route never appears twice
	
	public SwapPath(ArrayList<ReplaceNode> replaceNodeList, double cost, Set<Route> replacedRoutes) {
		super();
		this.replaceNodeList = replaceNodeList;
		this.cost = cost;
		this.changedRoutes = replacedRoutes;;
	}
	
	public SwapPath(ReplaceNode replaceNode, CostMap map){
		this.replaceNodeList = new ArrayList<ReplaceNode>();
		this.replaceNodeList.add(replaceNode);
		this.cost = replaceNode.getCostIncrease();
		this.changedRoutes =new HashSet<Route>();
		this.changedRoutes.add(replaceNode.getReplacedRoute());
		this.changedRoutes.add(map.getNodeToRoute().get(replaceNode.getNodeToReplace()));
		
	}
	
	/**
	 * add a replace node 
	 */
	public void addNewReplaceNode(ReplaceNode rn, CostMap costMap){
		replaceNodeList.add(rn);
		changedRoutes.add(rn.getReplacedRoute());
		cost = cost + rn.getCostIncrease();// + costMap.getRemoveCost(rn.getReplacedNode());
	}
	
	/**
	 * generate a new path, adding the @newNode to it
	 * @param newNode
	 * @return
	 */
	public SwapPath generateNewSwapPath(ReplaceNode newNode, CostMap costMap){
		ArrayList<ReplaceNode> newList = new ArrayList<ReplaceNode>(replaceNodeList);
		Set<Route> newReplacedRoutes = new HashSet<>(changedRoutes); 
		newList.add(newNode);
		newReplacedRoutes.add(newNode.getReplacedRoute());
		//to generate the new cost: remove the last node, and the move cost
		double newCost = cost + newNode.getCostIncrease();// + costMap.getRemoveCost(newNode.getReplacedNode());
		return new SwapPath(newList, newCost, newReplacedRoutes);
	}
	
	/**
	 * get the routes that would be modified
	 * @return
	 */
	public Set<Route> getModifiedRoutes(){
		return changedRoutes;
	}
	
	/**
	 * get the ReplaceNode that would form a circle with negative cost change 
	 * @return
	 */
	public ReplaceNode getNegativeCircleReplaceNode(CostMap costMap, WarehouseLoadingCapacity capacity){
		
		OrderNode firstNode = replaceNodeList.get(0).getNodeToReplace();
		OrderNode lastNode = replaceNodeList.get(replaceNodeList.size()-1).getReplacedNode();
		
		ReplaceNode rn = costMap.getReplaceNode(lastNode, firstNode);
		
		if(rn!=null ){ //!replacedRoutes.contains(rn.getReplacedRoute())
			
			double newCost = cost + rn.getCostIncrease();// + costMap.getRemoveCost(firstNode);;
			if(newCost<CostMap.threshould){
				if(capacity.isSwapCycleAvailable(this, null, null, rn)){
					return rn;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * get the negative cost path that form a feasible swap
	 * what happen to the remove node cost here? I did not incorporate it
	 * @param costMap
	 * @return
	 */
	public InsertNodeIntoArc getNegativeCostPath(CostMap costMap, LinkedHashSet<Route> modifiedRoutes, WarehouseLoadingCapacity capacity){
		OrderNode lastNode = replaceNodeList.get(replaceNodeList.size()-1).getReplacedNode();
//		LinkedHashSet<Route> routes = containedRoutes();
		//need to find some where to move
		RemoveNode removeNode = costMap.getRemoveNode(getFirstNode().getNodeToReplace());
		for(InsertNodeIntoArc ina : costMap.getInsertNodeIntoArcs(lastNode)){
			if(!changedRoutes.contains(ina.getRouteInserted()) && !modifiedRoutes.contains(ina.getRouteInserted())
					){//&& removeNode.getRoute()!=ina.getRouteInserted()
				double newCost = cost + ina.getCostChangeInsertedRoute() + removeNode.getCostChange();
				if(newCost<0 ){
					if(capacity.isSwapCycleAvailable(this, ina, removeNode, null)){
						//when the cost is negative, we need to make sure that the warehouse loading capacity is justified
						return ina;
					}
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * get the set of routes that is already in the list
	 * @return
	 */
	public LinkedHashSet<Route> containedRoutes(){
		LinkedHashSet<Route> routes = new LinkedHashSet<Route>();
		for(ReplaceNode rn : replaceNodeList){
			routes.add(rn.getReplacedRoute());
		}
		
		return routes;
	}

	/**
	 * @return the replaceNodeList
	 */
	public ArrayList<ReplaceNode> getReplaceNodeList() {
		return replaceNodeList;
	}

	/**
	 * @param replaceNodeList the replaceNodeList to set
	 */
	public void setReplaceNodeList(ArrayList<ReplaceNode> replaceNodeList) {
		this.replaceNodeList = replaceNodeList;
	}
	
	/**
	 * get the last replace node
	 * @return
	 */
	public ReplaceNode getLastNode(){
		return replaceNodeList.get(replaceNodeList.size()-1);
	}

	/**
	 * get the first replace node
	 * @return
	 */
	public ReplaceNode getFirstNode(){
		return replaceNodeList.get(0);
	}
	
	/**
	 * check if the replaced routes is already here
	 * @param route
	 * @return
	 */
	public boolean isContainRoute(Route route){
		return changedRoutes.contains(route);
	}
}
