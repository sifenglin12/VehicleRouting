package edu.utexas.orie.cvrptw.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import edu.utexas.orie.cvrptw.network.Arc;
import edu.utexas.orie.cvrptw.network.Network;
import edu.utexas.orie.cvrptw.network.OrderNode;
import edu.utexas.orie.cvrptw.network.SinkNode;
import edu.utexas.orie.cvrptw.network.SourceNode;
import edu.utexas.orie.cvrptw.output.Diagram;
import edu.utexas.orie.cvrptw.output.Statistics;
import edu.utexas.orie.cvrptw.output.StringProcessor;
import edu.utexas.orie.cvrptw.output.TimeProcessor;

public class InstanceCVRPTW {
	
	/**
	 * name of the batch
	 */
	String dataName = "";
	String batch;
	Warehouse warehouse;
	ArrayList<Store> stores;
	HashMap<Location, HashMap<Location, Double>> distanceMatrix;
	HashMap<Location, HashMap<Location, Double>> travelTimeMatrix;
	HashMap<String, Store> storeMap;  //map the name of the store to the store
	int numTrucks = 20;
	
//	Statistics statistics;
	
	/**
	 * get a sub instance that contains the first @numStores
	 * @param numStores
	 * @return
	 */
	public InstanceCVRPTW getSubInstance(int numStores){
		ArrayList<Store> newStores = new ArrayList<Store>();
		for(int i=0; i<numStores; i++){
			newStores.add(stores.get(i));
		}
		
		InstanceCVRPTW newInstance = new InstanceCVRPTW();
		newInstance.setDataName(this.dataName);
		newInstance.setBatch(this.batch);
		newInstance.setWarehouse(this.warehouse);
		newInstance.setStores(newStores);
		newInstance.distanceMatrix = this.distanceMatrix;
		newInstance.travelTimeMatrix = this.travelTimeMatrix;
		newInstance.storeMap = this.storeMap;
		
		return newInstance;
	}
	
	/**
	 * plot stores
	 */
	public void plotStores(String outputFile){
		try {
			Diagram.plotStore(outputFile, stores, warehouse);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * compare the factor of stores
	 */
	public void compareFactors(){
		
		for(Store storeOne: stores){
			for(Store storeTwo: stores){
				if(storeOne!=storeTwo){
					if(storeOne.getLatitude()>0 && storeTwo.getLatitude()>0){
						double val = Math.sqrt(Math.pow(storeOne.getLatitude() - storeTwo.getLatitude(), 2) + 
								Math.pow(storeOne.getLongitude() - storeTwo.getLongitude(), 2));
						if(distanceMatrix.get(storeOne).get(storeTwo)>50){
							System.out.println( distanceMatrix.get(storeOne).get(storeTwo)/val );
						}
						
					}
				}
			}
		}
	}
	
	/**
	 * get the distance between two stores
	 * @param storeOne
	 * @param storeTwo
	 * @return
	 */
	public double getDistance(Location locationOne, Location locationTwo){
		return distanceMatrix.get(locationOne).get(locationTwo);
	}

	/**
	 * get the distance between two stores
	 * @param storeOne
	 * @param storeTwo
	 * @return
	 */
	public double getTravelTimeHour(Location locationOne, Location locationTwo){
		return travelTimeMatrix.get(locationOne).get(locationTwo)/60;
	}
	
	/**
	 * @return the batch
	 */
	public String getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(String batch) {
		this.batch = batch;
	}
	
	public void readInstance(String dataName) throws IOException{
		
		String inputFolder = "input/";
		
		
		Parameter.readParameter(inputFolder + "Parameters.txt");
		Catlog.readProductType(inputFolder +"ProductTypes.txt");
		
		String instanceFolder = "input/"+dataName+"/";
		
		warehouse = new Warehouse();
		readPullTime(instanceFolder+dataName+"_Pulltime.csv");
		readStore(instanceFolder+dataName+"_Windows.csv");
		warehouse.setName(stores.get(0).getName().split("-")[0]);
		readDeliveryOrders(instanceFolder+dataName+"_Orders.csv");
		readSalvageOrders(instanceFolder+dataName+"_Salvage.csv");
		readStoreLocation();
		
		readTimeDistance(inputFolder + "TimeMatrix.csv", inputFolder + "DistanceMatrix.csv");
		this.dataName = dataName;
//		compareFactors();
	}
	
	/**
	 * generate the statistics
	 */
	public void setStat(Statistics statistics){
//		statistics = new Statistics();
		statistics.setDataName(dataName);
		statistics.setNumStores(stores.size());
		int numOrders = 0;
		int totalCube = 0;
		int totalWeight = 0;
		
		int numOrdersGrocery = 0;
		int numOrdersRefrigerate = 0;
		int numOrdersFrozen = 0;
		int numOrdersSalvage = 0;

		double cubeOrdersGrocery = 0;
		double cubeOrdersRefrigerate = 0;
		double cubeOrdersFrozen = 0;
		double cubeOrdersSalvage = 0;
		
		double weightOrdersGrocery = 0;
		double weightOrdersRefrigerate = 0;
		double weightOrdersFrozen = 0;
		double weightOrdersSalvage = 0;
		
		for(Store store : stores){
			numOrders += store.getDeliveryOrders().size();
			numOrders += store.getSalvageOrders().size();
			
			for(Order order : store.getDeliveryOrders()){
				totalCube += order.getCube();
				totalWeight += order.getWeight();
				
				if(Catlog.grocery.equals(order.getCatlog())){
					numOrdersGrocery++;
					cubeOrdersGrocery = cubeOrdersGrocery + order.getCube();
					weightOrdersGrocery = weightOrdersGrocery + order.getWeight();
				}
				else if(Catlog.refrigerated.equals(order.getCatlog())){
					numOrdersRefrigerate++;
					cubeOrdersRefrigerate = cubeOrdersRefrigerate + order.getCube();
					weightOrdersRefrigerate = weightOrdersRefrigerate + order.getWeight();
				}
				else if(Catlog.frozen.equals(order.getCatlog())){
					numOrdersFrozen++;
					cubeOrdersFrozen = cubeOrdersFrozen + order.getCube();
					weightOrdersFrozen = weightOrdersFrozen + order.getWeight();
				}
			}
			
			for(Order order : store.getSalvageOrders()){
				numOrdersSalvage++;
				cubeOrdersSalvage = cubeOrdersSalvage + order.getCube();
				weightOrdersSalvage = weightOrdersSalvage + order.getWeight();
			}
		}
		
		statistics.setNumOrders(numOrders);
		statistics.setNumOrdersGrocery(numOrdersGrocery);
		statistics.setNumOrdersRefrigerate(numOrdersRefrigerate);
		statistics.setNumOrdersFrozen(numOrdersFrozen);
		statistics.setNumOrdersSalvage(numOrdersSalvage);
		
		statistics.setTotalCube(totalCube);
		statistics.setCubeOrdersGrocery(cubeOrdersGrocery);
		statistics.setCubeOrdersRefrigerate(cubeOrdersRefrigerate);
		statistics.setCubeOrdersFrozen(cubeOrdersFrozen);
		statistics.setCubeOrdersSalvage(cubeOrdersSalvage);
		
		statistics.setTotalWeight(totalWeight);
		statistics.setWeightOrdersGrocery(weightOrdersGrocery);
		statistics.setWeightOrdersRefrigerate(weightOrdersRefrigerate);
		statistics.setWeightOrdersFrozen(weightOrdersFrozen);
		statistics.setWeightOrdersSalvage(weightOrdersSalvage);
		
	}
	
	/**
	 * read the delivery orders from the order file
	 * @param orderInputFile
	 * @throws IOException 
	 */
	private void readDeliveryOrders(String orderInputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(orderInputFile));
		in.readLine();
		String line = in.readLine();
		
		while(line !=null && line.trim().length()>0){
			String entries[] = line.replace("\"", "").split(",");
			int index = Integer.parseInt(entries[0]);
			String routeGroup = entries[1];
			Store store = storeMap.get(entries[2]);
			Catlog catlog = Catlog.getDeliveryCatlog(entries[3]);
			double cube = Double.parseDouble(entries[4]);
			double weight = Double.parseDouble(entries[5]);
			
			if(store!=null){
				store.addDeliverOrder(index, store, catlog, cube, weight, routeGroup);
			}
			
			line = in.readLine();
		}
		
		in.close();
		
	}
	
	/**
	 * read the salvage Order from the file
	 * @throws IOException 
	 */
	private void readSalvageOrders(String salvageInputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(salvageInputFile));
		in.readLine();
		String line = in.readLine();
		
		while(line!=null){
			String entries[] = line.replace("\"", "").split(",");
			Store store = storeMap.get(entries[1]);
			Order order = new Order(-1, store, Catlog.salvage, 0, 0, batch);
			if(store!=null){
				store.addSalvageOrder(order);
			}
			
			line = in.readLine();
		}
		
		in.close();
		
	}
	
	/**
	 * read the time and distance matrix from the input data
	 * here we assume that the first entry is the warehouse
	 * @param inputFolder
	 * @throws IOException 
	 */
	private void readTimeDistance(String timeInputFile, String distanceInputFile) throws IOException{
		
		String distanceMatrixString[][] = readMatrix(distanceInputFile);
		String timeMatrixString[][] = readMatrix(timeInputFile);
		distanceMatrix = new HashMap<>();
		distanceMatrix.put(warehouse, new HashMap<Location, Double>());
		
		travelTimeMatrix = new HashMap<>();
		travelTimeMatrix.put(warehouse, new HashMap<Location, Double>());
		
		for(int i=2; i<distanceMatrixString.length; i++){
			
			Location locationOne = storeMap.get(distanceMatrixString[i][0].trim());
			if(!distanceMatrix.containsKey(locationOne)){
				distanceMatrix.put(locationOne, new HashMap<Location, Double>());
			}
			
			for(int j=2; j<distanceMatrixString.length; j++){
				Location locationTwo = storeMap.get(distanceMatrixString[0][j].trim());
				double distance = Double.parseDouble(distanceMatrixString[i][j].trim());
				distanceMatrix.get(locationOne).put(locationTwo, distance);
			}
			
			double distance = Double.parseDouble(distanceMatrixString[i][1].trim());
			distanceMatrix.get(locationOne).put(warehouse, distance);
			
			locationOne = storeMap.get(distanceMatrixString[0][i]);
			distance = Double.parseDouble(distanceMatrixString[1][i].trim());
			distanceMatrix.get(warehouse).put(locationOne, distance);
			
		}
		
		for(int i=2; i<timeMatrixString.length; i++){
			Location locationOne = storeMap.get(timeMatrixString[i][0]);
			
			if(!travelTimeMatrix.containsKey(locationOne)){
				travelTimeMatrix.put(locationOne, new HashMap<Location, Double>());
			}
			
			for(int j=2; j<timeMatrixString.length; j++){
				Location locationTwo = storeMap.get(timeMatrixString[0][j]);
				double time = Double.parseDouble(timeMatrixString[i][j].trim());
				travelTimeMatrix.get(locationOne).put(locationTwo, time);
			}
			
			double time = Double.parseDouble(timeMatrixString[i][1].trim());
			travelTimeMatrix.get(locationOne).put(warehouse, time);
			
			locationOne = storeMap.get(timeMatrixString[0][i]);
			time = Double.parseDouble(timeMatrixString[1][i].trim());
			
			travelTimeMatrix.get(warehouse).put(locationOne, time);
		}
	}
	
	/**
	 * read the corresponding pull time input file
	 * @param pullTimeFile
	 * @throws IOException 
	 */
	private void readPullTime(String pullTimeFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(pullTimeFile));
		String line = in.readLine();
		Date start = StringProcessor.getTime(line.split(",")[1].trim().replaceAll("\t", ""));
		line = in.readLine();
		Date end = StringProcessor.getTime(line.split(",")[1].trim().replaceAll("\t", ""));
		
		TimeProcessor.setPullTimeStart(start);
		warehouse.setPullTimeEnd(end);
		warehouse.setPullTimeStart(start);
		
		in.close();
	}
	
	/**
	 * read the store information
	 * @throws IOException 
	 */
	private void readStore(String storeInputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(storeInputFile));
		in.readLine();
		String line = in.readLine();
		stores = new ArrayList<>();
		storeMap = new HashMap<>();
		
		while(line!=null){
			String entries[] = line.replace("\"", "").split(",");
			
			if(entries.length==0){
				break;
			}
			
			int information = Integer.parseInt(entries[0]);
			String name = entries[1].trim();
			Date winStart = StringProcessor.getTime(entries[2]);
			Date winEnd = StringProcessor.getTime(entries[3]);
			Date early = StringProcessor.getTime(entries[4]);
			
			Store store = new Store(name, winStart, winEnd, early, information);
			
			if(TimeProcessor.getTimeInHour(winEnd)>=0 ){  //&& TimeProcessor.getTimeInHour(winStart)<= 11
				stores.add(store);
				storeMap.put(name, store);
			}
			
			line = in.readLine();
		}
		
		in.close();
	}
	
	/**
	 * clean the stores, remove those who does not have any orders
	 */
	private void cleanUnusedStore(){
		
		ArrayList<Store> tempStores = new ArrayList<Store>(stores);
		
		for(Store store : tempStores){
			if(store.getDeliveryOrders().size()==0 && store.getSalvageOrders().size()==0){
				stores.remove(store);
			}
		}
		
	}
	
	private void readStoreLocation() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("input/StoreLocations.csv"));
		String line = br.readLine();
		HashMap<String, Double> latitudeMap = new LinkedHashMap<String, Double>(); 
        HashMap<String, Double> longitudeMap = new LinkedHashMap<String, Double>();
        line = br.readLine();
        String items[] = line.split(",");
        double lat = Double.parseDouble(items[1].trim());
    	double lon = Double.parseDouble(items[2].trim());
    	warehouse.setLatitude(lat);
    	warehouse.setLongitude(lon);

    	while(line != null){
        	items = line.split(",");
        	String storeID = items[0].trim();
        	
        	lat = Double.parseDouble(items[1].trim());
        	lon = Double.parseDouble(items[2].trim());
        	latitudeMap.put(storeID, lat);
        	longitudeMap.put(storeID, lon);
        	line = br.readLine();
        }
        br.close();
        
        //match the store with the inforamtion
        for(Store store: stores){
        	String name = store.getName();//.split("-")[1].trim()
        	if(latitudeMap.containsKey(name)){
        		store.setLatitude(latitudeMap.get(name));
        		store.setLongitude(longitudeMap.get(name));
        	}
        	else{
        		System.out.println(store.getName());
        	}
        }
	}
	
	/**
	 * read the longitude and latitude of the store
	 * @throws IOException 
	 */
	@Deprecated
	private void readStoreLocationOld() throws IOException{
		cleanUnusedStore();
		//reading the information from file
		BufferedReader br = new BufferedReader(new FileReader("input/Kroger stores.csv"));
        String line = br.readLine();
        HashMap<String, Double> latitudeMap = new LinkedHashMap<String, Double>(); 
        HashMap<String, Double> longitudeMap = new LinkedHashMap<String, Double>();
        line = br.readLine();
        while(line != null){
        	String items[] = line.split(",");
        	String storeID = items[2].split("-")[0].trim();
        	String groupID = items[1].substring(1, 4);
//        	if(items[15].startsWith("16") || !latitudeMap.containsKey(storeID) ){
//        	}
        	
        	storeID = groupID + "-" + storeID; 
        	double lat = Double.parseDouble(items[11].trim());
        	double lon = Double.parseDouble(items[12].trim());
        	latitudeMap.put(storeID, lat);
        	longitudeMap.put(storeID, lon);
        	line = br.readLine();
        }
        br.close();
        
        //match the store with the inforamtion
        for(Store store: stores){
        	String name = store.getName();//.split("-")[1].trim()
        	if(latitudeMap.containsKey(name)){
        		store.setLatitude(latitudeMap.get(name));
        		store.setLongitude(longitudeMap.get(name));
        	}
        	else{
        		System.out.println(store.getName());
        	}
        }
	}
	
	/**
	 * read the matrix from the input data file
	 * @param inputFile
	 * @return
	 * @throws IOException 
	 */
	private String[][] readMatrix(String inputFile) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		ArrayList<String> lines = new ArrayList<>(300);
		String line = in.readLine();
		
		while(line!=null){
			lines.add(line.replace("\"", ""));
			line = in.readLine();
		}
		
		in.close();
		
		String entries[][] = new String[lines.size()][];
		for(int i=0; i<lines.size(); i++){
			entries[i] = lines.get(i).split(",");
		}
		
		return entries;
	}
	
	/**
	 * generating the network using the order information
	 * @return
	 */
	public Network generateNetwork(){
		SourceNode source = new SourceNode(warehouse);
		SinkNode sink = new SinkNode(warehouse);
		
		ArrayList<OrderNode> orderNodes = new ArrayList<>(300);
		ArrayList<Arc> arcs = new ArrayList<>(500);
		
		for(Store store : stores){
			ArrayList<OrderNode> curStoreNodes = new ArrayList<>();
			
			//create delivery order nodes
			for(Order order : store.getDeliveryOrders()){
				OrderNode node = new OrderNode(order);
				orderNodes.add(node);
				curStoreNodes.add(node);
			}
			
			//create salvage order nodes
			for(Order order : store.getSalvageOrders()){
				OrderNode node = new OrderNode(order);
				orderNodes.add(node);
				curStoreNodes.add(node);
			}
			
		}
		
		for(OrderNode fromNode : orderNodes){
			//from and to the warehouse 
			arcs.add(Arc.getArc(source, fromNode, this));
			arcs.add(Arc.getArc(fromNode, sink, this));
			for(OrderNode toNode: orderNodes){
				Arc arc = Arc.getArc(fromNode, toNode, this);
				
				//only when the arc has enough time to go from one to the other would we permit the arc to be applied 
				if(arc!=null && 
						( fromNode.getOrder().getStore()==toNode.getOrder().getStore() 
							|| fromNode.getStartTime() + arc.getTotalTime() <= toNode.getEndTime())){
					arcs.add(arc);
				}
			}
		}
		
		return new Network(source, sink, orderNodes, arcs);
		
	}
	
	/**
	 * get the closest store to the current store
	 * @param store
	 * @return
	 */
	private ArrayList<Store> getClosetStores(Store store){
		ArrayList<Store> closetStore = new ArrayList<>();
		for(Store str : stores){
			int i = 0;
			while (i<closetStore.size() && getTravelTimeHour(closetStore.get(i), store) > getTravelTimeHour(str, store)){
				i++;
			}
			
			closetStore.add(i, str);
		}
		
		return closetStore;
	}

	/**
	 * @return the numTrucks
	 */
	public int getNumTrucks() {
		return numTrucks;
	}

	/**
	 * @param numTrucks the numTrucks to set
	 */
	public void setNumTrucks(int numTrucks) {
		this.numTrucks = numTrucks;
	}
	
	/**
	 * get the latest departure time from the warehouse
	 * @return
	 */
	public double getLatestDepartTime(){
		return TimeProcessor.getTimeInHour(warehouse.getPullTimeEnd());
	}

	/**
	 * get the latest return time to the warehouse
	 * @return
	 */
	public double getLatestReturnTime(){
		return TimeProcessor.getTimeInHour(warehouse.getPullTimeEnd()) + Parameter.getTotalTimeLimitPerRouteHour();
	}

	/**
	 * @return the warehouse
	 */
	public Warehouse getWarehouse() {
		return warehouse;
	}

	/**
	 * @param warehouse the warehouse to set
	 */
	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}

	/**
	 * @return the stores
	 */
	public ArrayList<Store> getStores() {
		return stores;
	}

	/**
	 * @param stores the stores to set
	 */
	public void setStores(ArrayList<Store> stores) {
		this.stores = stores;
	}

	/**
	 * @return the dataName
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * @param dataName the dataName to set
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	
}

