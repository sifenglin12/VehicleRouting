package edu.utexas.orie.cvrptw.test;

import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.utexas.orie.cvrptw.cplex_solver.CplexSolver;
import edu.utexas.orie.cvrptw.grasp.LocalImprovement;
import edu.utexas.orie.cvrptw.grasp.SolutionConstructor;
import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.output.Diagram;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.StringProcessor;
import edu.utexas.orie.cvrptw.output.VRPSolution;
import edu.utexas.orie.cvrptw.tabu.TabuSearch;

public class Test {
	
	public static BufferedWriter writer;
	static String dataNames[] = {
		"0910STKC",
		"0911STKC",
		"0912STKC",
		"0913STKC",
		"0914STKC",
		"0915STKC"
	};
	
	public static void main(String[] args) {
		String purpose = "NumVehiclesOptimal";
		String date = StringProcessor.getDate();
		String outputFolder = "output/VRPTW_"+purpose+date;
		(new File(outputFolder)).mkdirs();
		try {
			writer = new BufferedWriter( new FileWriter(outputFolder + "/statistics" + date + ".csv"));
			Statistics.writeHeader("Method", writer);
			testDifferentVehicles(dataNames, outputFolder);
//			testDifferentStores(dataNames, outputFolder);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void test(String dataName, String outputFolder){
		try {
			String curFolder = outputFolder + "/" + dataName;
			(new File(curFolder)).mkdirs();
			
			
			InstanceCVRPTW instance = new InstanceCVRPTW();
			instance.readInstance(dataName);
			
			instance.plotStores(curFolder+"/storeMap");
			Network network = instance.generateNetwork();
			VRPSolution solu = testConstructor(curFolder, instance, network);
			
			testLocalImprovement(curFolder, instance, network, solu);
			
			testTabu(curFolder, instance, network, solu);
			
//			testCPLEX(curFolder, instance, network, solu);
			
			System.out.println("\t\tFinish");
			
//			SeedGenerator generator = new SeedGenerator(instance, network);
//			generator.getSeedsMaxSumEachMinTimeHeuristic();
			
//			String constructorFolder = curFolder + "/constructor";
//			(new File(constructorFolder)).mkdirs();
//			SolutionConstructor soluConstructor = new SolutionConstructor(instance, network, constructorFolder);
//			VRPSolution solu = soluConstructor.generateSolution();
//			soluConstructor.generateStatistics().write("", writer);
//			
//			String improveFolder = curFolder + "/localImprove";
//			(new File(improveFolder)).mkdirs();
//			LocalImprovement localImprovement = new LocalImprovement(instance, network, improveFolder);
//			localImprovement.localSearchBetweenRoutes(solu, 5);
//			localImprovement.generateStatistics().write("", writer);
//			
//			String cplexFolder = curFolder + "/cplexImprove";
//			(new File(cplexFolder)).mkdirs();
//			instance.setNumTrucks(solu.getRoutes().size());
//			CplexSolver solver = new CplexSolver(instance, network, cplexFolder);
//			solver.solve();
//			solver.generateStatistics().write("", writer);
//
//			String tabuFolder = curFolder + "/tabuImprove";
//			(new File(tabuFolder)).mkdirs();
//			TabuSearch tabuSearch = new TabuSearch(instance, network, tabuFolder);
//			tabuSearch.tabuSearch(solu);
//			tabuSearch.generateStatistics().write("", writer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testDifferentStores(String dataName, String outputFolder){
		try {
			System.out.println(dataName);
			String curFolder = outputFolder + "/" + dataName;
			(new File(curFolder)).mkdirs();
			
			InstanceCVRPTW instance = new InstanceCVRPTW();
			instance.readInstance(dataName);
			
			for(int i=1; i<=10; i++){
				testNumStores(curFolder, instance, i*5);
			}
			
//			testDifferentVehicles(curFolder, instance, 10);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void testDifferentVehicles(String[] dataNames, String outputFolder){
		
		for(String dataName: dataNames){
			try {
				System.out.println(dataName);
				String curFolder = outputFolder + "/" + dataName;
				(new File(curFolder)).mkdirs();
				
				InstanceCVRPTW instance = new InstanceCVRPTW();
				instance.readInstance(dataName);
				
				for(int i=1; i<=2; i++){
					testDifferentVehicles(curFolder, instance, i*5);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IloException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void testDifferentStores(String[] dataNames, String outpputFolder){
		for(String dn : dataNames){
			testDifferentStores(dn, outpputFolder);
		}
	}
	
	public static void testNumStores(String folder, InstanceCVRPTW originalInstance, int numStores) throws IOException, IloException{
		InstanceCVRPTW instance = (numStores>= originalInstance.getStores().size()) ? 
									originalInstance : originalInstance.getSubInstance(numStores);
		
		System.out.println("\tNumber of of stores "+numStores);
		Network network = instance.generateNetwork();
		String curFolder = folder + "/NumStores" + numStores;
		
		VRPSolution solu = testConstructor(curFolder, instance, network);
		
		testLocalImprovement(curFolder, instance, network, solu);
		
		testTabu(curFolder, instance, network, solu);
		
		testCPLEX(curFolder, instance, network, solu);
		
		System.out.println("\t\tFinish");
	}
	
	public static VRPSolution testCPLEX(String curFolder, InstanceCVRPTW instance, Network network, VRPSolution solu) throws IOException{
		System.out.println("\t\tCPLEX Solu ");
		String cplexFolder = curFolder + "/cplexSolu";
		(new File(cplexFolder)).mkdirs();
		instance.setNumTrucks(solu.getRoutes().size());
		CplexSolver solver = new CplexSolver(instance, network, cplexFolder);
		VRPSolution curSolu = null;
		try {
			curSolu = solver.solve();
		} catch (IloException e) {
			e.printStackTrace();
		}
//		solver.generateStatistics().write("CPLEX", writer);
		
		Statistics statistics = solver.generateStatistics();
		statistics.setWaitCost(solu.getWaitCost());
		statistics.setTravelCost(solu.getTotalTravelCost());
		statistics.write("CPLEX", writer);
		
		
		return curSolu;
	}
	
	public static VRPSolution testTabu(String curFolder, InstanceCVRPTW instance, Network network, VRPSolution solu) throws IOException{

		System.out.println("\t\tTabu Improve ");
		String tabuFolder = curFolder + "/tabuImprove";
		(new File(tabuFolder)).mkdirs();
		TabuSearch tabuSearch = new TabuSearch(instance, network, tabuFolder);
		VRPSolution curSolu = tabuSearch.tabuSearch(solu);
		
		Statistics statistics = tabuSearch.generateStatistics();
		statistics.setWaitCost(solu.getWaitCost());
		statistics.setTravelCost(solu.getTotalTravelCost());
		statistics.write("Tabu", writer);
		
//		tabuSearch.generateStatistics().write("Tabu", writer);
		
		return curSolu;
	}
	
	public static VRPSolution testLocalImprovement(String curFolder, InstanceCVRPTW instance, Network network, VRPSolution solu) throws IOException{

		System.out.println("\t\tLocal Improve ");
		String improveFolder = curFolder + "/localImprove";
		(new File(improveFolder)).mkdirs();
		LocalImprovement localImprovement = new LocalImprovement(instance, network, improveFolder);
		VRPSolution curSolu = localImprovement.localSearchBetweenRoutes(solu, 5);
//		localImprovement.generateStatistics().write("LargeNeighbor", writer);
		
		Statistics statistics = localImprovement.generateStatistics();
		statistics.setWaitCost(solu.getWaitCost());
		statistics.setTravelCost(solu.getTotalTravelCost());
		statistics.write("LocalImprove", writer);
		
		return curSolu;
		
	}
	
	public static VRPSolution testConstructor(String curFolder, InstanceCVRPTW instance, Network network) throws IOException{
		System.out.println("\t\tConstructor ");
		String constructorFolder = curFolder + "/constructor";
		(new File(constructorFolder)).mkdirs();
		SolutionConstructor soluConstructor = new SolutionConstructor(instance, network, constructorFolder);
		VRPSolution solu = soluConstructor.generateSolution();
		Statistics statistics = soluConstructor.generateStatistics();
		statistics.setWaitCost(solu.getWaitCost());
		statistics.setTravelCost(solu.getTotalTravelCost());
		statistics.write("Initial", writer);
		
		return solu;
		
	}
	
	public static void testDifferentVehicles(String folder, InstanceCVRPTW originalInstance, int numStores) throws IOException, IloException{
		InstanceCVRPTW instance = (numStores>= originalInstance.getStores().size()) ? 
				originalInstance : originalInstance.getSubInstance(numStores);

		System.out.println("\tNumber of of stores "+numStores);
		Network network = instance.generateNetwork();
		String curFolder = folder + "/NumStores" + numStores;
		
		System.out.println("\t\tConstructor "+numStores);
		String constructorFolder = curFolder + "/constructor";
		(new File(constructorFolder)).mkdirs();
		SolutionConstructor soluConstructor = new SolutionConstructor(instance, network, constructorFolder);
		VRPSolution solu = soluConstructor.generateSolution();
		soluConstructor.generateStatistics().write("Initial", writer);
		
		System.out.println("\t\tLocal Improve "+numStores);
		String improveFolder = curFolder + "/localImprove";
		(new File(improveFolder)).mkdirs();
		LocalImprovement localImprovement = new LocalImprovement(instance, network, improveFolder);
		localImprovement.localSearchBetweenRoutes(solu, 5);
		localImprovement.generateStatistics().write("LargeNeighbor", writer);
		
		for(int i=-2; i<3; i++){
			System.out.println("\t\tCPLEX Solu "+numStores +" NumVehicles" + i);
			String cplexFolder = curFolder + "/cplexSolu" + i;
			(new File(cplexFolder)).mkdirs();
			instance.setNumTrucks(solu.getRoutes().size() + i);
			CplexSolver solver = new CplexSolver(instance, network, cplexFolder);
			solver.solve();
			solver.generateStatistics().write("CPLEX", writer);
			System.out.println("\t\tFinish");
		}
		
	}
	
	
	public static void test(String[] dataNames, String outpputFolder){
		for(String dn : dataNames){
			test(dn, outpputFolder);
		}
	}

}
