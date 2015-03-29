package edu.utexas.orie.cvrptw.grasp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * this object contains the map that contains all the cost information
 * @author SifengLin
 *
 */
public class CostMap {
	
	public static final double LARGE_VALUE = Double.MAX_VALUE/100;
	public static final double threshould = -1e-6;
	
	LinkedHashSet<Route> soluRoutes;
	WarehouseLoadingCapacity warehouseCapacity;
	
	/**
	 * map that denotes the route each node belongs to
	 */
	HashMap<OrderNode, Route> nodeToRoute;
	/**
	 * map that denotes the removing a node from a route
	 */
	HashMap<OrderNode, RemoveNode> removeNodeMap;
	
	/**
	 * map that denotes replacing one node with another
	 * the two nodes never belong to two routes
	 */
	HashMap<OrderNode, HashMap<OrderNode, ReplaceNode>> replaceNodeMap;
	
	/**
	 * map that denotes moving one node inside an arc.
	 * Here I would get the best InsertNodeIntoArc for each path and inserted there
	 * the two nodes never belong to two routes
	 */
	HashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>> insertNodeMap;
	
	String outputFolder;
	
	public CostMap(Collection<Route> routes, String outputFolder, WarehouseLoadingCapacity capacity){
		soluRoutes = new LinkedHashSet<Route>(routes);
		initializeNodeToRoute();
		initializeRemoveNodeMap(capacity);
		initializeInsertNodeMap(capacity);
//		initializeReplaceNodeMap();
		replaceNodeMap = new LinkedHashMap<OrderNode, HashMap<OrderNode,ReplaceNode>>();
		this.outputFolder = outputFolder;
	}
	
	public LinkedHashSet<InsertNodeIntoArc> getInsertNodeIntoArcs(OrderNode node){
		return insertNodeMap.get(node);
	}
	
//	/**
//	 * get the InsertNodeIntoArc after @nodeToInsert is inserted into @arcInserted
//	 * @param nodeToInsert
//	 * @param arcInserted
//	 * @return
//	 */
//	public InsertNodeIntoArc getInsertNodeIntoArc(OrderNode nodeToInsert, Arc arcInserted){
//		InsertNodeIntoArc ina = insertNodeMap.get(nodeToInsert).get(arcInserted);
//		return ina;
//	}
	
	/**
	 * get all current replace node
	 * @return
	 */
	public LinkedList<ReplaceNode> getElegibleReplaceNodes(){
		LinkedList<ReplaceNode> replaceNodes = new LinkedList<ReplaceNode>();
		
		for(OrderNode n : replaceNodeMap.keySet()){
			for(ReplaceNode rn : replaceNodeMap.get(n).values()){
				if(rn.getCostIncrease()<0){
					replaceNodes.add(rn);
				}
			}
		}
		
		return replaceNodes;
	}
	
	/**
	 * find all the possible pairs of single remove and insert, without considering any swap
	 * @throws IOException 
	 */
	public void findSingleInsert(WarehouseLoadingCapacity capacity) throws IOException{
		
		BufferedWriter writerSingleInsert = new BufferedWriter(new FileWriter(outputFolder + "/singleInsert.csv"));
		boolean isImproved = false;
		boolean isAnyImproveCurrentIteration = true;
		while(isAnyImproveCurrentIteration){
			writerSingleInsert.write("\n"+Route.getTotalCost(soluRoutes)+",\n");
			LinkedList<RemoveNode> rns = new LinkedList<RemoveNode>();
			LinkedList<InsertNodeIntoArc> inas = new LinkedList<InsertNodeIntoArc>();
			HashSet<Route> modifiedRoutes = new LinkedHashSet<Route>();
			
			for(OrderNode removedNode : removeNodeMap.keySet()){
				//need to search for all the remove node for the current node
				
				if(removedNode.toString().contains("016-348-GRO")){
					System.out.println("In CostMap");
				}
				
				RemoveNode rn = removeNodeMap.get(removedNode);
				if(modifiedRoutes.contains(rn.getRoute())){
					continue;
				}
				
				for(InsertNodeIntoArc ina : insertNodeMap.get(removedNode)){
					if(modifiedRoutes.contains(ina.getRouteInserted())){
						continue;
					}
					
					double cost = rn.getCostChange() + ina.getCostChangeInsertedRoute();
					if(cost < threshould){
						//a negative insert is found
						writerSingleInsert.write(","+cost +",");
						writerSingleInsert.write(ina.getNodeToInsert() +"," + ina.getArcInserted() + "," + ina.getRouteInserted()+",");
						writerSingleInsert.newLine();
//						writerSingleInsert.write(rn.getRoute()+",");
//						writerSingleInsert.newLine();
						writerSingleInsert.flush();
						
						rns.add(rn);
						inas.add(ina);
						modifiedRoutes.add(ina.getRouteInserted());
						modifiedRoutes.add(rn.getRoute());
						
						break;  //for one removedNode, only find one INA
					}
				}
			}
			
			Iterator<RemoveNode> rnIt = rns.iterator();
//			double expectedCost = getCostAfterUpdate(rns, inas);//used to verfiy the cost calculation
			for(InsertNodeIntoArc ina : inas){
				RemoveNode rn = rnIt.next();
				executePath(ina, rn, null, capacity);    
				//update the map
				updateCostMap(rn, capacity);
				updateCostMap(ina, capacity);
			}
			
			if(inas.size()>0){
				isImproved = true;
			}
			
//			if(Math.abs( expectedCost - Route.getTotalCost(soluRoutes) )>1e-6){
//				System.out.println("ERROR in cost calculation of CostMap.findSingleInsert");
//			}
			
//			writeSolutionRoutes(writerSingleInsert);
			isAnyImproveCurrentIteration = (inas.size()>0);
			
		}
		
		if(isImproved){
			System.out.println("\tSingleImprove");
		}
		
		writerSingleInsert.close();
		
	}
	
	/**
	 * get the cost after updating, based on the current cost
	 */
	private double getCostAfterUpdate(LinkedList<RemoveNode> rns, LinkedList<InsertNodeIntoArc> inas){
		double previousCost = Route.getTotalCost(soluRoutes);
		Iterator<RemoveNode> rnIt = rns.iterator();
		for(InsertNodeIntoArc ina : inas){
			RemoveNode rn = rnIt.next();
			
			previousCost = previousCost + ina.getCostChangeInsertedRoute();
			previousCost = previousCost + rn.getCostChange();
		}
		
		return previousCost;
	}
	
	/**
	 * write out the solution routes
	 * @throws IOException 
	 */
	public void writeSolutionRoutes(BufferedWriter writer) throws IOException{
		//print out the solution route
		for(Route route : soluRoutes){
			writer.write(route.toString());
			writer.newLine();
		}
		writer.newLine();
		writer.flush();

	}
	
	/**
	 * get the ReplaceNode after @nodeToReplaceOthers is replaced by @nodeReplaced
	 * @param nodeToReplaceOthers
	 * @param nodeReplaced
	 * @return
	 */
	public ReplaceNode getReplaceNode(OrderNode nodeToReplaceOthers, OrderNode nodeReplaced){
		ReplaceNode rn = replaceNodeMap.get(nodeToReplaceOthers).get(nodeReplaced);
		return rn;
	}
	
	/**
	 * @return the nodeToRoute
	 */
	public HashMap<OrderNode, Route> getNodeToRoute() {
		return nodeToRoute;
	}

	/**
	 * @return the removeNodeMap
	 */
	public HashMap<OrderNode, RemoveNode> getRemoveNodeMap() {
		return removeNodeMap;
	}

	/**
	 * @return the replaceNodeMap
	 */
	public HashMap<OrderNode, HashMap<OrderNode, ReplaceNode>> getReplaceNodeMap() {
		return replaceNodeMap;
	}

	/**
	 * @return the insertNodeMap
	 */
	public HashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>> getInsertNodeMap() {
		return insertNodeMap;
	}

	/**
	 * @param nodeToRoute the nodeToRoute to set
	 */
	public void setNodeToRoute(HashMap<OrderNode, Route> nodeToRoute) {
		this.nodeToRoute = nodeToRoute;
	}

	/**
	 * @param removeNodeMap the removeNodeMap to set
	 */
	public void setRemoveNodeMap(HashMap<OrderNode, RemoveNode> removeNodeMap) {
		this.removeNodeMap = removeNodeMap;
	}

	/**
	 * @param replaceNodeMap the replaceNodeMap to set
	 */
	public void setReplaceNodeMap(
			HashMap<OrderNode, HashMap<OrderNode, ReplaceNode>> replaceNodeMap) {
		this.replaceNodeMap = replaceNodeMap;
	}

	/**
	 * @param insertNodeMap the insertNodeMap to set
	 */
	public void setInsertNodeMap(
			HashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>> insertNodeMap) {
		this.insertNodeMap = insertNodeMap;
	}

	/**
	 * get RemoveNode to remove a node from a route
	 * @param node
	 * @return
	 */
	public RemoveNode getRemoveNode(OrderNode node){
		RemoveNode rn = removeNodeMap.get(node);
		return rn;
	}

//	/**
//	 * get the cost change after @nodeToInsert is inserted into @arcInserted
//	 * @param nodeToInsert
//	 * @param arcInserted
//	 * @return
//	 */
//	public double getInsertCost(OrderNode nodeToInsert, Arc arcInserted){
//		InsertNodeIntoArc ina = insertNodeMap.get(nodeToInsert).get(arcInserted);
//		return ina!=null ? ina.getCostChangeInsertedRoute() : LARGE_VALUE;
//	}
	
	/**
	 * get the cost change after @nodeToReplaceOthers is replaced by @nodeReplaced
	 * @param nodeToReplaceOthers
	 * @param nodeReplaced
	 * @return
	 */
	public double getReplaceNodeCost(OrderNode nodeToReplaceOthers, OrderNode nodeReplaced){
		ReplaceNode rn = replaceNodeMap.get(nodeToReplaceOthers).get(nodeReplaced);
		return rn!=null ? rn.getCostIncrease() : LARGE_VALUE;
	}
	
	/**
	 * get the cost change to remove a node from a route
	 * @param node
	 * @return
	 */
	public double getRemoveCost(OrderNode node){
		RemoveNode rn = removeNodeMap.get(node);
		return rn!=null ? rn.getCostChange() : LARGE_VALUE;
	}
	
	/**
	 * initialize the @insertNodeMap
	 */
	private void initializeInsertNodeMap(WarehouseLoadingCapacity capacity){
		insertNodeMap = new LinkedHashMap<OrderNode, LinkedHashSet<InsertNodeIntoArc>>();
		for(Route rOne: soluRoutes){
			for(OrderNode node: rOne.getOrderNodes()){
				LinkedHashSet<InsertNodeIntoArc> curInsertNodeMap = new LinkedHashSet<InsertNodeIntoArc>();
				for(Route rTwo : soluRoutes){
					if(rOne==rTwo){
						continue;
					}
					InsertNodeIntoArc ina = rTwo.getBestInsertArcsForNodeLS(node, capacity);
					if(ina!=null){
						curInsertNodeMap.add(ina);
					}
				}
				insertNodeMap.put(node, curInsertNodeMap);
			}
		}
	}
	
	/**
	 * initialize the <replaceNodeMap>
	 */
	public void initializeReplaceNodeMap(WarehouseLoadingCapacity capacity){
		replaceNodeMap = new LinkedHashMap<OrderNode, HashMap<OrderNode,ReplaceNode>>();
		for(Route rOne : soluRoutes){
			for(OrderNode nodeOne: rOne.getOrderNodes()){
				HashMap<OrderNode, ReplaceNode> curReplaceNodeMap = new LinkedHashMap<OrderNode, ReplaceNode>();
				for(Route rTwo : soluRoutes){
					if(rOne==rTwo){
						continue;
					}
					for(ReplaceNode rn: rTwo.getReplacableNode(nodeOne, capacity)){
						curReplaceNodeMap.put(rn.getReplacedNode(), rn);
					}
					
				}
				
				replaceNodeMap.put(nodeOne, curReplaceNodeMap);
				
			}
		}
	}
	
	
	/**
	 * initialize the @removeNodeMap
	 */
	private void initializeRemoveNodeMap(WarehouseLoadingCapacity capacity){
		removeNodeMap = new LinkedHashMap<OrderNode, RemoveNode>();
		for(Route r : soluRoutes){
			for(OrderNode node : r.getOrderNodes()){
				if(node.toString().contains("016-348-GRO")){
					System.out.println("In CostMap");
				}
				RemoveNode rn = r.getRemoveNode(node, capacity);
				if(rn!=null){
					removeNodeMap.put(node, rn);
				}
			}
		}
	}
	
	/**
	 * given the set of routes that is changed, update the cost map accordingly
	 * Specifically, we need to update
	 * 1. Remove Map for each node in the routes
	 * 2. Insert Map for each node that uses the route as an insert point
	 * 		for those routes that not more nodes are added, we do not need to update the insert node for those that does not contain it
	 * 		for other routes, we need to go over all the other nodes, and make sure the insert
	 * 3. Swap between two nodes
	 *		 
	 * @param removedRoutes the routes to be removed
	 * @param replacedNodes the routes to be replaced
	 * @param insertNodesInArcs insert node into arcs
	 */
	public void updateCostMap(Collection<ReplaceNode> replacedNodes, WarehouseLoadingCapacity capacity){
		//if one route is changed, we simply look for all other possible situations
		
		for(ReplaceNode rn : replacedNodes){
			updateCostMap(rn, capacity);
		}
		
	}
	
	/**
	 * improve within the current route
	 * @param route
	 * @param capacity
	 */
	private void updateWithinRoute(Route route, WarehouseLoadingCapacity capacity){
		Route newRoute = route.getImprovedRoute();
		if(newRoute!=null){
			capacity.updateRoute(route.getOriginTime(), newRoute.getOriginTime(), route);
		}
		route.changeRoute(newRoute);
	}
	
	
	/**
	 * update the cost map for the RemoveNode @rn
	 * @param rn
	 */
	public void updateCostMap(ReplaceNode rn, WarehouseLoadingCapacity capacity){
		nodeToRoute.put(rn.getNodeToReplace(), rn.getReplacedRoute());
		delteMapReplacedNode(rn);
		Route route = rn.getReplacedRoute();
		
		updateWithinRoute(route, capacity);
		
		updateCostMapRemoveNode(route, capacity);
		updateSomeNodesInsertNodeInArc(route, capacity);
		//need to create new route fromNode & old route
		updateSomeNodesReplaceNodeGivenRoute(route, rn.getReplacedNode(), capacity);
	}
	
	public void updateCostMap(InsertNodeIntoArc ina, WarehouseLoadingCapacity capacity){
		nodeToRoute.put(ina.getNodeToInsert(), ina.getRouteInserted());
		delteMapInsertNode(ina);
		Route route = ina.getRouteInserted();
		
		updateWithinRoute(route, capacity);
		
		updateCostMapRemoveNode(route, capacity);
		updateSomeNodesInsertNodeInArc(route, capacity);
		updateSomeNodesReplaceNodeGivenRoute(route, null, capacity);
	}
	
	/**
	 * update the cost map for the RemoveNode rn
	 * @param rn
	 */
	public void updateCostMap(RemoveNode rn, WarehouseLoadingCapacity capacity){
		
		Route route = rn.getRoute();
		
		updateWithinRoute(route, capacity);
		
		updateCostMapRemoveNode(route, capacity);   //based on the new route
		updateAllNodesInsertNodeInArc(route, capacity);  //based on the new route
		updateAllNodesReplaceNodeGivenRoute(route, capacity);
		if(route.getArcs().size()==0 || route.getArcs().get(0)==null){
			soluRoutes.remove(route);
		}
	}
	
	/**
	 * delete the redundant map information due to same route 
	 * @param ina
	 */
	private void delteMapInsertNode(InsertNodeIntoArc ina){
		OrderNode node = ina.getNodeToInsert();
		Route route = ina.getRouteInserted();
		deleteRouteNodePair(route, node);
	}
	
	/**
	 * delete the redunanta map information due to same route
	 * delete the pair of @node and @route
	 * I need to delete the map that contains fromNode --> toRoute
	 * delete toRoute--> fromNode
	 * @param node
	 * @param route
	 */
	private void delteMapReplacedNode(ReplaceNode rn){
		//for the replaceNode map
		OrderNode node = rn.getNodeToReplace();
		Route route = rn.getReplacedRoute();
		deleteRouteNodePair(route, node);
	}
	
	/**
	 * delete the @route and @node pair in the replace map and insert map
	 * @param route
	 * @param node
	 */
	private void deleteRouteNodePair(Route route, OrderNode node){
		
		if(replaceNodeMap.size()>0){
			ArrayList<OrderNode> nodes = route.getOrderNodes();
			for(OrderNode n : nodes){
				replaceNodeMap.get(node).remove(n);
				replaceNodeMap.get(n).remove(node);
			}
		}
		
		InsertNodeIntoArc curIna = null;
		LinkedHashSet<InsertNodeIntoArc> inaList = insertNodeMap.get(node);
		
		for(InsertNodeIntoArc ina : inaList){
			if(ina.getRouteInserted()==route){
				curIna = ina;
			}
		}
		
		inaList.remove(curIna);
		
	}
	
//	/**
//	 * update the costMap, given the Removed Routes
//	 * once a node is removed, we need to:
//	 * 1. update the remove node map
//	 * 2. update the replace node map for all other nodes, remove the current node, and add new
//	 * 3. update the insert node map
//	 */
//	private void updateCostMapGivenRemovedRoutes(LinkedList<RemoveNode> removedNodes){
//		for(RemoveNode rn : removedNodes){
//			Route route = rn.getRoute();
//			updateCostMapRemoveNode(route);
//			
//			//for all the node in the route, I would study if it could make difference in the replace route and other route
//			
//		}
//	}
	
	/**
	 * if a route is changed, search all nodes to update their InsertNodeInArc
	 * @param route
	 */
	private void updateAllNodesInsertNodeInArc(Route route, WarehouseLoadingCapacity capacity){
		
		LinkedHashSet<OrderNode> curOrderNodes = new LinkedHashSet<OrderNode>(route.getOrderNodes());
		for(OrderNode node : insertNodeMap.keySet()){
			if(curOrderNodes.contains(node)){//if it is part of the route now, there is no way to insert it
				continue;
			}
			
			//first need to find current route in the map
			LinkedHashSet<InsertNodeIntoArc> inas = insertNodeMap.get(node);
			//since each route only selects its best insert location, thus there is at most one route
			InsertNodeIntoArc curIna = null;
			for(InsertNodeIntoArc ina : inas){
				if(ina.getRouteInserted()==route){
					curIna = ina;
					break;
				}
			}
			
			inas.remove(curIna);
			
//			when there is no order nodes left, i.e., the path is removed
			if(route.getArcs().size()==0 || route.getArcs().get(0)==null){
				continue;
			}
			
			InsertNodeIntoArc newIna = route.getBestInsertArcsForNodeLS(node, capacity);
			if(newIna!=null){
				inas.add(newIna);
			}
			
		}
	}

	/**
	 * if a route is changed, search nodes (only those who contains current ina with route) to update their InsertNodeInArc
	 * @param route
	 */
	private void updateSomeNodesInsertNodeInArc(Route route, WarehouseLoadingCapacity capacity){
		
		LinkedHashSet<OrderNode> curOrderNodes = new LinkedHashSet<OrderNode>(route.getOrderNodes());
		for(OrderNode node : insertNodeMap.keySet()){
			
			//for every node check if there is a right ina, if so, remove it
			if(curOrderNodes.contains(node)){
				continue;
			}
			
			//first need to find current route in the map
			LinkedHashSet<InsertNodeIntoArc> inas = insertNodeMap.get(node);
			InsertNodeIntoArc curIna = null;
			for(InsertNodeIntoArc ina : inas){
				if(ina.getRouteInserted()==route){
					curIna = ina;
					break;
				}
			}
			
			if(curIna!=null ){
				inas.remove(curIna);
				
			}
			
			//add the new ina
			InsertNodeIntoArc newIna = route.getBestInsertArcsForNodeLS(node, capacity);
			if(newIna!=null){
				inas.add(newIna);
			}
		}
	}
	
	/**
	 * if a route is changed, search over all nodes to update their ReplaceNode Map accordingly, 
	 * i.e., study if the route could have any impact on the node
	 * 1. remove the old ReplaceNode that contains the old route
	 */
	private void updateAllNodesReplaceNodeGivenRoute(Route route, WarehouseLoadingCapacity capacity){
		
		LinkedHashSet<OrderNode> curOrderNodes = new LinkedHashSet<OrderNode>(route.getOrderNodes());
		
//		if(curOrderNodes.size()==0){
//			return;
//		}
		
		for(OrderNode node : replaceNodeMap.keySet()){
			if(curOrderNodes.contains(node)){
				continue;
			}
			//here you would significantly reduce the number of nodes that correspond to the route, 
			//we need only some
			
//			HashMap<OrderNode, ReplaceNode> curReplaceNodeMap = new LinkedHashMap<OrderNode, ReplaceNode>();
//			replaceNodeMap.put(node, curReplaceNodeMap);
			HashMap<OrderNode, ReplaceNode> curReplaceNodeMap = replaceNodeMap.get(node);
			LinkedList<OrderNode> nodesToRemove = new LinkedList<OrderNode>();
			for(OrderNode n : curReplaceNodeMap.keySet()){
				if(curReplaceNodeMap.get(n).getReplacedRoute() == route){
					nodesToRemove.add(n);
				}
			}
			
			for(OrderNode n : nodesToRemove){
				curReplaceNodeMap.remove(n);
			}
			
			for(ReplaceNode rn: route.getReplacableNode(node, capacity)){
				curReplaceNodeMap.put(rn.getReplacedNode(), rn);
			}
		}
	}

	/**
	 * if a route is changed, search over all nodes to update their ReplaceNode Map accordingly, 
	 * i.e., study if the route could have any impact on the node
	 * 1. remove the old ReplaceNode that contains the old route
	 * @param route the route changed
	 * @param replacedNode the node replaced, it is necessary when the node is replaced, we need to create an object denoting moving the node back
	 */
	private void updateSomeNodesReplaceNodeGivenRoute(Route route, OrderNode replacedNode, WarehouseLoadingCapacity capacity){
		
		LinkedHashSet<OrderNode> curOrderNodes = new LinkedHashSet<OrderNode>(route.getOrderNodes());
		for(OrderNode node : replaceNodeMap.keySet()){
			
//			Set<OrderNode> curOrderNodesCopy = new HashSet<>(curOrderNodes);
//			curOrderNodesCopy.add(replacedNode);
//			curOrderNodesCopy.retainAll(replaceNodeMap.get(node).keySet());
//			
//			if(curOrderNodesCopy.size() == 0 && node!=replacedNode){
//				continue;
//			}
			
//			HashMap<OrderNode, ReplaceNode> curReplaceNodeMap = new LinkedHashMap<OrderNode, ReplaceNode>();
//			replaceNodeMap.put(node, curReplaceNodeMap);
			
			HashMap<OrderNode, ReplaceNode> curReplaceNodeMap = replaceNodeMap.get(node);
			LinkedList<OrderNode> nodesToRemove = new LinkedList<OrderNode>();
			for(OrderNode n : curReplaceNodeMap.keySet()){
				if(curReplaceNodeMap.get(n).getReplacedRoute() == route){
					nodesToRemove.add(n);
				}
			}
			
			for(OrderNode n : nodesToRemove){
				curReplaceNodeMap.remove(n);
			}
			
			if(curOrderNodes.contains(node)){
				continue;
			}
			
			for(ReplaceNode rn: route.getReplacableNode(node, capacity)){
				curReplaceNodeMap.put(rn.getReplacedNode(), rn);
			}
		}
	}
	
//	/**
//	 * update the replace node map
//	 * 1. use the ReplaceNode object
//	 * if a node in some route is replaced, we only look at those situations where the route is part of a replacenode
//	 * if a node is move to other route, you need to create new replacenode object back
//	 * @param routesChanged
//	 * @param routesNodeAdded
//	 */
//	private void updateCostMapReplaceNode(LinkedHashSet<Route> routesChanged, LinkedHashSet<Route> routesNodeAdded){
//		
//	}
	
	/**
	 * update the removeNode object
	 * 1. find out all the nodes whose routes are changed
	 * 2. change it accordingly
	 * @param routesChanged the route that is changed
	 */
	private void updateCostMapRemoveNode(Route routesChanged, WarehouseLoadingCapacity capacity){
		
		for(OrderNode node : routesChanged.getOrderNodes()){
			removeNodeMap.remove(node);
			RemoveNode rn = routesChanged.getRemoveNode(node, capacity);
			if(rn!=null){
				removeNodeMap.put(node, rn);
			}
		}
	}
	
	
	/**
	 * move a single node from one route to another to decrease the total cost
	 */
	private void updateMoveSingleNode(WarehouseLoadingCapacity capacity){
		
		//the route that is already examined
		LinkedHashSet<Route> examinedRoute = new LinkedHashSet<Route>();
		
		for(OrderNode node : insertNodeMap.keySet()){
			RemoveNode rm = removeNodeMap.get(node);
			
			if(rm==null){
				continue;
			}
			for(InsertNodeIntoArc ina : insertNodeMap.get(node)){
				if(ina.getCostChangeInsertedRoute()+rm.getCostChange() < 0){
					//a possible route change is found
					executePath(ina, capacity);
				}
			}
		}
	}
	
	/**
	 * execute a move
	 * @param swapPath the swap path
	 * @param ina
	 */
	private void executePath(InsertNodeIntoArc ina, WarehouseLoadingCapacity capacity){
		
		OrderNode nodeToRemove = ina.getNodeToInsert();
		RemoveNode rm = getRemoveNode(nodeToRemove);
		getNodeToRoute().get(nodeToRemove).updateRemoveLocalSearch(rm, capacity);
		ina.getRouteInserted().updateInsertLocalSearch(ina, capacity);
		
	}

	/**
	 * execute the path, either a swap map or a 
	 * @param swapPath the swap path
	 * @param ina
	 * @param removeNode
	 * @throws IOException 
	 */
	public void executePath(InsertNodeIntoArc ina, RemoveNode removeNode, SwapPath swapPath,
			WarehouseLoadingCapacity capacity) throws IOException{
		
		if(swapPath!=null){
			for(ReplaceNode rn : swapPath.getReplaceNodeList()){
				rn.getReplacedRoute().updateReplaceLocalSearch(rn, capacity);
			}
		}
		
		if(ina!=null){
			ina.getRouteInserted().updateInsertLocalSearch(ina, capacity);
		}
		
		if(removeNode!=null){
			removeNode.getRoute().updateRemoveLocalSearch(removeNode, capacity);
			if(removeNode.getRoute().getArcs().size()==0){
				getSoluRoutes().remove(removeNode.getRoute());
			}
		}
	}
	
//	private void executeCycle(SwapPath swapPath, WarehouseLoadingCapacity capacity){
//		for(ReplaceNode rn : swapPath.getReplaceNodeList()){
//			rn.getReplacedRoute().updateReplaceLocalSearch(rn, capacity);
//		}
//	}
	
	/**
	 * initialize the @nodeToRoute
	 */
	private void initializeNodeToRoute(){
		nodeToRoute = new LinkedHashMap<OrderNode, Route>();
		for(Route r : soluRoutes){
			for(OrderNode n : r.getOrderNodes()){
				nodeToRoute.put(n, r);
			}
		}
	}

	/**
	 * @return the soluRoutes
	 */
	public LinkedHashSet<Route> getSoluRoutes() {
		return soluRoutes;
	}
}
