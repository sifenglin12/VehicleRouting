package edu.utexas.orie.cvrptw.cplex_solver;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.utexas.orie.cvrptw.grasp.Route;
import edu.utexas.orie.cvrptw.grasp.SolutionConstructor;
import edu.utexas.orie.cvrptw.grasp.WarehouseLoadingCapacity;
import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.Node;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.network.OrderSinkArc;
import edu.utexas.orie.cvrptw.network.SinkNode;
import edu.utexas.orie.cvrptw.network.SourceNode;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.VRPSolution;

public class CplexSolver {
	
	InstanceCVRPTW instance;
	Network network;
	HashMap<Arc, IloNumVar> arcVars[];
	IloNumVar[][] truckTimeSlotVars;
	IloNumVar[] truckStartTimeVars;
	IloNumVar[] truckEndTimeVars;
	HashMap<OrderNode, IloNumVar> nodeIdleTimeVars;
	HashMap<OrderNode, IloNumVar> nodeTimeVars;
	String outputFolder;
	
	boolean constraintName = true;
	
	double solutionTime;
	double modelBuildingTime = -1;
	double totalCost = -1;
	
	/**
	 * CPLEX total time limt
	 */
	double timeLimit = 1800;
	
	/**A solution that is injected, used for debug*/
	VRPSolution injectedSolution;
	
	public CplexSolver(InstanceCVRPTW instance, Network network, String outputFolder) {
		super();
		this.instance = instance;
		this.network = network;
		this.outputFolder = outputFolder;
	}

	public VRPSolution solve() throws IloException, IOException{
		double startModelBuilding = 1D*System.currentTimeMillis()/1000;
		IloCplex cplex = new IloCplex();
		initializeVariables(cplex);
		addRouteStartingConstraint(cplex);
		addObjectiveFunction(cplex);
		addFlowBalanceConstraint(cplex);
		addTimeWindowConstraints(cplex);
		addIdleTimeConstraints(cplex);
		addTrailerCapacityConstraint(cplex);
		addDOTLimits(cplex);
		
		addSymmetryBreakingConstraint(cplex);
		
		addSolution(injectedSolution, cplex);
		setCPLEXParameter(cplex);

//		cplex.exportModel("output/model.lp");
		double startSolvingTime = 1D*System.currentTimeMillis()/1000;
		modelBuildingTime = startSolvingTime - startModelBuilding;
		VRPSolution solu = null;
		if(cplex.solve()){
			System.out.println("\t\t\tCPLEX solve the model\t"+ cplex.getObjValue());
			totalCost = cplex.getObjValue();
			solu = extractSolution(cplex);
		}
		System.out.println("\t\t\t"+cplex.getStatus() );
		solutionTime = 1D*System.currentTimeMillis()/1000 - startSolvingTime;
		
		return solu;
	}
	
	/**
	 * set various parameter for CPLEX
	 * @param cplex
	 * @throws IloException 
	 * @throws FileNotFoundException 
	 */
	private void setCPLEXParameter(IloCplex cplex) throws IloException, FileNotFoundException{
		cplex.setParam(IloCplex.DoubleParam.TiLim, timeLimit);
		cplex.setParam (IloCplex.IntParam.MIPDisplay, 4);
		cplex.setParam (IloCplex.IntParam.NodeFileInd, 3);
//		cplex.setParam(IloCplex.DoubleParam.TreLim, 3000);
		
		FileOutputStream stream = new FileOutputStream(outputFolder+"/cplexLog.txt");
		cplex.setOut(stream);
		
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
	 * extract the VRP solution out of the cplex
	 * @return
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 * @throws IOException 
	 */
	private VRPSolution extractSolution(IloCplex cplex) throws UnknownObjectException, IloException, IOException{
		//extract all the values
		IloNumVar[] vars = getAllVars();
		HashMap<IloNumVar, Integer> indexMap = new HashMap<IloNumVar, Integer>();
		for(int i=0; i<vars.length; i++){
			indexMap.put(vars[i], i);
		}
		double[] values = cplex.getValues(vars);

		//get the slot map, startTime, endTime
		int[] slotMap = getSlotMap(values, indexMap);
		
		double[] startTimes = getStartTimes(values, indexMap);
		double[] endTimes = getEndTimes(values, indexMap);
		
		HashMap<OrderNode, Double> idleTime = getIdleTime(values, indexMap);
		HashMap<OrderNode, Double> serviceTime = getServiceTime(values, indexMap);
		
		Route[] routes = getRoutes(values, indexMap, serviceTime);
		LinkedList<Route> routeLinkedList = new LinkedList<Route>();
		//need to verfiy if the start time in routes is consistent with starttimes, and also endTimes
		WarehouseLoadingCapacity capacity = new WarehouseLoadingCapacity( instance.getLatestDepartTime() );
		for(Route route : routes){
			if(route!=null){
				capacity.addVehicle(route.getOriginTime(), route);
				routeLinkedList.add(route);
			}
		}
		try {
			compareStartEndTimeRoute(startTimes, endTimes, serviceTime, idleTime, routes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SolutionConstructor.plotSolution(outputFolder + "/CPLEXSolution" ,routeLinkedList, instance.getWarehouse());
		return new VRPSolution(instance, routeLinkedList, capacity);
	}
	
	private void compareStartEndTimeRoute(double[] startTimes, double[] endTimes, HashMap<OrderNode, Double> serviceTime,
			HashMap<OrderNode, Double> idleTime, Route[] routes) throws IOException{
		
//		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"/CPLEX_solu.txt"));
		LinkedList<Route> finalRoute = new LinkedList<Route>();
		for(int i=0; i<routes.length; i++){
			if(routes[i]==null){
				continue;
			}
			finalRoute.add(routes[i]);
//			writer.write( routes[i].getOriginTime() +"-->" + startTimes[i] +"\n");
//			routes[i].writeRouteDetails(writer);
			
//			for(OrderNode node : routes[i].getOrderNodes()){
//				writer.write(node + "\t" + serviceTime.get(node) +"\n");
//			}
		}
		
		SolutionConstructor.writeSolutionRoute(outputFolder+"/CPLEX_Solution.txt", finalRoute);
//		writer.close();
	}
	
	/**
	 * get the service time from the solution
	 * @param values
	 * @param indexMap
	 * @return
	 */
	private HashMap<OrderNode, Double> getServiceTime (double[] values, HashMap<IloNumVar, Integer> indexMap){
		HashMap<OrderNode, Double> serviceTime = new HashMap<OrderNode, Double>();
		for(OrderNode node : nodeTimeVars.keySet()){
			double value = values[indexMap.get(nodeTimeVars.get(node))];
			serviceTime.put(node, value);
		}
		
		return serviceTime;
	}
	
	/**
	 * get idle times from the solution
	 * @param values
	 * @param indexMap
	 * @return
	 */
	private HashMap<OrderNode, Double> getIdleTime(double[] values, HashMap<IloNumVar, Integer> indexMap){
		HashMap<OrderNode, Double> idleTime = new HashMap<OrderNode, Double>();
		for(OrderNode node : nodeIdleTimeVars.keySet()){
			double value = values[indexMap.get(nodeIdleTimeVars.get(node))];
			idleTime.put(node, value);
		}
		return idleTime;
	}
	
	/**
	 * get the end times from the solutions
	 * @param values
	 * @param indexMap
	 * @return
	 */
	private double[] getEndTimes(double[] values, HashMap<IloNumVar, Integer> indexMap){
		double[] endTimes = new double[truckEndTimeVars.length];
		
		
		for(int i=0; i<truckStartTimeVars.length; i++){
			endTimes[i] = values[indexMap.get(truckEndTimeVars[i])];
		}
		
		return endTimes;
	}
	
	/**
	 * get the start times from the solutions
	 * @param values
	 * @param indexMap
	 * @return
	 */
	private double[] getStartTimes(double[] values, HashMap<IloNumVar, Integer> indexMap){
		double[] startTimes = new double[truckStartTimeVars.length];
		for(int i=0; i<truckStartTimeVars.length; i++){
			startTimes[i] = values[indexMap.get(truckStartTimeVars[i])];
		}
		
		return startTimes;
	}
	
	/**
	 * get the slot map, i.e., the time that each truck is loaded at the warehouse
	 * @return
	 */
	private int[] getSlotMap(double[] values, HashMap<IloNumVar, Integer> indexMap){
		int[] slotMap = new int[truckTimeSlotVars.length];  //indicates the slot the truck uses
		for(int i=0; i<truckTimeSlotVars.length; i++){
			for(int j=0; j<truckTimeSlotVars[i].length; j++){
				if(values[indexMap.get(truckTimeSlotVars[i][j])] > 1e-6){
					slotMap[i] = j	;
				}
			}
		}
		
		return slotMap;
	}
	
	/**
	 * extract the route out of the solutions
	 * @param values
	 * @param indexMap
	 * @param serviceTime
	 * @return
	 */
	private Route[] getRoutes(double values[], HashMap<IloNumVar, Integer> indexMap, HashMap<OrderNode, Double> serviceTime){
		Route[] routes = new Route[arcVars.length];
		for(int i=0; i<arcVars.length; i++){
			ArrayList<Arc> usedArcs = new ArrayList<Arc>();
			for(Arc arc : arcVars[i].keySet()){
				IloNumVar var = arcVars[i].get(arc);
				if(values[indexMap.get(var)] > 1e-6){
					if(arc instanceof OrderSinkArc){
						usedArcs.add(arc);
					}
					else{
						//sort the index
						int curIndex = 0;
						while(curIndex<usedArcs.size() 
								&& !(usedArcs.get(curIndex) instanceof OrderSinkArc)
								&& serviceTime.get(arc.getToNode())> serviceTime.get(usedArcs.get(curIndex).getToNode()) ){
							curIndex++;
						}
						usedArcs.add(curIndex, arc);
					}
				}
			}
			if(usedArcs.size()>0){
				routes[i] = new Route(usedArcs);
			}
		}
		
		return routes;
	}
	
	/**
	 * get all the variables, in the list
	 * @return
	 */
	private IloNumVar[] getAllVars(){
		List<IloNumVar> vars = new LinkedList<IloNumVar>();
		for(int i=0; i<arcVars.length; i++){
			vars.addAll(arcVars[i].values());
		}
		for(int i=0; i< truckTimeSlotVars.length; i++){
			for(IloNumVar var: truckTimeSlotVars[i]){
				vars.add(var);
			}
		}
		for(IloNumVar var: truckStartTimeVars){
			vars.add(var);
		}
		for(IloNumVar var: truckEndTimeVars){
			vars.add(var);
		}

		vars.addAll(nodeIdleTimeVars.values());
		vars.addAll(nodeTimeVars.values());
		
		IloNumVar allVars[] = new IloNumVar[vars.size()];
		int i=0;
		for(IloNumVar var : vars){
			allVars[i] = var;
			i++;
		}
		return allVars;
	}
	
	/**
	 * add an initial solution by fixing its "x" values
	 * @throws IloException 
	 */
	private void addSolution(VRPSolution solution, IloCplex cplex) throws IloException{
		
		if(solution==null){
			return ;
		}
		List<IloNumVar> vars = new LinkedList<IloNumVar>();
		int curIndex = 0;
		for(Route route : solution.getRoutes()){
			vars.addAll(getActiveRouteVariables(route, curIndex));
			curIndex++;
		}
		
		fixValue(cplex, vars);
		
	}
	
	/**
	 * fix the corresponding vars to 1
	 * @param cplex
	 * @param vars
	 * @throws IloException 
	 */
	private void fixValue(IloCplex cplex, Collection<IloNumVar> vars) throws IloException{
		for(IloNumVar var : vars){
			cplex.addEq(var, 1);
		}
	}
	
	/**
	 * get the active route variables ('x') variables corresponding to the route
	 * @return
	 */
	private LinkedList<IloNumVar> getActiveRouteVariables(Route route, int vehicleIndex){
		LinkedList<IloNumVar> vars = new LinkedList<IloNumVar>();
		for(Arc arc : route.getArcs()){
			vars.add(arcVars[vehicleIndex].get(arc)	);
		}
		
		return vars;
	}
	
	
	/**
	 * initialize the variables
	 * @throws IloException 
	 */
	private void initializeVariables(IloCplex cplex) throws IloException{
		IloNumVar[] arcVarArray = cplex.boolVarArray(network.getArcs().size()*instance.getNumTrucks());
		arcVars = new HashMap[instance.getNumTrucks()];
		int totalNumVars = 0;
		for(int i=0; i<instance.getNumTrucks(); i++){
			arcVars[i] = new HashMap<>();
			for(Arc arc : network.getArcs()){
				arcVarArray[totalNumVars].setName("T"+i+arc.toString());
				arcVars[i].put(arc, arcVarArray[totalNumVars]);
				totalNumVars++;
			}
		}
		
		truckTimeSlotVars = new IloNumVar[instance.getNumTrucks()][];
		
		String truckStartTimeName[] = new String[instance.getNumTrucks()];
		String truckEndTimeName[] = new String[instance.getNumTrucks()];
		
		for(int i=0; i<instance.getNumTrucks(); i++){
			truckStartTimeName[i] = "T" + i +"Start";
			truckEndTimeName[i] = "T" + i +"End";
		}
		
		truckStartTimeVars = cplex.numVarArray(instance.getNumTrucks(), 0, instance.getLatestDepartTime(), truckStartTimeName);
		truckEndTimeVars = cplex.numVarArray(instance.getNumTrucks(), 0, instance.getLatestReturnTime(), truckEndTimeName);
		for(int i=0; i<instance.getNumTrucks(); i++){
			String[] truckTimeSlotNames = new String[instance.getWarehouse().getNumWarehouseSlots()];
			for(int a=0; a<truckTimeSlotNames.length; a++){
				truckTimeSlotNames[a] = "T"+i+"Slot" + a;
			}
			
			truckTimeSlotVars[i] = cplex.boolVarArray(instance.getWarehouse().getNumWarehouseSlots(), truckTimeSlotNames);
		}
		
		//ai<=ti<=bi
		nodeTimeVars = new HashMap<>();
		nodeIdleTimeVars = new HashMap<>();
		for(OrderNode node : network.getOrderNodes()){
			//here the latest end time is changed TODO fix the issue here
			IloNumVar var = cplex.numVar(node.getOrder().getStore().getEarliestTimeHour(), node.getLatestEndTime()*100, node.toString());
			nodeTimeVars.put(node, var);
			
			IloNumVar idleVar = cplex.numVar(0, node.getOrder().getStore().getEarliestTimeHour(), node.toString()+"Idle");
			
			nodeIdleTimeVars.put(node, idleVar);
		}
	}
	
	/**
	 * spreading of routes starts at warehouse 1h, hi, 1j, 1k
	 * @param cplex
	 * @throws IloException 
	 */
	private void addRouteStartingConstraint(IloCplex cplex) throws IloException{
		
		//constraint 1h
		for(int p=0; p<instance.getWarehouse().getNumWarehouseSlots(); p++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(int k = 0; k<instance.getNumTrucks(); k++){
				for(int q=0; q<=p; q++){
					expr.addTerm(1, truckTimeSlotVars[k][q]);
				}
			}
			
			String name = constraintName ? "WareLoading" + p : null; 
			
			cplex.addLe(expr, (p+1)*Parameter.getNumTrucksPerSlot(), name);
		}
		
		//constraint 1i
		for(int k = 0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr startExpr = cplex.linearNumExpr();
			IloLinearNumExpr endExpr = cplex.linearNumExpr();
			
			for(int p=0; p<instance.getWarehouse().getNumWarehouseSlots(); p++){
				double startTime = p*0.5;
				double endTime = (p+1)*0.5;
				startExpr.addTerm(startTime, truckTimeSlotVars[k][p]);
				endExpr.addTerm(endTime, truckTimeSlotVars[k][p]);
			}
			
			String nameLower = constraintName ? "StartLower" + k : null;
			String nameUpper = constraintName ? "StartUpper" + k : null;
			
			cplex.addGe(truckStartTimeVars[k], startExpr, nameLower);
			cplex.addLe(truckStartTimeVars[k], endExpr, nameUpper);
//			cplex.addGe(cplex.prod(1, truckStartTimeVars[k]), cplex.prod(startTime, truckTimeSlotVars[k][p]));
//			cplex.addLe(cplex.prod(1, truckStartTimeVars[k]), cplex.prod(endTime, truckTimeSlotVars[k][p]));
		}
		
		//constraint 1j
		for(int k=0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(int p=0; p<instance.getWarehouse().getNumWarehouseSlots(); p++){
				expr.addTerm(1, truckTimeSlotVars[k][p]);
			}
			
			String name = constraintName ? "vehicleOne" + k : null;
			cplex.addLe(expr, 1, name);
		}
		
		//constraint 1k
		for(int k=0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(int p=0; p<instance.getWarehouse().getNumWarehouseSlots(); p++){
				expr.addTerm(1, truckTimeSlotVars[k][p]);
			}
			
			for(Arc arc : network.getSource().getPostArcs()){
				expr.addTerm(-1, arcVars[k].get(arc));
			}
			
			String name = constraintName ? "routeAssign" + k : null;
			cplex.addGe(expr, 0, name);
		}
	}
	
	/**
	 * break the symmetry by enforcing orders on starting time
	 * @throws IloException 
	 */
	private void addSymmetryBreakingConstraint(IloCplex cplex) throws IloException{
		for(int p=0; p<instance.getWarehouse().getNumWarehouseSlots(); p++){
			for(int k=0; k<instance.getNumTrucks()-1; k++){
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for(int pp=0; pp<=p; pp++){
					expr.addTerm(1, truckTimeSlotVars[k][pp]);
					expr.addTerm(-1, truckTimeSlotVars[k+1][pp]);
				}
				
				cplex.addGe(expr, 0);
			}
		}
	}
	
	/**
	 * add the trailer capacity constraint 1l, 1m, 1n, 1o
	 * @param cplex
	 * @throws IloException 
	 */
	private void addTrailerCapacityConstraint(IloCplex cplex) throws IloException{
		//constraint 1l
		for(int k=0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(Arc arc : network.getArcs()){
				Node fromNode = arc.getFromNode();
				if(fromNode.isDeliveryNode()){
					OrderNode fromOrderNode = (OrderNode) fromNode;
					expr.addTerm(fromOrderNode.getOrder().getWeight(), arcVars[k].get(arc));
				}
			}
			
			String name = constraintName ? "truckWeight" + k : null;
			cplex.addLe(expr, Parameter.getTruckWeightLimit(), name);
		}

		//constraint 1m and 1o: unable to enforce due to insufficient data
		
		//constraint 1n
		for(int k=0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(Arc arc : network.getArcs()){
				Node fromNode = arc.getFromNode();
				if(fromNode.isDeliveryNode()){
					OrderNode fromOrderNode = (OrderNode) fromNode;
					expr.addTerm(fromOrderNode.getOrder().getCube(), arcVars[k].get(arc));
				}
				
				if(arc.isCatlogSeparation()){
					expr.addTerm(Parameter.getVolumeReductionOrders(), arcVars[k].get(arc));
				}
				
				if(arc.isStoreSeparation()){
					expr.addTerm(Parameter.getVolumeReductionStores(), arcVars[k].get(arc));;
				}
			}
			
			String name = constraintName ? "truckvolume" + k : null;
			cplex.addLe(expr, Parameter.getTruckVolumeLimit(), name);
			
		}
		
		
	}
	
	
	/**
	 * add the dirver time limit, 1p and 1q
	 * @param cplex
	 * @throws IloException 
	 */
	private void addDOTLimits(IloCplex cplex) throws IloException{
		
		//constraint 1p
		for(int k=0; k<instance.getNumTrucks(); k++){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(Arc arc : network.getArcs()){
				expr.addTerm(arc.getTravelTime(), arcVars[k].get(arc));
			}
			
			String name = constraintName ? "drivingTime" + k : null;
			cplex.addLe(expr, Parameter.getDrivingTimeLimitPerRouteHour(), name);
		}
		
		//constraint 1q
		for(int k=0; k<instance.getNumTrucks(); k++){
			String name = constraintName ? "totalTime" + k : null;
			cplex.addLe(cplex.diff(truckEndTimeVars[k], truckStartTimeVars[k]), Parameter.getTotalTimeLimitPerRouteHour(), name);
		}
	}
	
	/**
	 * add the time window constraint 1d, 1e, 1f, and 1g
	 * @param cplex
	 * @throws IloException 
	 */
	private void addTimeWindowConstraints(IloCplex cplex) throws IloException{
		for(int i=0; i<instance.getNumTrucks(); i++){
			for(Arc arc : network.getArcs()){
				
				Node toNode = arc.getToNode();
				IloLinearNumExpr expr = cplex.linearNumExpr();
				expr.addTerm(1, getTimeVar(toNode, i));
				expr.addTerm(-1, getTimeVar(arc.getFromNode(), i));
				
				double bigM = arc.getBigM();
				expr.addTerm(-1*bigM, arcVars[i].get(arc));
				String name = constraintName ? "routeContinuity" + i + "_"+arc: null;
				cplex.addGe(expr, arc.getTotalTime() - bigM, name);
			}
		}
		
		//enforce the right side of constraints 1g
		for(OrderNode node : network.getOrderNodes()){
			//here enforce the constraint
			IloLinearNumExpr expr = cplex.linearNumExpr();
			expr.addTerm(1, getTimeVar(node, -1));
			for(int i=0; i<instance.getNumTrucks(); i++){
				for(Arc arc : node.getPreArcs()){
					//for all the previous arc that is within the same store
					if(arc.isWithinSameStore()){
						double coef = -1*node.getCumulativeTimeInStore(arc);
						expr.addTerm(coef, arcVars[i].get(arc));
					}
				}
			//set up the M value
			}
			String name = constraintName ? "time window" + node: null;
			cplex.addLe(expr, node.getOrder().getStore().getLatestTimeHour(), name);
		}
	}
	
	/**
	 * add the idle time constraint 1r, 1s
	 * @param cplex
	 * @throws IloException 
	 */
	private void addIdleTimeConstraints(IloCplex cplex) throws IloException{
		for(int i=0; i<instance.getNumTrucks(); i++){
			for(Arc arc : network.getArcs()){
				if(arc.getToNode() instanceof SinkNode){
					continue;
				}
				
				IloLinearNumExpr expr = cplex.linearNumExpr();
				expr.addTerm(1, getTimeVar(arc.getFromNode(), i));
				expr.addTerm(-1, getTimeVar(arc.getToNode(), i));
				
				//TODO Be cautious here, when it is within the same store, does it still apply?
				//I think it would still
				double bigL = arc.getBigLIdleTime();
				expr.addTerm(-1*bigL, arcVars[i].get(arc));
				expr.addTerm(1, nodeIdleTimeVars.get(arc.getToNode()));
				String name = constraintName ? "idleTime" + i +"_" +arc : null;
				cplex.addGe(expr, 0 - arc.getTotalTime() - bigL, name);
			}
		}
	}
	
	/**
	 * get the time variable corresponding to the node
	 * @return
	 */
	private IloNumVar getTimeVar(Node node, int truck){
		if(node instanceof OrderNode){
			return nodeTimeVars.get(node);
		}
		else if(node instanceof SourceNode){
			return truckStartTimeVars[truck];
		}
		else if(node instanceof SinkNode){
			return truckEndTimeVars[truck];
		}
		
		return null;
	}
	
	/**
	 * add the flow balance constraint 1b and 1c
	 * @param cplex
	 * @throws IloException 
	 */
	private void addFlowBalanceConstraint(IloCplex cplex) throws IloException{
		
		//constraint 1b: each node is visited exactly once
		int n = 0;
		for(OrderNode node : network.getOrderNodes()){
			IloLinearNumExpr expr = cplex.linearNumExpr();
			
			for(int i = 0; i<instance.getNumTrucks(); i++){
				for(Arc arc : node.getPostArcs()){
					expr.addTerm(1, arcVars[i].get(arc));
				}
			}
			String name = constraintName ? "flow"+node : null;
			cplex.addEq(expr, 1, name);
			n++;
		}
		
		//constraint 1c
		n = 0;
		for(OrderNode node : network.getOrderNodes()){
			for(int i = 0; i<instance.getNumTrucks(); i++){
			
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for(Arc arc : node.getPostArcs()){
					expr.addTerm(1, arcVars[i].get(arc));
				}

				for(Arc arc : node.getPreArcs()){
					expr.addTerm(-1, arcVars[i].get(arc));
				}
				
				String name = constraintName ? "flowT" + i +"_"+node : null;
				cplex.addEq(expr, 0, name);
				n++;
			}
		}
		
	}
	
	/**
	 * add the objective function to cplex
	 * @param cplex
	 * @throws IloException 
	 */
	private void addObjectiveFunction(IloCplex cplex) throws IloException{
		IloLinearNumExpr expr = cplex.linearNumExpr();
		for(int i=0; i<instance.getNumTrucks(); i++){
			for(Arc arc : network.getArcs()){
				expr.addTerm(arc.getCost(), arcVars[i].get(arc));
			}
		}
		
		for(OrderNode node : network.getOrderNodes()){
			expr.addTerm(Parameter.getDriverIdleCost(), nodeIdleTimeVars.get(node));
		}
		
		cplex.addMinimize(expr);
	}

	/**
	 * @param injectedSolution the injectedSolution to set
	 */
	public void setInjectedSolution(VRPSolution injectedSolution) {
		this.injectedSolution = injectedSolution;
	}
	
	
}
