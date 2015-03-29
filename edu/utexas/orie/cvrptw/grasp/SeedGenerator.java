package edu.utexas.orie.cvrptw.grasp;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.instance.Order;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.instance.Store;
import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.Node;
import edu.utexas.orie.cvrptw.network.OrderNode;

/**
 * this method generate the seeds
 * @author SifengLin
 */
public class SeedGenerator {
	
//	int numSeeds; 
	InstanceCVRPTW instance;
	Network network;
	
	public SeedGenerator(InstanceCVRPTW instance, Network network) {
		super();
		this.instance = instance;
		this.network = network;
	}
	
	/**
	 * take the max { sum ( min time) }
	 */
	public LinkedHashSet<Store> getSeedsMaxSumEachMinTimeHeuristic(){
		
		double startTime = System.currentTimeMillis()*1D/1000;
		
		double curMaxSum = 0;
		LinkedHashSet<Integer> curMaxSumSeeds = null;
		int numSeeds = getNumSeeds();
		for(int i=0; i<instance.getStores().size(); i++){
			LinkedHashSet<Integer> curSeeds = getSeedsMaxSumMinTimeHeuristic(i, numSeeds);
			double curSum = getSumMinTime(curSeeds);
			
			if(curMaxSumSeeds==null || curMaxSum< curSum){
				curMaxSum = curSum;
				curMaxSumSeeds = curSeeds;
			}
		}
		
//		instance.getStatistics().setTimeGetSeed(System.currentTimeMillis()*1D/1000 - startTime);
//		System.out.println("Final \t: " + curMaxSum);
//		instance.getStatistics().setNumSeeds(curMaxSumSeeds.size());
		
		LinkedHashSet<Store> stores = new LinkedHashSet<Store>();
		for(int i : curMaxSumSeeds){
			stores.add(instance.getStores().get(i));
		}
		
		return stores;
	}
	
	/**
	 * get the sum of min time
	 * @param seeds
	 * @return
	 */
	public double getSumMinTime(LinkedHashSet<Integer> seeds){
		
		double curSum = 0;
		for(int i : seeds){
			//min value
			double curMin = Double.MAX_VALUE;
			for(int j : seeds){
				if(i!=j){
					curMin = Math.min(curMin, 
								instance.getTravelTimeHour(instance.getStores().get(i), instance.getStores().get(j)));
				}
			}
			
			curSum += curMin;
		}
		
		return curSum;
	}
	
	/**
	 * get the seed with maxium summation of seeds
	 * @param initialSeed
	 * @return
	 */
	protected LinkedHashSet<Integer> getSeedsMaxSumMinTimeHeuristic(int initialSeed, int numSeeds){
		ArrayList<Store> stores = instance.getStores();
		LinkedHashSet<Integer> selectedStoreIndices = new LinkedHashSet<Integer>();
		
		int numStores = instance.getStores().size();
		
		double[] minTime = new double[numStores];  //the min distance to the selected stores
		Arrays.fill(minTime, 1e7);
		int curSeed = initialSeed; //(new Random()).nextInt(numStores);
		
		while (selectedStoreIndices.size()<numSeeds){
			int maxIndices = 0;
			double maxValue = 0;
			for(int i=0; i<minTime.length; i++){
				
				minTime[i] = Math.min(minTime[i], 
									instance.getTravelTimeHour(stores.get(i), stores.get(curSeed)));
				
				if(minTime[i] >maxValue){
					maxIndices = i;
					maxValue = minTime[i];
				}
			}
			selectedStoreIndices.add(curSeed);
			curSeed = maxIndices;
//			System.out.println( maxIndices  + "\t: " + maxValue);
		}
		
		return selectedStoreIndices;
		
	}
	
	protected int getNumSeeds(){
		int weightSeeds = getNumSeedsBinPackLowerBoundWeight();
		int volumeSeeds = getNumSeedsBinPackLowerBoundVolume();
		
		int bestSeeds = Math.max(weightSeeds, volumeSeeds);
		
		int travelTimeSeeds = getNumSeedsBinPackDrivingTime(bestSeeds);
		bestSeeds = Math.max(bestSeeds, travelTimeSeeds);
		
		int totalTimeSeeds = getNumSeedsBinPackTotalTime(bestSeeds);
		bestSeeds = Math.max(bestSeeds, totalTimeSeeds);
		
//		System.out.println(instance.getDataName() + "\tNumber of seeds Necessary: " + bestSeeds);
		return bestSeeds;
		
	}
	
	/**
	 * get the number of seeds according to the total time requirement.  Specifically, we need to 
	 * 1. get the minimal time from each store to other stores
	 * 2. get the minal number of vehicles, and check how many bins are necessary
	 * @param curLowerBound: the current lower bound value given by other methods
	 * @return
	 */
	protected int getNumSeedsBinPackTotalTime(int curLowerBound){
		LinkedHashSet<Double> timeValues = new LinkedHashSet<Double>();
		ArrayList<Double> firstTripValues = new ArrayList<Double>();
		
		for(OrderNode node : network.getOrderNodes()){
			//get the min time for each store
			double endCurStore = node.getOrder().getStore().getLatestTimeHour();
			
			firstTripValues.add(instance.getTravelTimeHour(instance.getWarehouse(), node.getOrder().getStore()));
			
			double curMin = Double.MAX_VALUE;
			for(Arc arc : node.getPostArcs()){
				double time = arc.getTotalTime();
				Node toNode = arc.getToNode();
				if(toNode instanceof OrderNode){
					double startNextStore = ((OrderNode) toNode).getOrder().getStore().getEarliestTimeHour();
					time = Math.max(time, startNextStore - endCurStore);
				}
				
				curMin = Math.min(curMin, time);
			}
			timeValues.add(curMin);
		}
		
		Collections.sort(firstTripValues);
		for(int i=0; i<curLowerBound; i++){
			timeValues.add(firstTripValues.get(i));
		}
		
		return BinPacking.getLowerBound(timeValues, Parameter.getTotalTimeLimitPerRouteHour());
	}

	/**
	 * get the number of seeds according to the driving time requirement.  Specifically, we need to 
	 * 1. get the minimal time from each store to other stores
	 * 2. get the minal number of vehicles, and check how many bins are necessary
	 * @param curLowerBound: the current lower bound value given by other methods
	 * @return
	 */
	protected int getNumSeedsBinPackDrivingTime(int curLowerBound){
		LinkedHashSet<Double> timeValues = new LinkedHashSet<Double>();
		ArrayList<Double> firstTripValues = new ArrayList<Double>();
		
		for(Store store : instance.getStores()){
			//get the min time for each store
			double endCurStore = store.getLatestTimeHour();
			
			firstTripValues.add(instance.getTravelTimeHour(instance.getWarehouse(), store));
			
			double curMin = Double.MAX_VALUE;
			for(Store nextStore : instance.getStores()){
				if(store!=nextStore){
					double time = instance.getTravelTimeHour(store, nextStore);
					double startNextStore = store.getEarliestTimeHour();
					time = Math.max(time, startNextStore - endCurStore);
					curMin = Math.min(curMin, time);
				}
			}
			
			curMin = Math.min(curMin, instance.getTravelTimeHour(store, instance.getWarehouse()));
			timeValues.add(curMin);
		}
		
		Collections.sort(firstTripValues);
		for(int i=0; i<curLowerBound; i++){
			timeValues.add(firstTripValues.get(i));
		}
		return BinPacking.getLowerBound(timeValues, Parameter.getTotalTimeLimitPerRouteHour());
	}
	
	/**
	 * get the number of seeds using the binpacking lower bound
	 * @return
	 */
	protected int getNumSeedsBinPackLowerBoundWeight(){
		LinkedList<Double> orderWeights = new LinkedList<Double>();
		for(Store store : instance.getStores()){
			for(Order order : store.getDeliveryOrders()){
				orderWeights.add(order.getWeight());
			}
		}
		
		return BinPacking.getLowerBound(orderWeights, Parameter.getTruckWeightLimit());
	}

	/**
	 * get the number of seeds using the binpacking lower bound
	 * @return
	 */
	protected int getNumSeedsBinPackLowerBoundVolume(){
		LinkedList<Double> orderVolumes = new LinkedList<Double>();
		for(Store store : instance.getStores()){
			for(Order order : store.getDeliveryOrders()){
				orderVolumes.add(order.getCube());
			}
		}
		
		return BinPacking.getLowerBound(orderVolumes, Parameter.getTruckVolumeLimit());
	}
	
	/**
	 * get the seed, using the total time between seeds as the criteria
	 * @return
	 * @throws IloException 
	 */
	public Store[] getSeedsMaxTotalTime() throws IloException{
		int numSeeds = 20;
		IloCplex cplex = new IloCplex();
		ArrayList<Store> stores = instance.getStores();
		int numStores = instance.getStores().size();
		IloNumVar[] xVars = cplex.boolVarArray(numStores);
		IloNumVar[] yVars = cplex.boolVarArray(
				numStores*(numStores-1)/2
				);
		
		IloLinearNumExpr objExpr = cplex.linearNumExpr();
		int curyIndex = 0;
		for(int i=0; i<numStores; i++){
			for(int j=i+1; j<numStores; j++){
				IloNumVar yVar = yVars[curyIndex];
				cplex.addLe(yVar, xVars[i]);
				cplex.addLe(yVar, xVars[j]);
				double coef = instance.getTravelTimeHour(stores.get(i), stores.get(j));
				objExpr.addTerm(coef, yVar);
				
				
				
				curyIndex++;
			}
		}
		
		cplex.addLe(cplex.sum(xVars), numSeeds);
		cplex.addMaximize(objExpr);
		
		ArrayList<Store> selectedStores = new ArrayList<Store>(numSeeds);
		if(cplex.solve()){
			double values[] = cplex.getValues(xVars);
			for(int i=0; i<values.length; i++){
				if( values[i] > 1e-6){
					selectedStores.add(stores.get(i));
				}
			}
		}
		
		Store[] finalStores = new Store[selectedStores.size()];
		return finalStores;
	}
	
//	/**
//	 * get the y variables 
//	 */
//	protected IloNumVar getYVar(int storeIndexOne, int storeIndexTwo, IloNumVar[] yVars, int numStores){
//		if(storeIndexOne<=storeIndexTwo){
//			return yVars[storeIndexOne*numStores + storeIndexTwo];
//		}
//		else{
//			return yVars[storeIndexTwo*numStores + storeIndexOne];
//		}
//	}
}
