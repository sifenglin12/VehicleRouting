package edu.utexas.orie.cvrptw.grasp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.VRPSolution;

public class LocalImprovement {
	InstanceCVRPTW instance;
	Network network;
	String outputFolder;
	
	double solutionTime = -1;
	double totalCost = -1;
	
	public LocalImprovement(InstanceCVRPTW instance, Network network,
			String outputFolder) {
		super();
		this.instance = instance;
		this.network = network;
		this.outputFolder = outputFolder;
	}
	
	/**
	 * implement the local search between routes
	 * Specifically, use the depath first search to find all possible swaps and insert nodes
	 * @param routes the solution route
	 * @param lengthLimit the limit on the length of the local search route
	 * @throws IOException 
	 */
	public VRPSolution localSearchBetweenRoutes(VRPSolution solu, int lengthLimit) throws IOException{
		
		double startTime = 1D*System.currentTimeMillis()/1000;
		LinkedList<Route> routes = solu.getRoutes();
		WarehouseLoadingCapacity capacity = solu.getCapacity();
		BufferedWriter writerSingleInsert = new BufferedWriter(new FileWriter(outputFolder + "/executePath.txt"));
		
		//the first step is to update all possible negative cost move
		CostMap costMap = new CostMap(routes, outputFolder, capacity);
		//then search for possible insert
		costMap.findSingleInsert(capacity);
		costMap.initializeReplaceNodeMap(capacity);
		
		//get the first node and find all the other
		LinkedList<ReplaceNode> elegibleNodes = costMap.getElegibleReplaceNodes();
		while(elegibleNodes.size()>0){
			
			ReplaceNode firstReplace = elegibleNodes.removeFirst();
			
			LinkedList<SwapPath> curSwapPaths = new LinkedList<SwapPath>();
			LinkedList<SwapPath> swapCycles = new LinkedList<SwapPath>();//swap cycles found so far
			LinkedList<SwapPath> swapInsertPath = new LinkedList<SwapPath>();//swap path found so far
			LinkedList<InsertNodeIntoArc> swapInsertIna = new LinkedList<InsertNodeIntoArc>();//the INA corresponding to the swap path
			LinkedList<RemoveNode> swapRemoveNodes = new LinkedList<RemoveNode>();//the INA corresponding to the swap path
			
			LinkedHashSet<Route> modifiedRoutes = new LinkedHashSet<Route>();//the routes that are modified so far
			curSwapPaths.add( new SwapPath(firstReplace, costMap) );
			
			while(curSwapPaths.size()>0){
				SwapPath curPath = curSwapPaths.removeLast();
				if(isContainModifiedRoutes(modifiedRoutes, curPath)  //|| modifiedRoutes.contains(costMap.getNodeToRoute().get(curPath.getFirstNode()))
						){  //do not search the path if any of the route is already changed
					continue;
				}
				
				//need to search for the next possible extension to the current path
				OrderNode node = curPath.getLastNode().getReplacedNode();
				
				//if the path contains a cycle, stop current
				ReplaceNode cycleNode = curPath.getNegativeCircleReplaceNode(costMap, capacity);
				if(cycleNode==null){
					InsertNodeIntoArc negativeIna = curPath.getNegativeCostPath(costMap, modifiedRoutes, capacity);
					if(negativeIna!=null){ //a negative path is found
						System.out.print("\timproved path\t");
						swapInsertIna.add(negativeIna);
						swapInsertPath.add(curPath);
						swapRemoveNodes.add(costMap.getRemoveNode(curPath.getFirstNode().getNodeToReplace()));
						//need to make sure that all paths are included into the modified path set
						modifiedRoutes.addAll(curPath.getModifiedRoutes());
						modifiedRoutes.add(negativeIna.getRouteInserted());
						modifiedRoutes.add(costMap.getNodeToRoute().get(curPath.getFirstNode()));
						break;
					}
				}
				else{//a negative cycle is found
					System.out.print("\timproved cycle\t");
					curPath.addNewReplaceNode(cycleNode, costMap);
					swapCycles.add(curPath);
					modifiedRoutes.addAll(curPath.getModifiedRoutes());
					break;
				}
				for(ReplaceNode newNode : costMap.getReplaceNodeMap().get(node).values()){
					if(curPath.isContainRoute(newNode.getReplacedRoute()) || modifiedRoutes.contains(newNode.getReplacedRoute())
							){//|| newNode.getReplacedNode() == curPath.getFirstNode().getNodeToReplace()
						continue;
					}
					SwapPath newPath = curPath.generateNewSwapPath(newNode, costMap);
					if(newPath.getReplaceNodeList().size()<=lengthLimit){
						curSwapPaths.add(newPath);
					}
				}
			}
			
			if(swapCycles.size()==0 && swapInsertIna.size()==0){
				break;
			}
			else{
				System.out.println("\texecutePaths");
			}
//			double expectedCost = getExpectedCost(costMap, swapCycles, swapInsertPath, swapInsertIna);
			printSwapLine(swapCycles, writerSingleInsert);
			printInsertPathLine(swapInsertPath, writerSingleInsert, swapInsertIna, costMap);
			writeLocalSearchLine("CycleBefore", swapCycles, writerSingleInsert); //write out the routes before executing
			writeLocalSearchLine("PathBefore", costMap, swapInsertPath, swapInsertIna, swapRemoveNodes, writerSingleInsert);
			
			executePaths(swapCycles, swapInsertPath, swapInsertIna, costMap, capacity);
			
			writeLocalSearchLine("CycleAfter", swapCycles, writerSingleInsert); //write out the routes after executing
			writeLocalSearchLine("PathAfter", costMap, swapInsertPath, swapInsertIna, swapRemoveNodes ,writerSingleInsert);
			writerSingleInsert.newLine();
//			if(Math.abs(expectedCost - Route.getTotalCost(costMap.getSoluRoutes()))>1e-6){
//				System.out.println("ERROR in local search LocalImprovement.localSearchBetweenRoutes");
//			}
			
			costMap.findSingleInsert(capacity);
			
			elegibleNodes = costMap.getElegibleReplaceNodes(); //update the elegible routes
			
		}
		
		solutionTime = 1D*System.currentTimeMillis()/1000 - startTime;
		totalCost = Route.getTotalCost(costMap.getSoluRoutes());
		
		SolutionConstructor.writeSolutionRoute(outputFolder + "/ImprovedSolution.txt", costMap.getSoluRoutes());
		SolutionConstructor.verifyFeasibility(instance, costMap.getSoluRoutes(), null, outputFolder + "/VerifyFeaImprovedSolu.txt");
		
		writerSingleInsert.close();
		
		BufferedWriter writerWarehouse = new BufferedWriter(new FileWriter(outputFolder + "/warehosue.txt"));
		capacity.printWarehouseCapacity(costMap.getSoluRoutes(), writerWarehouse);		
		SolutionConstructor.plotSolution(outputFolder + "/ImprovedSolution" ,costMap.getSoluRoutes(), instance.getWarehouse());
		writerWarehouse.close();
		
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
	 * write the details of the swap
	 * @param swapCycles
	 * @throws IOException 
	 */
	private void printSwapLine(LinkedList<SwapPath> swapCycles, BufferedWriter writerSingleInsert) throws IOException{
		double cost = 0;
		for(SwapPath swapPath : swapCycles){
			for(ReplaceNode rn : swapPath.getReplaceNodeList()){
				writerSingleInsert.write(rn.getNodeToReplace() + "----->" + rn.getReplacedNode());
				writerSingleInsert.newLine();
				writerSingleInsert.flush();
				cost += rn.getCostIncrease();
			}
			writerSingleInsert.write(cost +"\n");
		}
	}

	/**
	 * write the details of the swap
	 * @param swapCycles
	 * @throws IOException 
	 */
	private void printInsertPathLine(LinkedList<SwapPath> swapCycles, BufferedWriter writerSingleInsert, 
			LinkedList<InsertNodeIntoArc> swapInsertIna, CostMap costMap) throws IOException{
		double cost = 0;
		Iterator<InsertNodeIntoArc> itr = swapInsertIna.iterator();
		
		for(SwapPath swapPath : swapCycles){
			
			InsertNodeIntoArc negativeIna = itr.next();
			RemoveNode removeNode = costMap.getRemoveNode(swapPath.getFirstNode().getNodeToReplace());
			
			writerSingleInsert.write( "       ----->" + removeNode.getNodeToRemove());
			writerSingleInsert.newLine();
			cost += removeNode.getCostChange();
			for(ReplaceNode rn : swapPath.getReplaceNodeList()){
				writerSingleInsert.write(rn.getNodeToReplace() + "----->" + rn.getReplacedNode());
				writerSingleInsert.newLine();
				writerSingleInsert.flush();
				cost += rn.getCostIncrease();
			}
			writerSingleInsert.write(negativeIna.getNodeToInsert() + "----->" + negativeIna.getArcInserted());
			writerSingleInsert.newLine();
			
			cost += negativeIna.getCostChangeInsertedRoute();
			writerSingleInsert.write(cost +"\n");
		}
	}
	
	/**
	 * get the expected cost, if everything is updated
	 * @return
	 */
	public double getExpectedCost(CostMap costMap, LinkedList<SwapPath> swapCycles, LinkedList<SwapPath> swapInsertPath, LinkedList<InsertNodeIntoArc> swapInsertIna){
		double previousCost = Route.getTotalCost(costMap.getSoluRoutes());
		for(SwapPath path : swapCycles){
			for(ReplaceNode rn : path.getReplaceNodeList()){
				previousCost = previousCost + rn.getCostIncrease();
			}
		}
		Iterator<InsertNodeIntoArc> itr = swapInsertIna.iterator();
		//execute the swap path
		for(SwapPath newPath : swapInsertPath){
			InsertNodeIntoArc negativeIna = itr.next();
			RemoveNode removeNode = costMap.getRemoveNode(newPath.getFirstNode().getNodeToReplace());
			previousCost = previousCost + removeNode.getCostChange();
			for(ReplaceNode rn : newPath.getReplaceNodeList()){
				previousCost = previousCost + rn.getCostIncrease();
			}
			
			previousCost = previousCost + negativeIna.getCostChangeInsertedRoute();
			
		}
		
		return previousCost;
	}

	/**
	 * get the expected cost, if everything is updated
	 * @return
	 * @throws IOException 
	 */
	public void writeLocalSearchLine(String info, CostMap costMap, 
			LinkedList<SwapPath> swapInsertPath, LinkedList<InsertNodeIntoArc> swapInsertIna, 
			LinkedList<RemoveNode> removeNodes, BufferedWriter writerSingleInsert) throws IOException{
		
		if(swapInsertPath.size()==0){
			return;
		}
		
		Iterator<InsertNodeIntoArc> itrINA = swapInsertIna.iterator();
		Iterator<RemoveNode> itrRN = removeNodes.iterator();
		
		//execute the swap path
		for(SwapPath newPath : swapInsertPath){
			writerSingleInsert.write(info);
			writerSingleInsert.newLine();
			InsertNodeIntoArc negativeIna = itrINA.next();
			RemoveNode removeNode = itrRN.next();
			
			writerSingleInsert.write(removeNode.getRoute().toString());
			writerSingleInsert.newLine();
			for(ReplaceNode rn : newPath.getReplaceNodeList()){
				writerSingleInsert.write(rn.getReplacedRoute().toString());
				writerSingleInsert.newLine();
			}
			
			writerSingleInsert.write(negativeIna.getRouteInserted().toString());
			writerSingleInsert.newLine();
			writerSingleInsert.flush();
			
		}
		
	}
	
	/**
	 * write the routes that is in the swap map
	 * @param otherInfo
	 * @param swapCycles
	 * @param writerSingleInsert
	 * @throws IOException
	 */
	private void writeLocalSearchLine(String otherInfo, LinkedList<SwapPath> swapCycles, BufferedWriter writerSingleInsert) throws IOException{
		for(SwapPath swapPath : swapCycles){
			writerSingleInsert.write(otherInfo);
			writerSingleInsert.newLine();
			if(swapPath!=null){
				for(ReplaceNode rn : swapPath.getReplaceNodeList()){
					writerSingleInsert.write(rn.getReplacedRoute().toString());
					writerSingleInsert.newLine();
				}
			}
			writerSingleInsert.flush();
		}
	}
	
	/**
	 * checks if @modifiedRoutes contains any path of the @curPath
	 * @param modifiedRoutes
	 * @param curPath
	 * @return
	 */
	
	public boolean isContainModifiedRoutes(LinkedHashSet<Route> modifiedRoutes, SwapPath curPath ){
		for(Route r : curPath.getModifiedRoutes()){
			if(modifiedRoutes.contains(r)){
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * execute the paths and cycles
	 * Caution when debugging.  Note that the costMap would be changed here.
	 * @param swapCycles
	 * @param swapInsertPath
	 * @param swapInsertIna
	 * @param costMap
	 * @throws IOException 
	 */
	public void executePaths(LinkedList<SwapPath> swapCycles, LinkedList<SwapPath> swapInsertPath, 
			LinkedList<InsertNodeIntoArc> swapInsertIna, CostMap costMap, 
			WarehouseLoadingCapacity capacity) throws IOException{
		//execute the swap cycle
		for(SwapPath cycle : swapCycles){
			costMap.executePath(null, null, cycle, capacity);
			costMap.updateCostMap(cycle.getReplaceNodeList(), capacity);
		}

		//execute the swap path
		Iterator<InsertNodeIntoArc> itr = swapInsertIna.iterator();
		for(SwapPath newPath : swapInsertPath){
			InsertNodeIntoArc negativeIna = itr.next();
			RemoveNode removeNode = costMap.getRemoveNode(newPath.getFirstNode().getNodeToReplace());
			costMap.getNodeToRoute().get(newPath.getFirstNode().getNodeToReplace());
			costMap.executePath(negativeIna, removeNode, newPath, capacity);
			
			//update the path, and update the
			costMap.updateCostMap(newPath.getReplaceNodeList(), capacity);
			costMap.updateCostMap(removeNode, capacity);
			costMap.updateCostMap(negativeIna, capacity);
		}
	}
	
	
}
