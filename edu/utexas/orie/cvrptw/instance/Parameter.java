package edu.utexas.orie.cvrptw.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parameter {
	
	/**
	 * read the parameter from the input file
	 * @param inputFile
	 * @throws IOException 
	 */
	public static void readParameter(String inputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		String setUpCost = getNextLine(in);
		setPerStopCost(Double.parseDouble(setUpCost));
		
		String drivingCost = getNextLine(in);
		setPerMileCost(Double.parseDouble(drivingCost));
		
		String driverIdleCost = getNextLine(in);
		setDriverIdleCost(Double.parseDouble(driverIdleCost));
		
		String truckWeightLimit = getNextLine(in);
		setTruckWeightLimit(Double.parseDouble(truckWeightLimit));
		
		String truckVolumeLimit = getNextLine(in);
		setTruckVolumeLimit(Double.parseDouble(truckVolumeLimit));
		
		String compatibleN = getNextLine(in);
		setCompatibleOrders(Integer.parseInt(compatibleN));
		
		String reductionPerOrder = getNextLine(in);
		setVolumeReductionOrders(Double.parseDouble(reductionPerOrder));
		
		String reductionPerStore = getNextLine(in);
		setVolumeReductionStores(Double.parseDouble(reductionPerStore));
		
		String loadRate = getNextLine(in);
		setLoadRate(Double.parseDouble(loadRate));
		
		String loadSetUpTime = getNextLine(in);
		setLoadSetUpTime(Double.parseDouble(loadSetUpTime));
		
		String driverTimeLimit = getNextLine(in);
		setDrivingTimeLimitPerRoute(Double.parseDouble(driverTimeLimit) * 60);

		String totalTimeLimit = getNextLine(in);
		setTotalTimeLimitPerRoute(Double.parseDouble(totalTimeLimit) * 60);

		String numTrucksPerSlot = getNextLine(in);
		setNumTrucksPerSlot(Integer.parseInt(numTrucksPerSlot) );
		
		
		
		in.close();
	}
	
	/**
	 * get the next line in the input file
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	private static String getNextLine(BufferedReader in) throws IOException{
		String line = in.readLine();
		while(line==null || line.contains("*")){
			line = in.readLine();
		}
		
		return line.split(":")[1].trim();
	}
	
	/**
	 * the cost to stop at every stop
	 */
	private static double perStopCost;
	
	/**
	 * the cost for trucks to travel per mile
	 */
	private static double perMileCost;
	
	/**
	 * the cost for driver idle time
	 */
	private static double driverIdleCost;
	
	/**
	 * the limit for the truck weights
	 */
	private static double truckWeightLimit;  

	/**
	 * the limit for the truck Volume 
	 */
	private static double truckVolumeLimit;
	
	/**
	 * the pick up orders should be the closest some orders
	 */
	private static int compatibleOrders;   
	
	/**
	 * the Volume reduction between different orders
	 */
	private static double volumeReductionOrders;
	/**
	 * the Volume reduction between different stores
	 */
	private static double volumeReductionStores;
	
	/**
	 * loading or unloading rate at the stores
	 */
	private static double loadRate;
	
	/**
	 * the set up time to load and unload
	 */
	private static double loadSetUpTime;
	
	/**
	 * limit on the driving time per route
	 */
	private static double drivingTimeLimitPerRoute;
	/**
	 * limit on the total time per route
	 */
	private static double totalTimeLimitPerRoute;
		
	/**
	 * number of trucks can be processed in every warehouse time slot
	 */
	
	private static int numTrucksPerSlot;
	
	
	/**
	 * @return the truckWeightLimit
	 */
	public static double getTruckWeightLimit() {
		return truckWeightLimit;
	}

	/**
	 * @return the truckVolumeLimit
	 */
	public static double getTruckVolumeLimit() {
		return truckVolumeLimit;
	}

	/**
	 * @return the compatiblePickUpOrders
	 */
	public static int getCompatibleOrders() {
		return compatibleOrders;
	}

	/**
	 * @return the VolumeReductionOrders
	 */
	public static double getVolumeReductionOrders() {
		return volumeReductionOrders;
	}

	/**
	 * @return the VolumeReductionStores
	 */
	public static double getVolumeReductionStores() {
		return volumeReductionStores;
	}

	/**
	 * @return the loadRate
	 */
	public static double getLoadRate() {
		return loadRate;
	}

	/**
	 * @return the loadSetUpTime
	 */
	public static double getLoadSetUpTime() {
		return loadSetUpTime/60;
	}

	/**
	 * @return the drivingTimeLimitPerRoute
	 */
	public static double getDrivingTimeLimitPerRouteHour() {
		return drivingTimeLimitPerRoute/60;
	}

	/**
	 * @return the totalTimeLimitPerRoute
	 */
	public static double getTotalTimeLimitPerRouteHour() {
		return totalTimeLimitPerRoute/60;
	}

	/**
	 * @param truckWeightLimit the truckWeightLimit to set
	 */
	public static void setTruckWeightLimit(double truckWeightLimit) {
		Parameter.truckWeightLimit = truckWeightLimit;
	}

	/**
	 * @param truckVolumeLimit the truckVolumeLimit to set
	 */
	public static void setTruckVolumeLimit(double truckVolumeLimit) {
		Parameter.truckVolumeLimit = truckVolumeLimit;
	}

	/**
	 * @param compatiblePickUpOrders the compatiblePickUpOrders to set
	 */
	public static void setCompatibleOrders(int compatibleOrders) {
		Parameter.compatibleOrders = compatibleOrders;
	}

	/**
	 * @param VolumeReductionOrders the VolumeReductionOrders to set
	 */
	public static void setVolumeReductionOrders(double volumemeReductionOrders) {
		Parameter.volumeReductionOrders = volumemeReductionOrders;
	}

	/**
	 * @param VolumeReductionStores the VolumeReductionStores to set
	 */
	public static void setVolumeReductionStores(double volumeReductionStores) {
		Parameter.volumeReductionStores = volumeReductionStores;
	}

	/**
	 * @param loadRate the loadRate to set
	 */
	public static void setLoadRate(double loadRate) {
		Parameter.loadRate = loadRate;
	}

	/**
	 * @param loadSetUpTime the loadSetUpTime to set
	 */
	public static void setLoadSetUpTime(double loadSetUpTime) {
		Parameter.loadSetUpTime = loadSetUpTime;
	}

	/**
	 * @param drivingTimeLimitPerRoute the drivingTimeLimitPerRoute to set
	 */
	public static void setDrivingTimeLimitPerRoute(double drivingTimeLimitPerRoute) {
		Parameter.drivingTimeLimitPerRoute = drivingTimeLimitPerRoute;
	}

	/**
	 * @param totalTimeLimitPerRoute the totalTimeLimitPerRoute to set
	 */
	public static void setTotalTimeLimitPerRoute(double totalTimeLimitPerRoute) {
		Parameter.totalTimeLimitPerRoute = totalTimeLimitPerRoute;
	}

	/**
	 * @return the perMileCost
	 */
	public static double getPerMileCost() {
		return perMileCost;
	}

	/**
	 * @return the driverIdleCost
	 */
	public static double getDriverIdleCost() {
		return driverIdleCost;
	}

	/**
	 * @param perMileCost the perMileCost to set
	 */
	public static void setPerMileCost(double perMileCost) {
		Parameter.perMileCost = perMileCost;
	}

	/**
	 * @param driverIdleCost the driverIdleCost to set
	 */
	public static void setDriverIdleCost(double driverIdleCost) {
		Parameter.driverIdleCost = driverIdleCost;
	}

	/**
	 * @return the perStopCost
	 */
	public static double getPerStopCost() {
		return perStopCost;
	}

	/**
	 * @param perStopCost the perStopCost to set
	 */
	public static void setPerStopCost(double perStopCost) {
		Parameter.perStopCost = perStopCost;
	}

	/**
	 * @return the numTrucksPerSlot
	 */
	public static int getNumTrucksPerSlot() {
		return numTrucksPerSlot;
	}

	/**
	 * @param numTrucksPerSlot the numTrucksPerSlot to set
	 */
	public static void setNumTrucksPerSlot(int numTrucksPerSlot) {
		Parameter.numTrucksPerSlot = numTrucksPerSlot;
	}
	
	
	
	
}
