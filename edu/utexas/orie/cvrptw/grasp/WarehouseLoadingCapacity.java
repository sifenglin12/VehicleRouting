package edu.utexas.orie.cvrptw.grasp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.output.StringProcessor;

/**
 * this function models the warehouse loading capacity.  
 * Specifically, it keeps track of the cumulative number of warehouses loaded in at each time 
 * @author SifengLin
 *
 */
public class WarehouseLoadingCapacity {
	
	public final double intervalLength = 0.5;  //the length of per time interval, in hours
	LinkedHashSet[] cumulativeVehiclesLoaded;  //the cumulative number of vehicles loaded
	
	public WarehouseLoadingCapacity(double planningHorizon){
		int numPeriods = (int) Math.ceil(planningHorizon/intervalLength) + 1;
		cumulativeVehiclesLoaded = new LinkedHashSet[numPeriods];
		for(int i=0; i<numPeriods; i++){
			cumulativeVehiclesLoaded[i] = new LinkedHashSet<Route>();
		}
	}
	
	/**
	 * print out the warehouse capacity
	 * @param routes the routes to be considered
	 * @throws IOException 
	 */
	public void printWarehouseCapacity(Collection<Route> routes, BufferedWriter writer) throws IOException{
		//the vehicles loaded currently
		LinkedHashSet[] vehiclesLoaded = new LinkedHashSet[cumulativeVehiclesLoaded.length];
		for(int i=0; i<cumulativeVehiclesLoaded.length; i++){
			vehiclesLoaded[i] = new LinkedHashSet<Route>();
		}
		
		for(Route r : routes){
			int period = (int) (r.getOriginTime()/intervalLength);
			if(period<cumulativeVehiclesLoaded.length){
				vehiclesLoaded[period].add(r);
			}
		}
		
		LinkedHashSet<Route> previousRoutes = new LinkedHashSet<Route>();
		for(int i=0; i<cumulativeVehiclesLoaded.length; i++){
			writer.write(i +"\n");
			LinkedHashSet<Route> newRoutes = new LinkedHashSet<Route>(cumulativeVehiclesLoaded[i]);
			newRoutes.removeAll(previousRoutes);
			for(Route r : newRoutes){
				
//				if(r.toString().contains("[018-449REF]")){
//					System.out.println();
//				}
				
				writer.write("\t"+ StringProcessor.getString(r.getOriginTime(), 5)+"\t" + r + "\n" );
			}
			
			previousRoutes = cumulativeVehiclesLoaded[i];
			
			writer.write("--------------------\n");
			for(Route r : new LinkedHashSet<Route>(vehiclesLoaded[i])){
				writer.write("\t"+ StringProcessor.getString(r.getOriginTime(), 5)+"\t" + r + "\n" );
			}
			if(!newRoutes.equals(vehiclesLoaded[i])){
				System.out.println("ERROR in warehouse capacity, it is not consistent");
				writer.write("ERROR in warehouse capacity, it is not consistent\n");
			}
			writer.flush();
		}
	}
	
	/**
	 * Assume the starting time of the route is changed from @oldTime to @newTime, update the map
	 * there are three situations:
	 * 1. @oldTime > @newTime: add the route between @newTime and @oldTime
	 * 2. @newTime > @oldTime: remove route between @oldTime and @newTime
	 * 3. Route no longer contains any node, remove everything
	 * @param oldTime
	 * @param newTime
	 * @param route
	 */
	public void updateRoute(double oldTime, double newTime, Route route){
		
		if(route.getArcs().size()==0 || route.getOrderNodes().size()==0){
			removeVehicle(0, route);
		}
		else if(oldTime > newTime){
			addVehicle(newTime, oldTime, route);
		}
		else if(newTime > oldTime){
			removeVehicle(oldTime, newTime, route);
		}
		
	}
	
	/**
	 * remove the vehicle from @startTime to the end of the planning horizon
	 * @param startTime start time of the route
	 * @param route the route to be removed
	 */
	public void removeVehicle(double startTime, Route route){
		int period = (int) (startTime/intervalLength);
		for(int i = period; i<cumulativeVehiclesLoaded.length; i++){
			cumulativeVehiclesLoaded[i].remove(route);
		}
	}

	/**
	 * remove the vehicle from @startTime to the @endtime(not included)
	 * @param startTime start time of removing
	 * @param endTime the endtime of removing
	 * @param route the route to be removed
	 */
	public void removeVehicle(double startTime, double endTime, Route route){
		int periodStart = (int) (startTime/intervalLength);
		int periodEnd = (int) (endTime/intervalLength);
		for(int i = periodStart; i<Math.min(periodEnd, cumulativeVehiclesLoaded.length); i++){
			cumulativeVehiclesLoaded[i].remove(route);
		}
	}
	
	/**
	 * add a vehicle from <period> onwards
	 * @param period
	 */
	private void addVehiclePeriod(int period, Route route){
		if(period==0){
			System.out.println("Warehoseloadingcpacity");
		}
		for(int i = period; i<cumulativeVehiclesLoaded.length; i++){
			cumulativeVehiclesLoaded[i].add(route);
		}
	}
	
	/**
	 * add a vehicle to to a specific time;
	 * @param time: recorded by the beginning of the planning horizon
	 */
	public void addVehicle(double time, Route route){
		int period = (int) (time/intervalLength);
		addVehiclePeriod(period, route);
	}

	/**
	 * add a vehicle from startTime to endTime(not included);
	 * @param time: recorded by the beginning of the planning horizon
	 */
	public void addVehicle(double startTime, double endTime, Route route){
		int startPeriod = (int) (startTime/intervalLength);
		if(startPeriod==0){
			System.out.println("Warehoseloadingcpacity");
		}
		
		int endPeriod = (int) (endTime/intervalLength);
		endPeriod = Math.min(endPeriod, cumulativeVehiclesLoaded.length);
		for(int i = startPeriod; i<endPeriod; i++){
			cumulativeVehiclesLoaded[i].add(route);
		}
	}
	
	/**
	 * check if there is enough capacity from startTime to endTime(not included);
	 * @param time: recorded by the beginning of the planning horizon
	 */
	public boolean isAvailable(double startTime, double endTime){
		int startPeriod = (int) (startTime/intervalLength);
		int endPeriod = (int) (endTime/intervalLength);
		endPeriod = Math.min(endPeriod, cumulativeVehiclesLoaded.length);
		
		for(int i = startPeriod; i<endPeriod; i++){
			if(i*Parameter.getNumTrucksPerSlot() <= cumulativeVehiclesLoaded[i].size()){
				return false;
			}
		}
		
		return true;
	}

//	/**
//	 * get the unavailable time window between <startTime> and <endTime>, if any;
//	 * @return [start unavailable time, end unavailable time], between <startTime> and <endTime>, and -1 otherwise.
//	 */
//	public double[] getUnAvailableTimeWindow(double startTime, double endTime){
//		int startPeriod = (int) (startTime/intervalLength);
//		int endPeriod = (int) (endTime/intervalLength);
//		endPeriod = Math.min(endPeriod, cumulativeVehiclesLoaded.length);
//		
//		double times[] = {-1, -1};
//		for(int i = startPeriod; i<endPeriod; i++){
//			if(isAvailableSlotPeriod(i)){
//				return i*intervalLength;
//			}
//		}
//		
//		
//		return ;
//	}
	
	/**
	 * check if a certain period has available slot. 
	 * Specifically, check if the number of trucks loaded at a specific period exceed the total limit
	 * @param period: the period to check
	 */
	private boolean isAvailableSlotPeriod(int period){
		return  period<cumulativeVehiclesLoaded.length 
					&& period*Parameter.getNumTrucksPerSlot() > cumulativeVehiclesLoaded[period].size();
	}
	
	/**
	 * check if a certain time has available slot. 
	 * Specifically, check if the number of trucks loaded at a specific time exceed the total limit
	 * the time is measured in hours
	 * @param time
	 * @return
	 */
	public boolean isAvailableSlotTime(double time){
		int period = (int) (time/intervalLength);
		return isAvailableSlotPeriod(period);
	}
	
	/**
	 * get the next available time on and after <time> 
	 * @return
	 */
	public double getNextAvaiableTime(double time){
		int period = (int) (time/intervalLength);
		
		if(period>=cumulativeVehiclesLoaded.length){
			return time;
		}
		while(!isAvailableSlotPeriod(period) ){
			period++;
		}
		
		return period * intervalLength;
	}

	
	/**
	 * 
	 * @return
	 */
	public double getSlotWindowStart(double time){
		int period = (int) (time/intervalLength);
		return period * intervalLength;
		
	}
	
	/**
	 * write the warehouse usage
	 * @param writer
	 * @throws IOException 
	 */
	public void writeWarehouseLoading(BufferedWriter writer) throws IOException{
		writer.write("The following are the warehouse capacity\n");
		for(int i=0; i<cumulativeVehiclesLoaded.length; i++){
			writer.write("\t"+i + "\t" + (i*Parameter.getNumTrucksPerSlot()) + "\t" + cumulativeVehiclesLoaded[i].size() +"\n" ); 
		}
	}
	
	/**
	 * double check if the swap cycle is available
	 * @param swapPath the swap path
	 * @param ina the object of inserting node
	 * @param rn the object of removing node
	 * @return
	 */
	public boolean isSwapCycleAvailable(SwapPath swapPath, InsertNodeIntoArc ina, RemoveNode rn, ReplaceNode newNode){
		int[] warehouseCapcityChange = new int[cumulativeVehiclesLoaded.length];
		if(swapPath!=null){
			for(ReplaceNode replaceNode : swapPath.getReplaceNodeList()){
				changeCapacity(replaceNode.getReplacedRoute().getOriginTime(), replaceNode.getNewOriginTime(), warehouseCapcityChange);
			}
		}
		if(newNode!=null){
			changeCapacity(newNode.getReplacedRoute().getOriginTime(), newNode.getNewOriginTime(), warehouseCapcityChange);
		}
		
		if(ina!=null){
			changeCapacity(ina.getRouteInserted().getOriginTime(), ina.getNewOriginTime(), warehouseCapcityChange);
		}
		
		if(rn!=null){
			if(rn.getRoute().getArcs().size()==2){
				int newPeriod = (int) (rn.getRoute().getOriginTime()/intervalLength);
				for(int i=newPeriod; i<warehouseCapcityChange.length; i++){
					warehouseCapcityChange[i] = warehouseCapcityChange[i] - 1;
				}
			}
			else{
				changeCapacity(rn.getRoute().getOriginTime(), rn.getNewOriginTime(), warehouseCapcityChange);
			}
		}
		
		return isAvailable(warehouseCapcityChange);
	}

	/**
	 * double check if the swap cycle is available
	 * @param swapPath the swap path
	 * @param ina the object of inserting node
	 * @param rn the object of removing node
	 * @return
	 */
	public boolean isSwapCycleAvailable(ReplaceNode nodeOne, ReplaceNode nodeTwo){
		int[] warehouseCapcityChange = new int[cumulativeVehiclesLoaded.length];
		changeCapacity(nodeOne.getReplacedRoute().getOriginTime(), nodeOne.getNewOriginTime(), warehouseCapcityChange);
		
		if(nodeTwo!=null){
			changeCapacity(nodeTwo.getReplacedRoute().getOriginTime(), nodeTwo.getNewOriginTime(), warehouseCapcityChange);
		}
		
		return isAvailable(warehouseCapcityChange);
	}

	/**
	 * double check if the swap cycle is available
	 * @param ina the object of inserting node
	 * @param rn the object of removing node
	 * @return
	 */
	public boolean isSwapCycleAvailable(InsertNodeIntoArc ina, RemoveNode rn, ReplaceNode newNode){
		return isSwapCycleAvailable(null, ina, rn, newNode);
	}

	/**
	 * double check if the swap cycle is available
	 * @param ina the object of inserting node
	 * @param rn the object of removing node
	 * @return
	 */
	public boolean isSwapCycleAvailable(InsertNodeIntoArc ina, RemoveNode rn){
		return isSwapCycleAvailable(null, ina, rn, null);
	}
	
	/**
	 * combine the warehouse capacity and its change to check if there is enough capacity, used by isSwapCycleAvailable
	 * @param warehouseCapcityChange
	 */
	private boolean isAvailable(int[] warehouseCapcityChange){
		for(int i=0; i<warehouseCapcityChange.length; i++){
			if(warehouseCapcityChange[i] + cumulativeVehiclesLoaded[i].size() > i * Parameter.getNumTrucksPerSlot() ){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * change the capacity map, use by function  isSwapCycleAvailable
	 *  there are two situations:
	 * 1. @oldTime > @newTime: add the route between @newTime and @oldTime
	 * 2. @newTime > @oldTime: remove route between @oldTime and @newTime
	 */
	private void changeCapacity(double oldTime, double newTime, int[] warehouseCapcityChange){
		
		int oldPeriod = (int) (oldTime/intervalLength);
		int newPeriod = (int) (newTime/intervalLength);
		
		if(oldPeriod>newPeriod){
			for(int i=newPeriod; i<Math.min(oldPeriod, warehouseCapcityChange.length); i++){
				warehouseCapcityChange[i] = warehouseCapcityChange[i] + 1;
			}
		}
		else if(oldPeriod<newPeriod){
			for(int i=oldPeriod; i<Math.min(newPeriod, warehouseCapcityChange.length); i++){
				warehouseCapcityChange[i] = warehouseCapcityChange[i] - 1;
			}
		}
	}
}
