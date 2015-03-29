package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.*;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.output.TimeProcessor;

public class OrderOrderArc extends Arc {
	
	double pickupTimeSalvage = 0.1;
	
	OrderNode fromNode;
	OrderNode toNode;
	
	public OrderOrderArc(OrderNode fromNode, OrderNode toNode, InstanceCVRPTW instance) {
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		
		double stopCost = fromNode.getOrder().getStore()==toNode.getOrder().getStore() ? 0 : Parameter.getPerStopCost();
		cost = stopCost + instance.getDistance(fromNode.getOrder().getStore(), toNode.getOrder().getStore())*Parameter.getPerMileCost();
		
		travelTime = instance.getTravelTimeHour(fromNode.getOrder().getStore(), toNode.getOrder().getStore());
		double setUpTimeForLoading =  (fromNode.getOrder().getStore()!=toNode.getOrder().getStore()) ? Parameter.getLoadSetUpTime() : 0; 
		totalTime = fromNode.getOrder().getCube() /Parameter.getLoadRate() + travelTime + setUpTimeForLoading;
		
		if(Catlog.salvage.equals(fromNode.getOrder().getCatlog())){
			totalTime += pickupTimeSalvage;
		}
	}

	@Override
	public Node getFromNode() {
		return fromNode;
	}

	@Override
	public Node getToNode() {
		return toNode;
	}

	@Override
	public double getBigM() {
		double candidate = TimeProcessor.getDifferenceHour(fromNode.getOrder().getStore().getEndTime(), toNode.getOrder().getStore().getEarlyTime()) 
				+getTotalTime();
		
		return Math.max(0, candidate);
	}
	
	@Override
	public double getBigLIdleTime() {
		double candidate = TimeProcessor.getDifferenceHour(toNode.getOrder().getStore().getEndTime(), fromNode.getOrder().getStore().getEarlyTime()) 
				- getTotalTime();
		
		return Math.max(0, candidate);
	}

	@Override
	public boolean isCatlogSeparation() {
		return Catlog.needSeparation(fromNode.getOrder().getCatlog(), toNode.getOrder().getCatlog());
	}

	@Override
	public boolean isStoreSeparation() {
		
		if(Catlog.salvage.equals(fromNode.getOrder().getCatlog() ) || Catlog.salvage.equals(toNode.getOrder().getCatlog())){
			return false;
		}
		
		return fromNode.getOrder().getStore()!=toNode.getOrder().getStore();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + fromNode + "." + toNode
				+ ")";
	}
	
	public double getTotalSeparationVolume(){
		double totalSeparation = 0;
		if(isCatlogSeparation()){
			totalSeparation += Parameter.getVolumeReductionOrders();
		}
		
		if(isStoreSeparation()){
			totalSeparation += Parameter.getVolumeReductionStores();
		}
		
		return totalSeparation;
	}

	@Override
	/**
	 * it is always within the time window if the arc is between ordernode for the same store
	 */
	public boolean isOutsideEndNodeTW(double time) {
		
		if(fromNode.getOrder().getStore()==toNode.getOrder().getStore()){
			return false;
		}
		else{
			return time > getToNode().getEndTime();
		}
	}

	@Override
	public boolean isWithinSameStore() {
		return fromNode.getOrder().getStore()==toNode.getOrder().getStore();
	}
	
	
}
