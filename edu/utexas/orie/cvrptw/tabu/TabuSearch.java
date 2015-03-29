package edu.utexas.orie.cvrptw.tabu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.grasp.CostMap;
import edu.utexas.orie.cvrptw.grasp.InsertNodeIntoArc;
import edu.utexas.orie.cvrptw.grasp.RemoveNode;
import edu.utexas.orie.cvrptw.grasp.ReplaceNode;
import edu.utexas.orie.cvrptw.grasp.Route;
import edu.utexas.orie.cvrptw.grasp.SolutionConstructor;
import edu.utexas.orie.cvrptw.grasp.WarehouseLoadingCapacity;
import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.VRPSolution;

/**
 * This class implements the tabu search
 * @author SifengLin
 *
 */
public class TabuSearch {
	
	InstanceCVRPTW instance;
	Network network;
	String outputFolder;
	int numTabuIteration = 10;
	
	double solutionTime = -1;
	double totalCost = -1;
	
	public TabuSearch(InstanceCVRPTW instance, Network network,
			String outputFolder) {
		super();
		this.instance = instance;
		this.network = network;
		this.outputFolder = outputFolder;
	}

	/**
	 * perform the tabu search
	 * @param solu
	 * @throws IOException
	 */
	public VRPSolution tabuSearch(VRPSolution solu) throws IOException{
		
		double startTime = 1D*System.currentTimeMillis()/1000;
		LinkedList<Route> routes = solu.getRoutes();
		WarehouseLoadingCapacity capacity = solu.getCapacity();
		
		CostMap costMap = new CostMap(routes, outputFolder, capacity);
		costMap.initializeReplaceNodeMap(capacity);
		
		LinkedHashSet<TabuCycle> curTabuLists = new LinkedHashSet<TabuCycle>();
		
		/*
		 * search for three types of neighborhood.  
		 * 1. single insert: just look at the insertNodeIntoArc
		 * 2. two swap: just go over the list once
		 * 3. one swap with a insert: just go over the list once
		 */
		
		for(int i=0; i<numTabuIteration; i++){
			//update the neighborhood
			
			TabuLocal bestNeighbor = getBestNeighbor(costMap, curTabuLists, capacity);
			bestNeighbor.exeuctePath(capacity, costMap);
			bestNeighbor.updateCostMap(capacity, costMap);
			
			//update the tabu list
			curTabuLists.add(bestNeighbor.getTabuCycle());
//			SolutionConstructor.writeSolutionRoute(outputFolder + "/TabuSolution"+i+".txt", costMap.getSoluRoutes());
		}
		
		solutionTime = 1D*System.currentTimeMillis()/1000 - startTime;
		totalCost = Route.getTotalCost(costMap.getSoluRoutes());
		SolutionConstructor.writeSolutionRoute(outputFolder + "/TabuSolution.txt", costMap.getSoluRoutes());
		SolutionConstructor.plotSolution(outputFolder + "/CPLEXSolution", costMap.getSoluRoutes(), instance.getWarehouse());
		
		return new VRPSolution(instance, costMap.getSoluRoutes(), capacity);
	}
	
	/**
	 * generate statistics for this model
	 * @return
	 */
	public Statistics generateStatistics(){
		Statistics statistics = new Statistics();
		instance.setStat(statistics);
		statistics.setTotalCost(totalCost);
		statistics.setTotalTime(solutionTime);
		
		return statistics;
	}
	
	
	/**
	 * @return the numTabuIteration
	 */
	public int getNumTabuIteration() {
		return numTabuIteration;
	}

	/**
	 * @param numTabuIteration the numTabuIteration to set
	 */
	public void setNumTabuIteration(int numTabuIteration) {
		this.numTabuIteration = numTabuIteration;
	}

	/**
	 * 
	 * @param costMap
	 * @return
	 */
	private TabuLocal getBestNeighbor(CostMap costMap, LinkedHashSet<TabuCycle> curTabuLists,
			WarehouseLoadingCapacity capacity){
		TabuLocal neighborCycle = getBestNeighborCycle(costMap, curTabuLists, capacity);
		TabuLocal neighborSingleInsert = getBestNeighborSingleInsert(costMap, curTabuLists, capacity);
		TabuLocal neighborDoubleInsert = getBestNeighborDoubleInsert(costMap, curTabuLists, capacity);
		
		//TODO how come the neighborhood could be null?
		TabuLocal bestNeighbor = neighborCycle;
		if(bestNeighbor==null || neighborSingleInsert!=null && neighborSingleInsert.getCost() < bestNeighbor.getCost()){
			bestNeighbor = neighborSingleInsert;
		}
		
		if(bestNeighbor==null || neighborDoubleInsert!=null && neighborDoubleInsert.getCost() < bestNeighbor.getCost()){
			bestNeighbor = neighborDoubleInsert;
		}
		
		return bestNeighbor;
	}
	
	/**
	 * get the best tabu cycle among all
	 * @return
	 */
	private TabuLocal getBestNeighborCycle(CostMap costMap, LinkedHashSet<TabuCycle> curTabuLists,
			WarehouseLoadingCapacity capacity){
		TabuLocal bestLocal = null;
		HashMap<OrderNode, HashMap<OrderNode, ReplaceNode>>  replaceNodeMap = costMap.getReplaceNodeMap();
		for(OrderNode firstNode : replaceNodeMap.keySet()){
			HashMap<OrderNode, ReplaceNode> lastNodeMap = replaceNodeMap.get(firstNode);
			for( OrderNode lastNode: lastNodeMap.keySet()){
				ReplaceNode nodeTwo = replaceNodeMap.get(lastNode).get(firstNode);
				if(nodeTwo!=null){
					ReplaceNode nodeOne = replaceNodeMap.get(firstNode).get(lastNode);
					double cost = nodeTwo.getCostIncrease() + nodeOne.getCostIncrease();
					if( (bestLocal ==null || bestLocal.getCost() < cost)
							&& capacity.isSwapCycleAvailable(nodeOne, nodeTwo)){
						TabuLocal curLocal = new TabuLocal(nodeOne, nodeTwo, cost);
						if(!curTabuLists.contains(curLocal.getTabuCycle())){
							bestLocal = curLocal;
						}
					}
				}
			}
		}
		
		return bestLocal;
	}
	
	/**
	 * get the best lcoal: remove one node and insert it into the other arc
	 * @param costMap
	 * @return
	 */
	private TabuLocal getBestNeighborSingleInsert(CostMap costMap, LinkedHashSet<TabuCycle> curTabuLists,
			WarehouseLoadingCapacity capacity){
		//learn from the old one
		HashMap<OrderNode, RemoveNode> removeNodeMap = costMap.getRemoveNodeMap();
		HashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>> insertNodeMap = costMap.getInsertNodeMap();
		TabuLocal bestLocal = null;
		
		for(OrderNode removedNode : removeNodeMap.keySet()){
			//need to search for all the remove node for the current node
			RemoveNode rn = removeNodeMap.get(removedNode);
			for(InsertNodeIntoArc ina : insertNodeMap.get(removedNode)){
				double cost = rn.getCostChange() + ina.getCostChangeInsertedRoute();
				if( (bestLocal==null || bestLocal.getCost() < cost)
						&& capacity.isSwapCycleAvailable(ina, rn)){
					TabuLocal curLocal = new TabuLocal(rn, ina, cost);
					if(!curTabuLists.contains(curLocal.getTabuCycle())){
						bestLocal = curLocal;
					}
				}
			}
		}
		
		return bestLocal;
	}
	
	/**
	 * get the best local: remove one node, replace another, and move the replaced node to another route
	 * @param costMap
	 * @return
	 */
	private TabuLocal getBestNeighborDoubleInsert(CostMap costMap, LinkedHashSet<TabuCycle> curTabuLists,
			WarehouseLoadingCapacity capacity){
		HashMap<OrderNode, RemoveNode> removeNodeMap = costMap.getRemoveNodeMap();
		HashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>> insertNodeMap = costMap.getInsertNodeMap();
		HashMap<OrderNode, HashMap<OrderNode, ReplaceNode>>  replaceNodeMap = costMap.getReplaceNodeMap();
		TabuLocal bestLocal = null;
		
		for(OrderNode firstNode : replaceNodeMap.keySet()){
			HashMap<OrderNode, ReplaceNode> lastNodeMap = replaceNodeMap.get(firstNode);
			for( OrderNode lastNode: lastNodeMap.keySet()){
				ReplaceNode nodeOne = replaceNodeMap.get(firstNode).get(lastNode);
				RemoveNode rn = removeNodeMap.get(firstNode);
				double curCost = nodeOne.getCostIncrease() + rn.getCostChange();
				for(InsertNodeIntoArc ina : insertNodeMap.get(lastNode)){
					
					if(rn.getRoute()==ina.getRouteInserted()){
						continue;
					}
					
					double cost = curCost + ina.getCostChangeInsertedRoute();
					if( (bestLocal==null || cost < bestLocal.getCost()
							&& capacity.isSwapCycleAvailable(ina, rn, nodeOne))){
						TabuLocal curLocal = new TabuLocal(nodeOne, cost, rn, ina);
						if(!curTabuLists.contains(curLocal.getTabuCycle())){
							bestLocal = curLocal;
						}
					}
				}
			}
		}
		
		return bestLocal;
		
	}
}

/**
 * This class implements the tabu local
 * @author SifengLin
 *
 */
class TabuLocal{
	
	ReplaceNode replaceNodeOne;
	ReplaceNode replaceNodeTwo;
	RemoveNode removeNode;
	InsertNodeIntoArc insertNodeIntoArc;
	
	double cost;
	
	
	/**
	 * execute the path accordingly
	 * @param capacity
	 */
	public void exeuctePath(WarehouseLoadingCapacity capacity, CostMap costMap){
		if(replaceNodeOne!=null){
			replaceNodeOne.getReplacedRoute().updateReplaceLocalSearch(replaceNodeOne, capacity);
		}
		if(replaceNodeTwo!=null){
			replaceNodeTwo.getReplacedRoute().updateReplaceLocalSearch(replaceNodeTwo, capacity);
		}
		if(insertNodeIntoArc!=null){
			insertNodeIntoArc.getRouteInserted().updateInsertLocalSearch(insertNodeIntoArc, capacity);
		}
		if(removeNode!=null){
			removeNode.getRoute().updateRemoveLocalSearch(removeNode, capacity);
			if(removeNode.getRoute().getArcs().size()==0){
				costMap.getSoluRoutes().remove(removeNode.getRoute());
			}
		}
	}
	
	/**
	 * update the cost map
	 * @param capacity
	 * @param costMap
	 */
	public void updateCostMap(WarehouseLoadingCapacity capacity, CostMap costMap){
		if(replaceNodeOne!=null){
			costMap.updateCostMap(replaceNodeOne, capacity);
		}
		if(replaceNodeTwo!=null){
			costMap.updateCostMap(replaceNodeTwo, capacity);
		}
		if(removeNode!=null){
			costMap.updateCostMap(removeNode, capacity);
		}
		if(insertNodeIntoArc!=null){
			costMap.updateCostMap(insertNodeIntoArc, capacity);
		}
	}
	
	public TabuLocal(ReplaceNode replaceNodeOne, double cost,
			RemoveNode removeNode, InsertNodeIntoArc insertNodeIntoArc) {
		super();
		this.replaceNodeOne = replaceNodeOne;
		this.removeNode = removeNode;
		this.insertNodeIntoArc = insertNodeIntoArc;
		this.cost = cost;
	}

	public TabuLocal(RemoveNode removeNode, InsertNodeIntoArc insertNodeIntoArc,
			double cost) {
		super();
		this.removeNode = removeNode;
		this.insertNodeIntoArc = insertNodeIntoArc;
		this.cost = cost;
	}
	
	public TabuLocal(ReplaceNode replaceNodeOne, ReplaceNode replaceNodeTwo,
			double cost) {
		super();
		this.replaceNodeOne = replaceNodeOne;
		this.replaceNodeTwo = replaceNodeTwo;
		this.cost = cost;
	}

	/**
	 * get the corresponding tabu cycle
	 * @return
	 */
	public TabuCycle getTabuCycle(){
		ArrayList<OrderNode> nodes = new ArrayList<OrderNode>();
		if(replaceNodeOne==null){
			nodes.add(insertNodeIntoArc.getNodeToInsert());
			return new TabuPath(nodes, insertNodeIntoArc.getArcInserted());
		}
		else if(replaceNodeTwo==null){
			nodes.add(removeNode.getNodeToRemove());
			nodes.add(replaceNodeOne.getReplacedNode());
			return new TabuPath(nodes, insertNodeIntoArc.getArcInserted());
		}
		else{
			nodes.add(replaceNodeOne.getReplacedNode());
			nodes.add(replaceNodeTwo.getReplacedNode());
			return new TabuCycle(nodes);
		}
	}
	
	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

}
