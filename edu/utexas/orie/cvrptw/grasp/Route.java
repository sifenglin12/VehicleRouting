package edu.utexas.orie.cvrptw.grasp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.instance.Store;
import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.Node;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.output.StringProcessor;
/**
 * Route is a set of movements of the truck.
 * Used in the first stage of the GRASP heuristic.
 * To guarantee that the driver idle time cost is minimized, we always gurantee that the driver depart as late as possible
 * @author SifengLin
 */
public class Route {
	
	int maxNumStores = 3;
	
	final int outputLength = 15;
	
	/**
	 * weight assigned to the available weight and volume
	 */
	double deltaOne = 0.4;
	double capacityCostFactor = 50;
	
	/**
	 * weight assigned to the cost
	 */
	double deltaTwo = 0.6;
	
	/**
	 * weight assigned to the time
	 */
	double deltaThree = 0;

	/**
	 * arcs that represent the movement of trucks
	 */
	ArrayList<Arc> arcs;

	/**
	 * the time to leave the warehouse
	 */
	double originTime;
	
	/**
	 * the waiting cost associated with the route
	 */
	double waitCost;
	
	double totalWeight;
	double totalVolume;
	double totalTravelTime;
	
	LinkedHashSet<Store> stores;
	
	/**
	 * change the route according to the new data
	 * @param newRoute
	 */
	public void changeRoute(Route newRoute){
		if(newRoute==null){
			return;
		}
		arcs = newRoute.arcs;
		originTime = newRoute.getOriginTime();
		waitCost = newRoute.waitCost;
		totalWeight = newRoute.totalWeight;
		totalVolume = newRoute.totalVolume;
		totalTravelTime = newRoute.totalTravelTime;
		stores = newRoute.stores;
	}
	
	/**
	 * if only the arcs are given
	 * @param arcs
	 */
	public Route(ArrayList<Arc> arcs){
		this(arcs, null);
	}

	/**
	 * if only the arcs are given
	 * @param arcs
	 */
	public Route(ArrayList<Arc> arcs, ArrayList<OrderNode> orderNodes){
		this.arcs = arcs;
		this.originTime = getEarliestTimeVerfify();
		if(this.originTime<0){
			return;
		}
		this.waitCost = getWaitCostVerify(this.originTime, this.arcs);
		stores = new LinkedHashSet<Store>();
		
		this.totalTravelTime = 0;
		this.totalVolume = 0;
		this.totalWeight = 0;
		
		if(orderNodes==null){
			orderNodes = new ArrayList<OrderNode>();
			for(Arc arc : arcs){
				Node toNode = arc.getToNode();
				if(toNode instanceof OrderNode){
					orderNodes.add((OrderNode) toNode);
				}
			}
		}
		
		for(OrderNode node: orderNodes){
			this.totalWeight += node.getWeight();
			this.totalVolume += node.getCube();
			stores.add(node.getOrder().getStore());
		}
		
		for(Arc arc : arcs){
			this.totalVolume += arc.getTotalSeparationVolume();
			this.totalTravelTime += arc.getTotalTime();
		}
		
	}
	
	public Route(ArrayList<Arc> arcs, double originTime, double waitCost,
			double totalWeight, double totalVolume, double totalTravelTime) {
		super();
		this.arcs = arcs;
		this.originTime = originTime;
		this.waitCost = waitCost;
		this.totalWeight = totalWeight;
		this.totalVolume = totalVolume;
		this.totalTravelTime = totalTravelTime;
		stores = new LinkedHashSet<Store>();
		for(int i=0; i<arcs.size()-1; i++){
			stores.add(((OrderNode)arcs.get(i).getToNode()).getOrder().getStore());
		}
	}
	
	/**
	 * update the insert
	 * @param point
	 */
	public void updateInsert(InsertPoint point, WarehouseLoadingCapacity capacity){
		Arc splitedArc = point.getArcInserted(); 
		OrderNode node = point.getNodeToInsert();
		int index = arcs.indexOf(splitedArc);
		arcs.remove(splitedArc);
		LinkedList<Arc> newArcs = new LinkedList<Arc>();
		newArcs.add( splitedArc.getFromNode().getPostArc(node));
		newArcs.add( node.getPostArc(splitedArc.getToNode()) );
		
		arcs.addAll(index, newArcs);
		
		/*
		 * can the time be increased? it is not clear
		 */
//		capacity.addVehicle(point.getNewOriginTime(), originTime, this);
		capacity.updateRoute(originTime, point.getNewOriginTime(), this);
		updateOriginTimeWaitCost(point.getNewOriginTime());
		
		stores.add(node.getOrder().getStore());
		for(Arc a : newArcs){
			totalTravelTime += a.getTravelTime();
			totalVolume += a.getTotalSeparationVolume();
		}

		totalTravelTime -= splitedArc.getTravelTime();
		totalWeight += point.getNodeToInsert().getWeight();
		totalVolume += point.getNodeToInsert().getCube();  
		totalVolume -= splitedArc.getTotalSeparationVolume();
		
	}

	/**
	 * update the insert in local search
	 * @param ina
	 */
	public void updateInsertLocalSearch(InsertNodeIntoArc ina, WarehouseLoadingCapacity capacity){
		Arc splitedArc = ina.getArcInserted(); 
		OrderNode node = ina.getNodeToInsert();
		int index = arcs.indexOf(splitedArc);
		arcs.remove(splitedArc);
		LinkedList<Arc> newArcs = new LinkedList<Arc>();
		newArcs.add( splitedArc.getFromNode().getPostArc(node));
		newArcs.add( node.getPostArc(splitedArc.getToNode()) );
		
		arcs.addAll(index, newArcs);
//		capacity.addVehicle(ina.getNewOriginTime(), originTime, this);
		capacity.updateRoute(originTime, ina.getNewOriginTime(), this);

		updateOriginTimeWaitCost(ina.getNewOriginTime());
		
		stores.add(node.getOrder().getStore());
		for(Arc a : newArcs){
			totalTravelTime += a.getTravelTime();
			totalVolume += a.getTotalSeparationVolume();
		}
		totalTravelTime -= splitedArc.getTravelTime();
		totalWeight += ina.getNodeToInsert().getWeight();
		totalVolume += ina.getNodeToInsert().getCube();
		totalVolume -= splitedArc.getTotalSeparationVolume();
		
	}
	
	/**
	 * get the total cost of a collection of routes
	 * @return
	 */
	public static double getTotalCost(Collection<Route> routes){
		
		double cost = 0;
		for(Route route : routes){
			cost += route.getTotalCost();
		}
		
		return cost;
	}

	/**
	 * get the total cost of a collection of routes
	 * @return
	 */
	public static double getTotalTravelCost(Collection<Route> routes){
		
		double cost = 0;
		for(Route route : routes){
			cost += route.getTotalTransitCost();
		}
		
		return cost;
	}

	/**
	 * get the total cost of a collection of routes
	 * @return
	 */
	public static double getTotalWaitCost(Collection<Route> routes){
		
		double cost = 0;
		for(Route route : routes){
			cost += route.getTotalWaitCost();
		}
		
		return cost;
	}
	
	

	/**
	 * update the insert in local search
	 * @param ina
	 */
	public void updateRemoveLocalSearch(RemoveNode rn, WarehouseLoadingCapacity capacity){
		int removeLocation = rn.getNodeLocation();
		Arc arcOne = arcs.remove(removeLocation);
		Arc arcTwo = arcs.remove(removeLocation);
		
		//if there is no arcs in the route, remove it
		if(arcs.size()==0){
			capacity.updateRoute(0, 0, this);
			return;
		}
		
		Arc newArc = arcOne.getFromNode().getPostArc(arcTwo.getToNode());
		arcs.add(removeLocation, newArc);
		
		capacity.updateRoute(originTime, rn.getNewOriginTime(), this);
		if(newArc!=null){
			updateOriginTimeWaitCost(rn.getNewOriginTime());
			totalTravelTime += newArc.getTravelTime();
			totalTravelTime -= arcOne.getTravelTime();
			totalTravelTime -= arcTwo.getTravelTime();
			
			totalWeight -= rn.getNodeToRemove().getWeight();
			
			totalVolume -= rn.getNodeToRemove().getCube();
			totalVolume += newArc.getTotalSeparationVolume();
			totalVolume -= arcOne.getTotalSeparationVolume();
			totalVolume -= arcTwo.getTotalSeparationVolume();
			
			stores = new LinkedHashSet<Store>();
			for(OrderNode n : getOrderNodes()){
				stores.add(n.getOrder().getStore());
			}
		}
		
	}

	/**
	 * update the insert in local search
	 * @param ina
	 */
	public void updateReplaceLocalSearch(ReplaceNode replaceNode, WarehouseLoadingCapacity capacity){
		
		int replaceLocation = replaceNode.getReplacedNodeLocation();
		
		Arc arcOne = arcs.remove(replaceLocation);
		Arc arcTwo = arcs.remove(replaceLocation);
		
		Arc newArcOne = arcOne.getFromNode().getPostArc(replaceNode.getNodeToReplace());
		Arc newArcTwo = replaceNode.getNodeToReplace().getPostArc(arcTwo.getToNode());
		
		arcs.add(replaceLocation, newArcTwo);
		arcs.add(replaceLocation, newArcOne);
		
		capacity.updateRoute(originTime, replaceNode.getNewOriginTime(), this);
		
		updateOriginTimeWaitCost(replaceNode.getNewOriginTime());
		
		totalTravelTime += newArcOne.getTravelTime();
		totalTravelTime += newArcTwo.getTravelTime();
		totalTravelTime -= arcOne.getTravelTime();
		totalTravelTime -= arcTwo.getTravelTime();
		
		totalWeight -= replaceNode.getReplacedNode().getWeight();
		totalWeight += replaceNode.getNodeToReplace().getWeight();
		
		totalVolume -= replaceNode.getReplacedNode().getCube();
		totalVolume += replaceNode.getNodeToReplace().getCube();
		
		totalVolume += newArcOne.getTotalSeparationVolume();
		totalVolume += newArcTwo.getTotalSeparationVolume();
		totalVolume -= arcOne.getTotalSeparationVolume();
		totalVolume -= arcTwo.getTotalSeparationVolume();
		
		stores = new LinkedHashSet<Store>();
		for(OrderNode n : getOrderNodes()){
			stores.add(n.getOrder().getStore());
		}
	}
	
	/**
	 * update the orign time and wait cost
	 */
	private void updateOriginTimeWaitCost(double earliestTime){
		
		this.originTime = earliestTime;
		double startTime = earliestTime;
		double cost = 0; 
		double arriveTime; 
		for(Arc arc : arcs){
			arriveTime = startTime + arc.getTotalTime();
			startTime = Math.max(arc.getToNode().getStartTime(), arriveTime);
			cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();
		}
		waitCost = cost;
	}
	
	/**
	 * get the wait cost, according to the starting time
	 * Note that this is only for verification purposes
	 */
	public static double getWaitCostVerify(double startTime, Collection<Arc> arcsInPath){
//		double startTime = this.originTime;
		double cost = 0; 
		double arriveTime; 
		for(Arc arc : arcsInPath){
			arriveTime = startTime + arc.getTotalTime();
			startTime = Math.max(arc.getToNode().getStartTime(), arriveTime);
			cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();
		}
		
		return cost;
	}
	
	/**
	 * get the total transportation cost associated with the route
	 * @return
	 */
	public static double getTransitCostVerify(Collection<Arc> arcsInPath){
		double cost = 0;
		for(Arc arc : arcsInPath){
			cost += arc.getCost();
		}
		
		return cost;
	}
	
	/**
	 * write the header of the routes
	 * @param writer
	 * @throws IOException 
	 */
	public static void writeRouteSummaryHeader(BufferedWriter writer) throws IOException{
		writer.write("TotalCost," );
		writer.write("TransitionCost," );
		writer.write("WaitCost," );
		writer.write("Weight,");
		writer.write("Volume," );
		writer.write("TotalTime,");
		writer.write("DriveTime,");
		writer.newLine();
	}

	/**
	 * write the header of the routes
	 * @param writer
	 * @throws IOException 
	 */
	public void writeRouteSummary(BufferedWriter writer) throws IOException{
		writer.write(getTotalCost() + "," );
		writer.write(getTotalTransitCost() + "," );
		writer.write(getTotalWaitCost() + "," );
		writer.write(getTotalWeight() + ",");
		writer.write(getTotalVolume() + "," );
		writer.write(getTotalTime() + ",");
		writer.write(getDriveTime() + ",");
		writer.newLine();
	}
	
	/**
	 * get the driving time of the route
	 */
	public double getDriveTime(){
		double driveTime = 0;
		for(Arc arc : arcs){
			driveTime += arc.getTravelTime();
		}
		
		return driveTime;
	}
	
	/**
	 * report the details of the route
	 * @param writer
	 * @throws IOException 
	 */
	public void writeRouteDetails(BufferedWriter writer) throws IOException{
		//write reoute introduction
		writer.write("Cost:" + getTotalCost() + " \t");
		writer.write("Weight:" + getTotalWeight() + " \t");
		writer.write("Volume:" + getTotalVolume() + " \t");
		writer.write("TransitCost:" + getTotalTransitCost() + " \t");
		writer.write("WaitCost:" + getTotalWaitCost() + " \t");
		writer.write("WaitCostVerify:" + getWaitCostVerify(this.originTime, this.arcs) + " \t");
		writer.newLine();
		
		String additional = "\t";
		writer.write(additional + getString("Node") );
		writer.write(getString("weight"));
		writer.write(getString("volume"));
		writer.write(getString("arriveTime"));
		writer.write(getString("servTime"));
		writer.write(getString("winStart"));
		writer.write(getString("winEnd"));
		writer.write(getString("totalTime"));
		writer.newLine();
		double curTime = originTime;
		
		boolean isAllTimeInWindow = true;
		double totalDrivingTime = 0;
		for(int i=0; i<arcs.size(); i++){
			Arc arc = arcs.get(i);
			Node fromNode = arc.getFromNode();
			double weight = fromNode.getWeight();
			double volume = fromNode.getCube();
			double arriveTime = curTime;
			double startServiceTime = Math.max(arriveTime, fromNode.getStartTime());
			double winStart = fromNode.getStartTime();
			double winEnd = fromNode.getEndTime();
			writer.write(additional + getString(fromNode.toString()) );
			writer.write(getString(weight));
			writer.write(getString(volume));
			writer.write(getString(arriveTime));
			writer.write(getString(startServiceTime));
			writer.write(getString(winStart));
			writer.write(getString(winEnd));
			writer.write(getString(arc.getTotalTime()));
			writer.newLine();
			totalDrivingTime += arc.getTravelTime();
			
			if(i==0 || !arcs.get(i-1).isWithinSameStore()){
				if(startServiceTime + 1e-6 < winStart || startServiceTime > winEnd + 1e-6){
					isAllTimeInWindow = false;
				}
			}
			
			curTime = startServiceTime + arc.getTotalTime();
			writer.flush();
		}
		//check if everything is within the time window
//		String timeWindow = isAllTimeInWindow ? "WithinTW" : "ViolateTW";
//		writer.write(additional+timeWindow+"\n");
		
		if(!isAllTimeInWindow){
			System.out.println("ERROR: there is a node outside its time window for route " + toString());
		}
		if(getTotalWeight()>Parameter.getTruckWeightLimit()){
			System.out.println("ERROR: the truck weight limit is violated " + toString());
		}
		if(getTotalVolume() > Parameter.getTruckVolumeLimit()){
			System.out.println("ERROR: the truck volume limit is violated " + toString());
		}
		if( totalDrivingTime > Parameter.getDrivingTimeLimitPerRouteHour()){
			System.out.println("ERROR: the driving time limit is violated " + toString());
		}
		if(curTime - originTime > Parameter.getTotalTimeLimitPerRouteHour()){
			System.out.println("ERROR: the total time limit is violated " + toString());
		}
	}
	
	/**
	 * get the duration of the route, from and to the warehouse
	 * @return
	 */
	public double getTotalTime(){
		
		double curTime = originTime;
		for(Arc arc : arcs){
			Node fromNode = arc.getFromNode();
			double arriveTime = curTime;
			double startServiceTime = Math.max(arriveTime, fromNode.getStartTime());
			curTime = startServiceTime + arc.getTotalTime();
		}
		
		return curTime - originTime;
	}
	
	/**
	 * get the total wait cost of the route
	 * @return
	 */
	public double getTotalWaitCost(){
		
//		double cost = 0;
//		double curTime = originTime;
//		for(Arc arc : arcs){
//			Node fromNode = arc.getFromNode();
//			double arriveTime = curTime;
//			double startServiceTime = Math.max(arriveTime, fromNode.getStartTime());
//			
//			cost = cost + (startServiceTime - arriveTime)*Parameter.getDriverIdleCost();
//			
//			curTime = startServiceTime + arc.getTotalTime();
//		}
//		
//		return cost;
		
//		double cost = 0; 
//		double arriveTime;
//		double startTime = originTime;
//		for(Arc arc : arcs){
//			arriveTime = startTime + arc.getTotalTime();
//			startTime = Math.max(arc.getToNode().getStartTime(), arriveTime);
//			cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();
//		}
//		
//		return cost;
		
		return getWaitCostVerify(originTime, arcs);
	}
	
	/**
	 * get the total cost of the current route
	 * @return
	 */
	public double getTotalCost(){
		
		return getTotalWaitCost() + getTotalTransitCost();
	}
	
	/**
	 * get the output String
	 * @param value
	 * @return
	 */
	public String getString(double value){
		return StringProcessor.getString(value, outputLength)+" ";
	}

	/**
	 * get the output String
	 * @param value
	 * @return
	 */
	public String getString(String value){
		return StringProcessor.getString(value, outputLength)+" ";
	}
	
	/**
	 * get the total volume consumped by the path, including the volume for separation
	 * @return
	 */
	public double getTotalVolume(){
		double volume = 0;
		for(Arc arc : arcs){
			volume += arc.getToNode().getCube();
			volume += arc.getTotalSeparationVolume();
		}
		
		return volume;
	}
	
	/**
	 * get total cost
	 * @param writer
	 * @return
	 */
	public double getTotalTransitCost(){
		double cost = 0;
		for(Arc arc : arcs){
			cost += arc.getCost();
		}
		
		return cost;
	}

	/**
	 * get the insert point, i.e., the one with the min cost across all arcs
	 * @param node the node to insert
	 * @param warehouseCapacity the corresponding warehouse capacity
	 * @return
	 */
	public InsertPoint getInsertPoint(OrderNode node, WarehouseLoadingCapacity warehouseCapacity){
		LinkedHashMap<Arc, Double> earliestTimes = getInsertableArc(node, warehouseCapacity);
		LinkedHashMap<Arc, Double> inserCosts = getInsertOpportunityCost(node, earliestTimes);
		
		Arc arc = null;
		double curCost = Double.MAX_VALUE;
		for(Arc a : inserCosts.keySet()){
			double c = inserCosts.get(a);
			if( c < curCost ){
				arc = a;
				curCost = c;
			}
		}
		
		return arc==null ? null : new InsertPoint(node, this, arc, earliestTimes.get(arc), curCost);
	}

	/**
	 * get the insert arc in local search, i.e., the one with the cost across all arcs
	 * Note that we do not consider the warehouse capacity here, it would be considered after the local search solution is given
	 * @param node the node to insert
	 * @return
	 */
	public InsertNodeIntoArc getBestInsertArcsForNodeLS(OrderNode node, WarehouseLoadingCapacity capacity){
		LinkedHashMap<Arc, Double> earliestTimes = getInsertableArc(node, capacity);
//		LinkedList<InsertNodeIntoArc> insertArcs = new LinkedList<InsertNodeIntoArc>();
		InsertNodeIntoArc bestNodeLocation = null;
		
		for(Arc arc : earliestTimes.keySet()){
			double newOriginTime = earliestTimes.get(arc);
			double cost = getInsertWaitCostIncrease(node, arc, newOriginTime) + getInsertMoveCostIncrease(node, arc);
			
			if(bestNodeLocation==null || cost<bestNodeLocation.getCostChangeInsertedRoute()){
				bestNodeLocation = new InsertNodeIntoArc(node, this, arc, newOriginTime, cost);
			}
		}
		return bestNodeLocation;
	}
	
	/**
	 * get the removable node from current route
	 * @param node
	 * @return
	 */
	public RemoveNode getRemoveNode(OrderNode node, WarehouseLoadingCapacity capacity){
		
		ArrayList<Arc> newArcs = new ArrayList<Arc>();
		
		int location = -1;
		for(int i=0; i<arcs.size(); i++){
			if(arcs.get(i).getToNode()==node){
				location = i;
				
				Arc newArc = arcs.get(i).getFromNode().getPostArc(arcs.get(i+1).getToNode());
				if(newArc!=null){
					newArcs.add(newArc);
				}
				i++;
			}
			else{
				newArcs.add(arcs.get(i));
			}
		}
		
		double earliestTimeDestination = getEarliestEndTimeDestination(newArcs);
		double earlistTimeOrign = getEarlistStartOrigin(earliestTimeDestination, newArcs);
		
		earlistTimeOrign = Math.max(earlistTimeOrign, capacity.getNextAvaiableTime(earlistTimeOrign));
		
		double costChange = newArcs.size()==0 ? -1* getTotalCost() : getTotalCost(earlistTimeOrign, newArcs) - getTotalCost();
		
		return new RemoveNode(node, this, earlistTimeOrign, costChange, location);
		
	}

//	/**
//	 * get the insert arc in local search, i.e., the one with the cost across all arcs
//	 * Note that we do not consider the warehouse capacity here, it would be considered after the local search solution is given
//	 * @param node the node to insert
//	 * @return
//	 */
//	public LinkedList<InsertNodeIntoArc> getReplaceNodeForNodeLS(OrderNode node){
////		LinkedHashMap<Arc, Double> earliestTimes = getInsertableArc(node, null);
////		LinkedList<InsertNodeIntoArc> insertArcs = new LinkedList<InsertNodeIntoArc>();
////		
////		for(Arc arc : earliestTimes.keySet()){
////			double newOriginTime = earliestTimes.get(arc);
////			double cost = getInsertWaitCostIncrease(node, arc, newOriginTime);
////			InsertNodeIntoArc ia = new InsertNodeIntoArc(node, this, arc, newOriginTime, cost);
////			insertArcs.add(ia);
////		}
////		return insertArcs;
//	}
//	
	
	/**
	 * get the set of arcs that the node could insert into
	 * @param node the node to insert
	 * @param warehouseCapacity 
	 * @return a map that contains all the insertable arcs, and the corresponding earliest time from the warehouse
	 */
	private LinkedHashMap<Arc, Double> getInsertableArc(OrderNode node, WarehouseLoadingCapacity warehouseCapacity){
		
		ArrayList<Arc> candidateArcs = arcs;
		
		LinkedHashMap<Arc, Double> timeMap = new LinkedHashMap<Arc, Double>();
		
		if( totalWeight + node.getWeight() > Parameter.getTruckWeightLimit() 
				|| totalVolume + node.getCube() > Parameter.getTruckVolumeLimit()
				|| isMaxStoreExceed(node)){
			return timeMap;
		}
		
		for(Arc arc : candidateArcs){
			double insertableTime = insertableTime(node, warehouseCapacity, arc);
			if(insertableTime<0){
				continue;
			}
			timeMap.put(arc, insertableTime);
			
		}
		return timeMap;
	}
	
	/**
	 * check if the maximum number of stores constrain would be violated after node is added to the route
	 * @param node
	 */
	private boolean isMaxStoreExceed(OrderNode node){
		return ! (stores.contains(node.getOrder().getStore()) || stores.size()<maxNumStores ); 
	}
	
	private LinkedHashMap<Arc, Double> getInsertOpportunityCost(OrderNode node, LinkedHashMap<Arc, Double> earliestTimeMap ){
		
		LinkedHashMap<Arc, Double> costCapacity = getCostCapacity(node);
		LinkedHashMap<Arc, Double> costInsert = new LinkedHashMap<Arc, Double>();
		
		for(Arc arc : earliestTimeMap.keySet()){
			double costTwo = getInsertMoveCostIncrease(node, arc);
			costTwo += getInsertWaitCostIncrease(node, arc, earliestTimeMap.get(arc));
			
			double cost = costCapacity.get(arc) * deltaOne*capacityCostFactor + costTwo * deltaTwo;
			costInsert.put(arc, cost);
		}
		return costInsert;
		
	}
	
	/**
	 * get the cost one, which is based on the capacity
	 * you need to take into account the volume reduction due to the separation
	 * @param node
	 * @return a map of values, which corresponds to insert it to every possible point
	 */
	private LinkedHashMap<Arc, Double> getCostCapacity(OrderNode node){
		LinkedHashMap<Arc, Double> costOnes = new LinkedHashMap<Arc, Double>();
		/*
		 * 1.get the min available capacity 
		 * since no weight or volume for the salvage order is given, it is always the last order
		 */
		double availWeight = Parameter.getTruckWeightLimit();
		double availVolume = Parameter.getTruckVolumeLimit();
		
		for(Arc arc : arcs){
			double curCost = getWeightVolumeCost(availVolume, Parameter.getTruckVolumeLimit(), node.getCube())
					+ getWeightVolumeCost(availWeight, Parameter.getTruckWeightLimit(), node.getWeight());
			curCost = curCost /2;
			costOnes.put(arc, curCost);

			availVolume = availVolume - getVolumeConsumpation(arc);
			availWeight = availWeight - getWeightConsumpation(arc);
		}
		
		return costOnes;
	}

	/**
	 * get the earliest time that the truck could leave the warehouse 
	 * @return
	 */
	private double getEarliestTimeVerfify(){
		
		//get the earliest time at the destination
		double earliestTimeDestination = getEarliestEndTimeDestination(arcs);
		
		double startTime = getEarlistStartOrigin(earliestTimeDestination, arcs);
		
		if(earliestTimeDestination - startTime > Parameter.getTotalTimeLimitPerRouteHour()){
			return -1;
		}
		else{
			return startTime;
		}
	}
	
	/**
	 * check if <node> could be inserted into the head node.  Specifically, we check
	 * 1. If the time is appropriate
	 * 2. If the capacity meet the requirement
	 * @param node the node to be insertable
	 * @param warehouseCapacity current warehouse capacity. if the warehouseCapacity is null, it means do not need to consider it
	 * @param splitedArc the arc to be splited
	 * @return the earliest time to start the new path; return -1 if the insertion is not possible
	 */
	private double insertableTime(OrderNode node, WarehouseLoadingCapacity warehouseCapacity, Arc splitedArc){
		Arc arcOne = splitedArc.getFromNode().getPostArc(node);
		Arc arcTwo = splitedArc.getToNode().getPreArc(node);
		
		if(arcOne==null
				|| arcTwo==null
				|| totalTravelTime - splitedArc.getTravelTime() + arcOne.getTravelTime() + arcTwo.getTravelTime() > Parameter.getDrivingTimeLimitPerRouteHour()
				){
			return -1;
		}
		double totalSeparation = arcOne.getTotalSeparationVolume();
		totalSeparation += arcTwo.getTotalSeparationVolume();

		if(totalVolume + totalSeparation + node.getCube() > Parameter.getTruckVolumeLimit() ){   
			return -1;
		}

		
		ArrayList<Arc> curArcs = new ArrayList<Arc>();
		for(Arc arc : arcs){
			if(arc==splitedArc){
				curArcs.add(arcOne);
				curArcs.add(arcTwo);
			}
			else{
				curArcs.add(arc);
			}
		}
		//given the above start time
		double earliestTimeDestination = getEarliestEndTimeDestination(curArcs);
		
		if(earliestTimeDestination<0){
			return -1;
		}
		
		double earliestTimeOrigin = getEarlistStartOrigin(earliestTimeDestination, curArcs);
		
		//now check if the train has any capacity to use between current ealiest time and new ealiest time
		if(earliestTimeDestination - earliestTimeOrigin > Parameter.getTotalTimeLimitPerRouteHour()){
			return -1;
		}
		else if(warehouseCapacity==null || warehouseCapacity.isAvailable(earliestTimeOrigin, originTime)){
			
			return earliestTimeOrigin;
		}
		else{
			return -1;
		}
	}
	
	public void printRouteDetails(double earliestTimeDestination, ArrayList<Arc> curArcs){
		double curTime = earliestTimeDestination;
		for(int i=0; i<curArcs.size(); i++){
			Arc arc = curArcs.get(i);
			Node fromNode = arc.getFromNode();
			double arriveTime = curTime;
			double startServiceTime = Math.max(arriveTime, fromNode.getStartTime());
			double winStart = fromNode.getStartTime();
			double winEnd = fromNode.getEndTime();
			System.out.print(getString(fromNode.toString()) );
			System.out.print(getString(arriveTime));
			System.out.print(getString(startServiceTime));
			System.out.print(getString(winStart));
			System.out.print(getString(winEnd));
			System.out.println();
			curTime = startServiceTime + arc.getTotalTime();
		}
		System.out.println();
	}
	
	/**
	 * get the set of replacable nodes corresponding to OderNode
	 * @param node
	 * @return
	 */
	public LinkedList<ReplaceNode> getReplacableNode(OrderNode node, WarehouseLoadingCapacity capacity){
		LinkedList<ReplaceNode> replacableNodes = new LinkedList<ReplaceNode>();
		
		for(int i=0; i<arcs.size()-1; i++){
			ReplaceNode rn = insertableTimeReplaceNodeLS(node, i, capacity);
			if(rn!=null){
				replacableNodes.add(rn);
			}
		}
		
		return replacableNodes;
	}
	
	/**
	 * check if <node> could replace the end of the the arc.  Specifically, we check
	 * 1. If the time is appropriate
	 * 2. If the capacity meet the requirement
	 * @param node the node to be insertable
	 * @param endNoteArc the end of of the arc would be replaced
	 * @return the earliest time to start the new path; return -1 if the insertion is not possible
	 */
	private ReplaceNode insertableTimeReplaceNodeLS(OrderNode node, int insertIndex, WarehouseLoadingCapacity capacity){
		
		if(insertIndex<0 || insertIndex>arcs.size()-1 || isMaxStoreExceed(node)){
			//the last arc, i.e. i= arcs.size()-1 would not be selected, since it is the warehouse
			return null;
		}
		
		Arc arcOne = arcs.get(insertIndex).getFromNode().getPostArc(node);
		Arc arcTwo = node.getPostArc(arcs.get(insertIndex+1).getToNode());
		
		if(arcOne==null
				|| arcTwo==null
				){
			return null;
		}
		
		double totalVolume = 0;
		double totalWeight = 0;
		double totalDriveTime = 0;
		
		ArrayList<Arc> curArcs = new ArrayList<Arc>();
		for(int i=0; i<arcs.size(); i++){
			if(i==insertIndex){
				curArcs.add(arcOne);
			}
			else if(i-1==insertIndex){
				curArcs.add(arcTwo);
			}
			else{
				curArcs.add(arcs.get(i));
			}
		}
		
		//check the weight and volume limit
		for(Arc arc : curArcs){
			totalVolume += arc.getToNode().getCube();
			totalVolume += arc.getTotalSeparationVolume();
			totalWeight += arc.getToNode().getWeight();
			totalDriveTime += arc.getTravelTime();
		}
		if(totalVolume>Parameter.getTruckVolumeLimit() || totalWeight > Parameter.getTruckWeightLimit()
				|| totalDriveTime > Parameter.getDrivingTimeLimitPerRouteHour() ){
			return null;
		}
		
		//given the above start time
		double earliestTimeDestination = getEarliestEndTimeDestination(curArcs);
		if(earliestTimeDestination<0){
			return null;
		}
		
		double earliestTimeOrigin = getEarlistStartOrigin(earliestTimeDestination, curArcs);
		
		earliestTimeOrigin = Math.max(earliestTimeOrigin, capacity.getNextAvaiableTime(earliestTimeOrigin));
		
		//now check if the train has any capacity to use between current ealiest time and new ealiest time
		if(earliestTimeDestination - earliestTimeOrigin > Parameter.getTotalTimeLimitPerRouteHour()){
			return null;
		}
		else{
			
			double costIncrease = getTotalCost(earliestTimeOrigin, curArcs) - getTotalCost();
			ReplaceNode rn = new ReplaceNode(node, insertIndex, this, earliestTimeOrigin, costIncrease);
			return rn;
		}
	}
	
	/**
	 * get the total cost of a path, given the <startTime> and the <arcsInPath>
	 * @param startTime
	 * @param arcsToInPath
	 * @return
	 */
	public static double getTotalCost(double startTime, Collection<Arc> arcsInPath){
		return getWaitCostVerify(startTime, arcsInPath) + getTransitCostVerify(arcsInPath);
	}
	
	/**
	 * get the earlist time that a vhichel could start from the origin, given the arcs, and earliestTimeDestination
	 * @param earliestTimeDestination the time to arrive at the destination
	 * @param curArcs current arcs
	 * @return
	 */
	private static double getEarlistStartOrigin(double earliestTimeDestination, ArrayList<Arc> curArcs){
		/*
		 * based on the earliest time, we need to find the entry time that is as late as possible.
		 */
		for(int i=curArcs.size()-1; i>=0; i--){
			
			Arc arc = curArcs.get(i);
			//when the previous arc is between the same store, there is no need to enforce the time window
			
			if(i>0 && curArcs.get(i-1).isWithinSameStore()){
				//when it is within the same store, not necessary for TW
				earliestTimeDestination = earliestTimeDestination - arc.getTotalTime();
			}
			else{
				earliestTimeDestination = Math.min(arc.getFromNode().getEndTime(),  earliestTimeDestination - arc.getTotalTime());
			}
			
		}
		
		return earliestTimeDestination;
	}
	
	
	
	/**
	 * get the increase in the total waiting time due to inserting <node> into <location>, 
	 * when the new path start at <startTime> at the warehouse
	 * @param node	the node to be inserted
	 * @param splitedArc the arc to be splited
	 * @param startTime	the time the new path start from the warehouse
	 * @return
	 */
	private double getInsertWaitCostIncrease(OrderNode node, Arc splitedArc, double earliestTime){
		double startTime = earliestTime;
		double cost = 0; 
		double arriveTime; 
		for(Arc arc : arcs){
			if(arc==splitedArc){
				Arc arcOne = splitedArc.getFromNode().getPostArc(node);
				arriveTime = startTime + arcOne.getTotalTime();
				startTime = Math.max(arcOne.getToNode().getStartTime(), arriveTime);
				cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();
				
				Arc arcTwo = node.getPostArc( splitedArc.getToNode());
				arriveTime = startTime + arcTwo.getTotalTime();
				startTime = Math.max(arcTwo.getToNode().getStartTime(), arriveTime);
				cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();

			}
			else{
				arriveTime = startTime + arc.getTotalTime();
				startTime = Math.max(arc.getToNode().getStartTime(), arriveTime);
				cost = cost + Math.max(0, startTime - arriveTime)*Parameter.getDriverIdleCost();
			}
		}
		
		cost = cost - waitCost;
		
		return cost;
		
	}
	
	/**
	 * get increase of cost of insertion into a specific locatio <location>
	 * @param node
	 * @param splitedArc the arc to be splited
	 * @return
	 */
	private double getInsertMoveCostIncrease(OrderNode node, Arc splitedArc){
		Arc arcOne = splitedArc.getFromNode().getPostArc(node);
		Arc arcTwo = node.getPostArc(splitedArc.getToNode());
		
		return arcOne.getCost() + arcTwo.getCost() - splitedArc.getCost();
	}
	
//	/**
//	 * get the earliest end time at the destination if 
//	 * @param node the node to be insertable
//	 * @param splitedArc the arc to be splited
//	 * @return the earliest time to arrive at the destination; -1 if not insertable
//	 */
//	private double getEarliestEndTimeDestination(OrderNode node, Arc splitedArc){
//		Arc arcOne = splitedArc.getFromNode().getPostArc(node);
//		Arc arcTwo = splitedArc.getToNode().getPreArc(node);
//		
//		if(arcOne==null 
//				|| arcTwo==null
//				|| totalTravelTime - splitedArc.getTravelTime() + arcOne.getTravelTime() + arcTwo.getTravelTime() > Parameter.getDrivingTimeLimitPerRouteHour()
//				){
//			return -1;
//		}
//		
//		double totalSeparation = arcOne.getTotalSeparationVolume();
//		totalSeparation += arcTwo.getTotalSeparationVolume();
//
//		if(totalVolume + totalSeparation > Parameter.getTruckVolumeLimit() ){   
//			return -1;
//		}
//
//		
//		ArrayList<Arc> curArcs = new ArrayList<Arc>();
//		for(Arc arc : arcs){
//			if(arc==splitedArc){
//				curArcs.add(arcOne);
//				curArcs.add(arcTwo);
//			}
//			else{
//				curArcs.add(arc);
//			}
//		}
//		
//		//the following code verify that the earliest time result is right
//		return getEarliestEndTimeDestination(curArcs);
//	}
	
	
	/**
	 * get the earliest end time at the destination if 
	 * @param curArcs: arcs sequence
	 * @return the earliest time to arrive at the destination; -1 if not insertable
	 */
	public static double getEarliestEndTimeDestination(ArrayList<Arc> curArcs){
		double startTime = 0;
		
		for(Arc arc : curArcs){
			startTime = Math.max(arc.getToNode().getStartTime(), startTime + arc.getTotalTime());
			if( arc.isOutsideEndNodeTW(startTime)//startTime > arc.getToNode().getEndTime()
					){
				return -1;
			}
			
		}
		return startTime;
	}
	
	/**
	 * get total weight of the order
	 * @return
	 */
	private double getTotalWeight(){
		double totalWeight = 0;
		for(Arc arc : arcs){
			totalWeight += arc.getToNode().getWeight();
		}
		return totalWeight;
	}

//	/**
//	 * get total weight of the order
//	 * @return
//	 */
//	private double getAvailableWeight(){
//		double availWeight = Parameter.getTruckWeightLimit();
//		for(Arc arc : arcs){
//			availWeight -= arc.getToNode().getWeight();
//		}
//		return availWeight;
//	}
	
	/**
	 * check if the weight feasible
	 * @param node
	 * @return
	 */
	public boolean isWeightFeasible(OrderNode node){
		return node.getWeight() + totalWeight <= Parameter.getTruckWeightLimit(); 
	}
	

//	/**
//	 * get total weight of the order
//	 * @return
//	 */
//	private double getAvailableVolume(){
//		double availVolume = Parameter.getTruckVolumeLimit();
//		for(Arc arc : arcs){
//			availVolume -= arc.getToNode().getCube();
//		}
//		return availVolume;
//		
//	}
	
	/**
	 * get the cost corresponding to the weight or volume reduction
	 * @return
	 */
	private double getWeightVolumeCost(double remainingValue, double totalValue, double curDeduction){
		return (remainingValue - curDeduction) / totalValue;
	}
	
	/**
	 * get the consumption of cube after traversing the arc ( 
	 * mainly due to separation and the order in the to node), not including the order volume
	 * @param arc
	 * @return
	 */
	private double getVolumeConsumpation(Arc arc){
		
		double volumeConsumption = 0;
		volumeConsumption += arc.getToNode().getCube();
		if(arc.isCatlogSeparation()){
			volumeConsumption += Parameter.getVolumeReductionOrders();
		}
		
		if(arc.isStoreSeparation()){
			volumeConsumption += Parameter.getVolumeReductionStores();
		}
		
		return volumeConsumption;
	}
	
	/**
	 * get the consumption of cube after traversing the arc ( 
	 * mainly due to separation and the order in the to node)
	 * @param arc
	 * @return
	 */
	private double getWeightConsumpation(Arc arc){
		return arc.getToNode().getWeight();
	}


	/**
	 * @return the originTime
	 */
	public double getOriginTime() {
		return originTime;
	}


	/**
	 * @param originTime the originTime to set
	 */
	public void setOriginTime(double originTime) {
		this.originTime = originTime;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String output = "";
		for(OrderNode node : getOrderNodes()){
			output = output + node.toString() + " > ";
		}
		
		return  output;
	}
	
	/**
	 * get the order node that is within the route
	 * @return
	 */
	public ArrayList<OrderNode> getOrderNodes(){
		ArrayList<OrderNode> nodes = new ArrayList<OrderNode>();
		for(int i=0; i<arcs.size()-1; i++){
			Node node = arcs.get(i).getToNode();
			nodes.add((OrderNode) node);
		}
		
		return nodes;
	}

	/**
	 * @return the arcs
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	/**
	 * @return the stores
	 */
	public LinkedHashSet<Store> getStores() {
		return stores;
	}
	
	/**
	 * get the improved route of the current route, by shuffling the nodes
	 * @return
	 */
	public Route getImprovedRoute(){
		LinkedHashSet<ArrayList<OrderNode>> alternativeRoutes = getAlternativeRoutes();
		
		double curCost = getTotalCost();
		Route bestRoute = null;
		for(ArrayList<OrderNode> nodes : alternativeRoutes){
			ArrayList<Arc> newArcs = new ArrayList<Arc>();
			
			//creat the arc from and out of the warehouse
			
			newArcs.add(arcs.get(0).getFromNode().getPostArc(nodes.get(0)));
			for(int i=0; i<nodes.size()-1; i++){
				Arc arc = nodes.get(i).getPostArc(nodes.get(i+1));
				newArcs.add(arc);
			}
			Node sinkNode = arcs.get(arcs.size()-1).getToNode();
			newArcs.add(nodes.get(nodes.size()-1).getPostArc(sinkNode));
			
			//need to verify that the volume constraint is not violated
			//get the earlist time
			
			Route newRoute = new Route(newArcs, nodes);
			
			if(newRoute.getOriginTime() > 0 && newRoute.getTotalVolume() <= Parameter.getTruckVolumeLimit()){
				double newCost = newRoute.getTotalCost();
				if(newCost < curCost){
					bestRoute = newRoute;
					curCost = newCost;
				}
			}
		}
		
		return bestRoute;
		
	}
	
	/**
	 * get the alterative route to the current one that cotnaisn exactly the same nodes,
	 * nothing would be return if the route only contains one node
	 * @return
	 */
	private LinkedHashSet<ArrayList<OrderNode>> getAlternativeRoutes(){
		//extract out the nodes
		ArrayList<OrderNode> orderNodes = getOrderNodes();
		
		if(orderNodes.size()<=1){
			return new LinkedHashSet<ArrayList<OrderNode>>();
		}
		
		//construct a small hashmap, containg the next node
		HashMap<OrderNode, LinkedList<Arc>> nextArcs = new LinkedHashMap<OrderNode, LinkedList<Arc>>();
		for(OrderNode nodeOne : orderNodes){
			LinkedList<Arc> postArcs = new LinkedList<Arc>();
			for(OrderNode nodeTwo : orderNodes){
				if(nodeOne==nodeTwo){
					continue;
				}
				
				Arc arc = nodeOne.getPostArc(nodeTwo);
				if(arc!=null){
					postArcs.add(arc);
				}
				
			}
			nextArcs.put(nodeOne, postArcs);
		}
		//based on this small hashmap construct the possible paths
		LinkedHashSet<ArrayList<OrderNode>> elegibleRoutes = new LinkedHashSet<ArrayList<OrderNode>>();
		//how do you make sure that the route would not be the first one?
		for(OrderNode n : orderNodes){
			LinkedList<ArrayList<OrderNode>> subRoutes = new LinkedList<ArrayList<OrderNode>>();  //subroutes found
			ArrayList<OrderNode> curRoute = new ArrayList<OrderNode>();
			curRoute.add(n);
			subRoutes.add(curRoute);
			
			while(subRoutes.size()>0){
				curRoute = subRoutes.removeFirst();
				OrderNode lastNode = curRoute.get(curRoute.size()-1);
				for(Arc arc : nextArcs.get(lastNode)){
					OrderNode nextNode = (OrderNode) arc.getToNode();
					
					if(curRoute.contains(nextNode)){
						continue;
					}
					
					ArrayList<OrderNode> newRoute = new ArrayList<OrderNode>(curRoute);
					newRoute.add(nextNode);
					//add to the elegible route if all nodes are added
					if(newRoute.size()==orderNodes.size()){
						elegibleRoutes.add(newRoute);
					}
					else{
						//otherwhise, just add to the sub path
						subRoutes.add(newRoute);
					}
					
				}
			}
		}
		
		elegibleRoutes.remove(orderNodes);
		return elegibleRoutes;
	}
}
