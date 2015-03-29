package edu.utexas.orie.cvrptw.output;

import java.io.BufferedWriter;
import java.io.IOException;

public class Statistics {
	
	String dataName;
	int numStores = -1;
	int numOrders = -1;
	double totalCube = -1;
	double totalWeight = -1;
	
	int numOrdersGrocery = -1;
	int numOrdersRefrigerate = -1;
	int numOrdersFrozen = -1;
	int numOrdersSalvage = -1;

	double cubeOrdersGrocery = -1;
	double cubeOrdersRefrigerate = -1;
	double cubeOrdersFrozen = -1;
	double cubeOrdersSalvage = -1;
	
	double weightOrdersGrocery = -1;
	double weightOrdersRefrigerate = -1;
	double weightOrdersFrozen = -1;
	double weightOrdersSalvage = -1;

	/**The solution methods applied*/
	String method="";
	double totalTime;
	double totalCost;
	double travelCost;
	double waitCost;
	
	public static void writeHeader(String optionName, BufferedWriter writer) throws IOException{
		writer.write("DataName");
		writer.write(","+optionName);
		writer.write(",TotalTime");
		writer.write(",TotalCost");
		writer.write(",TravelCost");
		writer.write(",WaitCost");

		writer.write(",NumStores");
		writer.write(",NumOrders");
		writer.write(",Cube");
		writer.write(",Weight");
		
		writer.write(",NumGroceryOrders");
		writer.write(",NumRefrigerateOrders");
		writer.write(",NumFrozenOrders");
		writer.write(",NumSalvageOrders");
		
		writer.write(",CubeGroceryOrders");
		writer.write(",CubeRefrigerateOrders");
		writer.write(",CubeFrozenOrders");
		writer.write(",CubeSalvageOrders");
		
		writer.write(",WeightGroceryOrders");
		writer.write(",WeightRefrigerateOrders");
		writer.write(",WeightFrozenOrders");
		writer.write(",WeightSalvageOrders");


		writer.newLine();
		writer.flush();
	}
	
	
	public void write(String option, BufferedWriter writer) throws IOException{
		writer.write(""+dataName);
		writer.write(","+option);
		writer.write(","+totalTime);
		writer.write(","+totalCost);
		writer.write(","+travelCost);
		writer.write(","+waitCost);

		writer.write(","+numStores);
		writer.write(","+numOrders);
		writer.write(","+totalCube);
		writer.write(","+totalWeight);
		
		writer.write(","+numOrdersGrocery);
		writer.write(","+numOrdersRefrigerate);
		writer.write(","+numOrdersFrozen);
		writer.write(","+numOrdersSalvage);
		
		writer.write(","+cubeOrdersGrocery);
		writer.write(","+cubeOrdersRefrigerate);
		writer.write(","+cubeOrdersFrozen);
		writer.write(","+cubeOrdersSalvage);
		
		writer.write(","+weightOrdersGrocery);
		writer.write(","+weightOrdersRefrigerate);
		writer.write(","+weightOrdersFrozen);
		writer.write(","+weightOrdersSalvage);

		
		writer.newLine();
		writer.flush();
	}

	/**
	 * @return the travelCost
	 */
	public double getTravelCost() {
		return travelCost;
	}


	/**
	 * @return the waitCost
	 */
	public double getWaitCost() {
		return waitCost;
	}


	/**
	 * @param travelCost the travelCost to set
	 */
	public void setTravelCost(double travelCost) {
		this.travelCost = travelCost;
	}


	/**
	 * @param waitCost the waitCost to set
	 */
	public void setWaitCost(double waitCost) {
		this.waitCost = waitCost;
	}


	/**
	 * @return the dataName
	 */
	public String getDataName() {
		return dataName;
	}


	/**
	 * @return the numStores
	 */
	public int getNumStores() {
		return numStores;
	}


	/**
	 * @return the numOrders
	 */
	public int getNumOrders() {
		return numOrders;
	}


	/**
	 * @return the totalCube
	 */
	public double getTotalCube() {
		return totalCube;
	}


	/**
	 * @return the totalWeight
	 */
	public double getTotalWeight() {
		return totalWeight;
	}

	/**
	 * @return the numOrdersGrocery
	 */
	public int getNumOrdersGrocery() {
		return numOrdersGrocery;
	}


	/**
	 * @return the numOrdersRefrigerate
	 */
	public int getNumOrdersRefrigerate() {
		return numOrdersRefrigerate;
	}


	/**
	 * @return the numOrdersFrozen
	 */
	public int getNumOrdersFrozen() {
		return numOrdersFrozen;
	}


	/**
	 * @return the numOrdersSalvage
	 */
	public int getNumOrdersSalvage() {
		return numOrdersSalvage;
	}


	/**
	 * @return the cubeOrdersGrocery
	 */
	public double  getCubeOrdersGrocery() {
		return cubeOrdersGrocery;
	}


	/**
	 * @return the cubeOrdersRefrigerate
	 */
	public double  getCubeOrdersRefrigerate() {
		return cubeOrdersRefrigerate;
	}


	/**
	 * @return the cubeOrdersFrozen
	 */
	public double  getCubeOrdersFrozen() {
		return cubeOrdersFrozen;
	}


	/**
	 * @return the cubeOrdersSalvage
	 */
	public double  getCubeOrdersSalvage() {
		return cubeOrdersSalvage;
	}


	/**
	 * @return the weightOrdersGrocery
	 */
	public double  getWeightOrdersGrocery() {
		return weightOrdersGrocery;
	}


	/**
	 * @return the weightOrdersRefrigerate
	 */
	public double  getWeightOrdersRefrigerate() {
		return weightOrdersRefrigerate;
	}


	/**
	 * @return the weightOrdersFrozen
	 */
	public double  getWeightOrdersFrozen() {
		return weightOrdersFrozen;
	}


	/**
	 * @return the weightOrdersSalvage
	 */
	public double  getWeightOrdersSalvage() {
		return weightOrdersSalvage;
	}


	/**
	 * @param dataName the dataName to set
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}


	/**
	 * @param numStores the numStores to set
	 */
	public void setNumStores(int numStores) {
		this.numStores = numStores;
	}


	/**
	 * @param numOrders the numOrders to set
	 */
	public void setNumOrders(int numOrders) {
		this.numOrders = numOrders;
	}

	/**
	 * @param totalCube the totalCube to set
	 */
	public void setTotalCube(double totalCube) {
		this.totalCube = totalCube;
	}


	/**
	 * @param totalWeight the totalWeight to set
	 */
	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	/**
	 * @param numOrdersGrocery the numOrdersGrocery to set
	 */
	public void setNumOrdersGrocery(int numOrdersGrocery) {
		this.numOrdersGrocery = numOrdersGrocery;
	}


	/**
	 * @param numOrdersRefrigerate the numOrdersRefrigerate to set
	 */
	public void setNumOrdersRefrigerate(int numOrdersRefrigerate) {
		this.numOrdersRefrigerate = numOrdersRefrigerate;
	}


	/**
	 * @param numOrdersFrozen the numOrdersFrozen to set
	 */
	public void setNumOrdersFrozen(int numOrdersFrozen) {
		this.numOrdersFrozen = numOrdersFrozen;
	}


	/**
	 * @param numOrdersSalvage the numOrdersSalvage to set
	 */
	public void setNumOrdersSalvage(int numOrdersSalvage) {
		this.numOrdersSalvage = numOrdersSalvage;
	}


	/**
	 * @param cubeOrdersGrocery the cubeOrdersGrocery to set
	 */
	public void setCubeOrdersGrocery(double cubeOrdersGrocery) {
		this.cubeOrdersGrocery = cubeOrdersGrocery;
	}


	/**
	 * @param cubeOrdersRefrigerate the cubeOrdersRefrigerate to set
	 */
	public void setCubeOrdersRefrigerate(double cubeOrdersRefrigerate) {
		this.cubeOrdersRefrigerate = cubeOrdersRefrigerate;
	}


	/**
	 * @param cubeOrdersFrozen the cubeOrdersFrozen to set
	 */
	public void setCubeOrdersFrozen(double cubeOrdersFrozen) {
		this.cubeOrdersFrozen = cubeOrdersFrozen;
	}


	/**
	 * @param cubeOrdersSalvage the cubeOrdersSalvage to set
	 */
	public void setCubeOrdersSalvage(double cubeOrdersSalvage) {
		this.cubeOrdersSalvage = cubeOrdersSalvage;
	}


	/**
	 * @param weightOrdersGrocery the weightOrdersGrocery to set
	 */
	public void setWeightOrdersGrocery(double weightOrdersGrocery) {
		this.weightOrdersGrocery = weightOrdersGrocery;
	}


	/**
	 * @param weightOrdersRefrigerate the weightOrdersRefrigerate to set
	 */
	public void setWeightOrdersRefrigerate(double weightOrdersRefrigerate) {
		this.weightOrdersRefrigerate = weightOrdersRefrigerate;
	}


	/**
	 * @param weightOrdersFrozen the weightOrdersFrozen to set
	 */
	public void setWeightOrdersFrozen(double weightOrdersFrozen) {
		this.weightOrdersFrozen = weightOrdersFrozen;
	}


	/**
	 * @param weightOrdersSalvage the weightOrdersSalvage to set
	 */
	public void setWeightOrdersSalvage(double weightOrdersSalvage) {
		this.weightOrdersSalvage = weightOrdersSalvage;
	}


	/**
	 * @return the totalCost
	 */
	public double getTotalCost() {
		return totalCost;
	}


	/**
	 * @param totalCost the totalCost to set
	 */
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}


	/**
	 * @return the totalTime
	 */
	public double getTotalTime() {
		return totalTime;
	}


	/**
	 * @param totalTime the totalTime to set
	 */
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}
	
	
}
