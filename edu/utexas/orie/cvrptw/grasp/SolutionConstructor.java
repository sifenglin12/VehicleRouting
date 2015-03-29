package edu.utexas.orie.cvrptw.grasp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.instance.Order;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.instance.Store;
import edu.utexas.orie.cvrptw.instance.Warehouse;
import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.Node;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.output.Diagram;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.VRPSolution;

/**
 * implements the first stage of the GRASP, solution constructor
 * @author SifengLin
 *
 */
public class SolutionConstructor {
	
	boolean logInitializeInsertPointMap = false;
	boolean logAdoptOrphanNode = false;
	
	InstanceCVRPTW instance;
	Network network;
	String outputFolder;
	
	int numMinCostCandidate = 1;  //number of candidates, which are min cost ones to choose
	int randomSeedCandidateSelection = 82649274; //random seed for candidate selection
	
	double solutionTime = -1;
	double totalCost = -1;
	
	public SolutionConstructor(InstanceCVRPTW instance, Network network, String outputFolder) {
		super();
		this.instance = instance;
		this.network = network;
		this.outputFolder = outputFolder;
	}
	
	/**
	 * generate a solution
	 * @throws IOException 
	 */
	public VRPSolution generateSolution() throws IOException{
		
		double startTime = 1D*System.currentTimeMillis()/1000;
		
		WarehouseLoadingCapacity capacity = new WarehouseLoadingCapacity(instance.getLatestDepartTime());
		LinkedList<Route> curRoutes = generateSeedRoutes(capacity);
		
		BufferedWriter writerAdoptOrphanNode = null;
		if(logAdoptOrphanNode){
			writerAdoptOrphanNode = new BufferedWriter(new FileWriter(outputFolder + "/logAdoptOrphanNode.txt"));
		}

		//the insert point map for all the routes and insert point
		LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap = initializeInsertPointMap(capacity, curRoutes);
		
//		logAdoptOprhanNodeOne(writerAdoptOrphanNode, insertPointsMap);
		
		//first step, search for the orphon node
		while(insertPointsMap.size()>0){  //Is it possible to find some infeasible situation? how to stop it
			
			//if some node can not be inserted into any path, it is called orphon
			Route newRoute = adoptOrphanNode(insertPointsMap, capacity);
			if(newRoute!=null){
//				logAdoptOrphanNodeTwo(newRoute, writerAdoptOrphanNode);
				curRoutes.add(newRoute);
				updateInsertPointMapNewRoute(insertPointsMap, newRoute, capacity);
				continue;
			}
			
			//find the point to insert
			InsertPoint pointToUpdate = getMinPiCostInsertPoint(insertPointsMap);
			pointToUpdate.update(capacity);
			updateInserPointMapInsertNode(insertPointsMap, pointToUpdate, capacity);
		}
		
		if(writerAdoptOrphanNode!=null){
			writerAdoptOrphanNode.close();
		}
		
		writeSolutionRoute(outputFolder + "/solution.txt", curRoutes);
		
		verifyFeasibility(instance, curRoutes, capacity, outputFolder + "/logVerifySolutionFeasibility.txt");
//		improveWithinRoute(curRoutes);
		
		BufferedWriter writerWarehouse = new BufferedWriter(new FileWriter(outputFolder + "/warehosueOne.txt"));
		capacity.printWarehouseCapacity(curRoutes, writerWarehouse);		
		writerWarehouse.close();
		
		//improve all the routes to its local minimum
		LinkedList<Route> finalRoutes = new LinkedList<Route>(); 
		for(Route r : curRoutes){
			Route improvedRoute = r.getImprovedRoute();
			if(improvedRoute!=null){
				capacity.updateRoute(r.getOriginTime(), improvedRoute.getOriginTime(), r);
				r.changeRoute(improvedRoute);
			}
			finalRoutes.add(r);
		}
		
		plotSolution(outputFolder + "/initialSolution" ,finalRoutes, instance.getWarehouse());
		totalCost = Route.getTotalCost(finalRoutes);
		solutionTime = 1D*System.currentTimeMillis()/1000 - startTime;
		return new VRPSolution(instance, finalRoutes, capacity);
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
	 * verify that the solution is feasible
	 * @param routes: the routes of the solution
	 * @throws IOException 
	 */
	public static void verifyFeasibility(InstanceCVRPTW instance, Collection<Route> routes, WarehouseLoadingCapacity capacity, String logFile) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
		
		LinkedHashMap<Order, Boolean> fullfilled = new LinkedHashMap<Order, Boolean>();  //check if the order is fullfilled
		for(Store store : instance.getStores()){
			for(Order order : store.getDeliveryOrders()){
				fullfilled.put(order, false);
			}
			for(Order order : store.getSalvageOrders() ){
				fullfilled.put(order, false);
			}
		}
		
		for(Route route : routes){
			for(Arc arc : route.getArcs()){
				Node toNode = arc.getToNode();
				if(toNode instanceof OrderNode){
					fullfilled.put(((OrderNode) toNode).getOrder(), true);
				}
			}
		}
		
		writer.write("The following orders are not fullfilled\n");
		for(Order order : fullfilled.keySet()){
			if(!fullfilled.get(order)){
				writer.write(order + "\t");
			}
		}
		
		if(capacity!=null){
			capacity.writeWarehouseLoading(writer);
		}
		
		writer.close();
		
	}
	
	/**
	 * produce the log for orphon adoption
	 * @param writerAdoptOrphanNode
	 * @param insertPointsMap
	 * @throws IOException
	 */
	private void logAdoptOprhanNodeOne(BufferedWriter writerAdoptOrphanNode, LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap) throws IOException{
		if(logAdoptOrphanNode){
			for(OrderNode node : insertPointsMap.keySet()){
				LinkedHashMap<Route, InsertPoint> curMap = insertPointsMap.get(node);

				if(logInitializeInsertPointMap){
					writerAdoptOrphanNode.write(node.toString() + node.getTWString() + "\t");
					for(Route r : curMap.keySet()){
						writerAdoptOrphanNode.write("\t" + r);
						writerAdoptOrphanNode.newLine();
					}
					writerAdoptOrphanNode.newLine();
				}
			}
			writerAdoptOrphanNode.write("----------------------------------");
			writerAdoptOrphanNode.newLine();
			
		}
	}
	
	/**
	 * writing the log for oprphan adopting
	 * @throws IOException 
	 */
	private void logAdoptOrphanNodeTwo(Route newRoute, BufferedWriter writerAdoptOrphanNode) throws IOException{
		if(logAdoptOrphanNode){
			writerAdoptOrphanNode.write("-----------Adopt the orphon-------");
			writerAdoptOrphanNode.newLine();
			writerAdoptOrphanNode.write(newRoute.arcs.get(0).getToNode().toString());
			
			writerAdoptOrphanNode.newLine();
			writerAdoptOrphanNode.write("----------------------------------");
			writerAdoptOrphanNode.newLine();
			writerAdoptOrphanNode.flush();
		}
	}
	
	public static void plotSolution(String outputFile, Collection<Route> curRoutes, Warehouse warehouse) throws IOException{
		//plot the store
		
		LinkedHashSet<Store> stores = new LinkedHashSet<>();
		LinkedHashSet<Route> routes = new LinkedHashSet<Route>();
		for(Route r : curRoutes){
			if(r.getArcs().size()>=3){
				stores.addAll(r.getStores());
				routes.add(r);
			}
		}
		
		
		Diagram.plotSolution(outputFile, stores, routes, warehouse);
	}
	
	
	/**
	 * print the solution routes
	 * @param curRoutes
	 * @throws IOException 
	 */
	public static void writeSolutionRoute(String fileName, Collection<Route> curRoutes) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		
		writer.write("Total Cost\t" + Route.getTotalCost(curRoutes)+"\n");
		writer.write("WeightLimit: " + Parameter.getTruckWeightLimit());
		writer.newLine();
		writer.write("VolumeLimit: " + Parameter.getTruckVolumeLimit());
		writer.newLine();
		for(Route route : curRoutes){
			route.writeRouteDetails(writer);
		}
		writer.close();
	}

	/**
	 * print the solution summary
	 * @param curRoutes
	 * @throws IOException 
	 */
	private void writeSolutionSummary(LinkedHashSet<Route> curRoutes) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "/solutionSummary.txt"));
		writer.newLine();
		for(Route route : curRoutes){
			route.writeRouteDetails(writer);
		}
		writer.close();
	}
	
	/**
	 * update the map after a new route is found.  Specifically, updat the following
	 * 1. add new entry to each ordernode
	 * 2. for the old one, we need to double check that the loss of capacity due to 
	 *    the warehouse loading capacity for <newRoute> does not affect other route
	 * @param insertPointsMap
	 * @param newRoute
	 */
	private void updateInsertPointMapNewRoute(LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap,
			Route newRoute, WarehouseLoadingCapacity capacity){
		double startTimeRoute = newRoute.getOriginTime();
//		if(capacity.isAvailableSlotTime(startTimeRoute)){
//			return;
//		}
		
		//get the used up time start
		double startTime = capacity.getSlotWindowStart(startTimeRoute);
		//get the used up time end
		double endTime = capacity.getNextAvaiableTime(startTimeRoute);
		
		LinkedHashSet<InsertPoint> pointsToUpdate = getUpdatableInsertPointMapBetweenTimes(startTime, endTime, insertPointsMap, capacity);
		updateInserPoints(pointsToUpdate, insertPointsMap, capacity);
		
		//any insertion point that falls between these two times should be updated
		for(OrderNode node : insertPointsMap.keySet()){
			InsertPoint newPoint = newRoute.getInsertPoint(node, capacity);
			if(newPoint!=null){
				insertPointsMap.get(node).put(newRoute, newPoint);
			}
		}
	}
	
	/**
	 * execute the new insert point and update the time accordingly
	 * @param insertPointsMap
	 * @param point
	 * @param capacity
	 */
	private void updateInserPointMapInsertNode(LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap,
			InsertPoint point, WarehouseLoadingCapacity capacity){
		
		/*
		 * origin time is the earliest possible time, so adding a node is only going to shift it late
		 */
		insertPointsMap.remove(point.getNodeToInsert());
		double startTime = point.getNewOriginTime();
		double endTime = point.getRouteInserted().getOriginTime();
		
		//we need to double check, if between <startTime> and <endTime>, there is any available capacity to hold more trains
		LinkedHashSet<InsertPoint> pointsToUpdate = getUpdatableInsertPointMapBetweenTimes(startTime, endTime, insertPointsMap, capacity);
		
		pointsToUpdate.addAll(getInsertPointRoute(insertPointsMap, point.getRouteInserted()));
		
		//next, need to find the route 
		updateInserPoints(pointsToUpdate, insertPointsMap, capacity);
		
	}
	
	/**
	 * get the insert point that contains a specific route
	 * @param insertPointsMap
	 * @param route
	 */
	private LinkedHashSet<InsertPoint> getInsertPointRoute(LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap,
			Route updatedRoute){
		LinkedHashSet<InsertPoint> pointsToUpdate = new LinkedHashSet<InsertPoint>();
		for(OrderNode node : insertPointsMap.keySet()){
			LinkedHashMap<Route, InsertPoint> curMap = insertPointsMap.get(node);
			for(Route route : curMap.keySet()){
				pointsToUpdate.add(  curMap.get(route)  );
			}
		}
		
		return pointsToUpdate;
	}
	
	
	/**
	 * update the insert point between @startTime and @endTime, since the warehouse loading capacity between these two times are used up
	 * @param startTime
	 * @param endTime
	 * @param insertPointsMap
	 * @param capacity
	 */
	private LinkedHashSet<InsertPoint> getUpdatableInsertPointMapBetweenTimes(double startTime, double endTime, 
			LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap,
			WarehouseLoadingCapacity capacity){
		
		//any insertion point that falls between these two times should be updated
		LinkedHashSet<InsertPoint> pointsToUpdate = new LinkedHashSet<InsertPoint>();
		for(OrderNode node : insertPointsMap.keySet()){
			LinkedHashMap<Route, InsertPoint> curMap = insertPointsMap.get(node);
			for(Route route : curMap.keySet()){
				InsertPoint p = curMap.get(route);
				if(p.getNewOriginTime()>=startTime && p.getNewOriginTime()<endTime){
					pointsToUpdate.add(p);
				}
			}
		}
		
		return pointsToUpdate;
		//for all those point to update, I need to update their value
	}
	
	private void updateInserPoints(LinkedHashSet<InsertPoint> pointsToUpdate, 
			LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap,
			WarehouseLoadingCapacity capacity){
		for(InsertPoint point : pointsToUpdate){
			Route route = point.getRouteInserted();
			OrderNode node = point.getNodeToInsert();
			InsertPoint newPoint = route.getInsertPoint(node, capacity);
			if(newPoint!=null){
				insertPointsMap.get(node).put(route, newPoint);
			}
			else{
				insertPointsMap.get(node).remove(route);
			}
		}
	}
	
	
	/**
	 * find the orphan node (the node that can not be served by any existing route), and create a route for it
	 * @return
	 * @throws IOException 
	 */
	private Route adoptOrphanNode(LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap, 
			WarehouseLoadingCapacity capacity) throws IOException{
		
		for(OrderNode node : insertPointsMap.keySet()){
			
			if(insertPointsMap.get(node).size()==0){
				
				//how to update the map, after finding the route
				Route route = createNewRoute(node, capacity);
				insertPointsMap.remove(node);
				return route;
			}
		}
		
		return null;
		
	}
	
	/**
	 * get the best insert piont for a specific node
	 * @param insertPointsMap
	 * @return
	 */
	private InsertPoint getBestInserPoint(LinkedHashMap<Route, InsertPoint> insertPointsMap){
		InsertPoint minPoint = null;
		
		for(Route route : insertPointsMap.keySet()){
			InsertPoint curPoint = insertPointsMap.get(route);
			if(minPoint==null || curPoint.getOpportunityCost() < minPoint.getOpportunityCost()){
				minPoint = curPoint;
			}
		}
		
		return minPoint;
		
	}
	
	/**
	 * get the Pi Cost for each node
	 * @return
	 */
	private InsertPoint getMinPiCostInsertPoint(LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> insertPointsMap){
		LinkedHashMap<OrderNode, Double> piCostForNodes = new LinkedHashMap<OrderNode, Double>();
		LinkedHashMap<OrderNode, InsertPoint> minCostInsertPoints = new LinkedHashMap<OrderNode, InsertPoint>();
		
		for(OrderNode node : insertPointsMap.keySet()){
			LinkedHashMap<Route, InsertPoint> curInsertPointsMap = insertPointsMap.get(node);
			InsertPoint minPoint = getBestInserPoint(curInsertPointsMap);
			
			//we are sure that the minpoint is not null.  since it would be handle at the stage of adopt orphan node
			minCostInsertPoints.put(node, minPoint);
			piCostForNodes.put(node, getPiCost(curInsertPointsMap, minPoint));
		}
		
		//change it to several min cost ones
//		OrderNode minNode = null;
//		double minCost = Double.MAX_VALUE;
//		for(OrderNode node : piCostForNodes.keySet()){
//			double curCost = piCostForNodes.get(node);
//			if( curCost  < minCost){
//				minCost = curCost;
//				minNode= node;
//			}
//		}
		
		LinkedList<OrderNode> minNodes = getMinNodes(piCostForNodes);
		int indexSelected = (new Random(randomSeedCandidateSelection)).nextInt(minNodes.size());
		OrderNode nodeSelected = minNodes.get(indexSelected);
		return minCostInsertPoints.get(nodeSelected);
	}
	
	/**
	 * get the minimal node in of all the pi cost, the parameter is given by <numMinCostCandidate>
	 * @param piCostForNodes the cost for all nodes
	 * @return the <numMinCostCandidate> in the list
	 */
	private LinkedList<OrderNode> getMinNodes(LinkedHashMap<OrderNode, Double> piCostForNodes ){
		LinkedList<OrderNode> nodes = new LinkedList<OrderNode>();
		for(OrderNode nodeToInsert : piCostForNodes.keySet()){
			double curCost = piCostForNodes.get(nodeToInsert);
			int location = 0;
			for(OrderNode n : nodes){
				double cost = piCostForNodes.get(n);
				if(cost > curCost){
					break;
				}
				location++;
			}
			
			nodes.add(location, nodeToInsert);

			if(nodes.size()>numMinCostCandidate){
				nodes.removeLast();
			}
		}
		
		return nodes;
	}
	
	
	private double getPiCost(LinkedHashMap<Route, InsertPoint> curInsertPointsMap, InsertPoint minPoint){
		double cost = 0;
		for(Route route : curInsertPointsMap.keySet()){
			cost = cost + curInsertPointsMap.get(route).getOpportunityCost() - minPoint.getOpportunityCost();
		}
		
		return cost;
	}
	
	/**
	 * initialize the insert point map
	 * @param capacity the warehouse capacity object
	 * @param unassignedNode the unassigned node
	 * @param curRoutes the route found so far
	 * @return
	 * @throws IOException 
	 */
	private LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> initializeInsertPointMap(WarehouseLoadingCapacity capacity, 
			Collection<Route> curRoutes) throws IOException{
		
		BufferedWriter writer = null;
		
		if(logInitializeInsertPointMap){
			writer = new BufferedWriter(new FileWriter(outputFolder + "/logInitializeInserPiontMap.txt"));
		}
		
		LinkedHashSet<OrderNode> unassignedNodes = new LinkedHashSet<OrderNode>(network.getOrderNodes());
		
		for(Route route : curRoutes){
			unassignedNodes.remove(route.arcs.get(0).getToNode());
		}
		
		LinkedHashMap<OrderNode, LinkedHashMap<Route, InsertPoint>> inserPointMap = new LinkedHashMap<OrderNode, LinkedHashMap<Route,InsertPoint>>();
		for(OrderNode node : unassignedNodes){
			LinkedHashMap<Route, InsertPoint> curMap = new LinkedHashMap<Route, InsertPoint>();
			for(Route route: curRoutes){
				
				//the current route
				InsertPoint point = route.getInsertPoint(node, capacity);
				if(logInitializeInsertPointMap){
					writer.write(node.toString() + node.getTWString() + "\t");
					writer.write(route.toString());
					writer.write("\t");
					String costString = "---";
					if(point!=null){
						costString = point.getOpportunityCost()+"";
					}
					writer.write(costString);
					writer.newLine();
				}
				
				if(point!=null){
					curMap.put(route, point);
				}
			}
			inserPointMap.put(node, curMap);
		}

//		for(OrderNode node : unassignedNodes){
//			LinkedHashMap<Route, InsertPoint> curMap = new LinkedHashMap<Route, InsertPoint>();
//			
//			for(Route route: curRoutes){
//				//the current route
//				InsertPoint point = route.getInsertPoint(node, capacity);
//				if(point!=null){
//					curMap.put(route, point);
//				}
//			}
//			
//			if(logInitializeInsertPointMap){
//				writer.write(node.toString() + node.getTWString() + "\t");
//				for(Route r : curMap.keySet()){
//					writer.write("\t" + r);
//					writer.newLine();
//				}
//				writer.newLine();
//			}
//		}
			
		if(logInitializeInsertPointMap){
			writer.close();
		}
		
		return inserPointMap;
	}

	/**
	 * generate the seed routes
	 * @param capacity
	 * @return
	 */
	private LinkedList<Route> generateSeedRoutes(WarehouseLoadingCapacity capacity){
		
		LinkedList<OrderNode> unassignedNodes = new LinkedList<OrderNode>(network.getOrderNodes());
		
		LinkedList<Route> routes = new LinkedList<Route>();
		
		SeedGenerator gen = new SeedGenerator(instance, network);
		LinkedHashSet<Store> stores = gen.getSeedsMaxSumEachMinTimeHeuristic();
		
		//create routes from those stores
		
		for(Store store : stores){
			Order firstDeliveryOrder = store.getFirstDeliveryOrder();
			if(firstDeliveryOrder==null){
				continue;
			}
			
			OrderNode node = network.getOrderNodeFromOrder(firstDeliveryOrder);
			Route route = createNewRoute(node, capacity);
			
			routes.add(route);
			unassignedNodes.remove(node); 
			
		}
		
		return routes;
		
	}
	
	/**
	 * create a new route according to a OrderNode
	 * @return
	 */
	private Route createNewRoute(OrderNode node, WarehouseLoadingCapacity capacity){
		Arc originArc = network.getSource().getPostArc(node);
		Arc destArc = network.getSink().getPreArc(node);
		
		ArrayList<Arc> arcs = new ArrayList<Arc>();
		arcs.add(originArc);
		arcs.add(destArc);
		
		double originTime = Math.max(0, node.getStartTime() - originArc.getTotalTime()) ;
		//still need to gurantee that the starting time is within the 
		originTime = Math.min(originTime, instance.getLatestDepartTime());
		double waitCost = 0;
		double totalWeight = node.getWeight();
		double totalVolume = node.getCube();
		double totalTravelTime = originArc.getTravelTime() + destArc.getTravelTime();
		
		//need to double check if the origin time is feasible
		double nextAvailTime = Math.max(originTime, capacity.getNextAvaiableTime(originTime)) ;  //get the next feasible time, probably it is <originTime>
		if(nextAvailTime<=node.getEndTime()){
			Route route = new Route(arcs, nextAvailTime, waitCost, totalWeight, totalVolume, totalTravelTime);
			capacity.addVehicle(route.getOriginTime(), route);
			return route;
		}
		
		return null;
	}
	
}
