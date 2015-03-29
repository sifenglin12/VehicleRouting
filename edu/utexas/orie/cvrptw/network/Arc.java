package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.Catlog;
import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;

public abstract class Arc {
	
	double cost;
	double travelTime;
	double totalTime;
	
	public abstract Node getFromNode();
	public abstract Node getToNode();
	
	/**
	 * give two order node, and return the corresponding arc; if the arc does not exist, return null
	 * @return
	 */
	public static OrderOrderArc getArc(OrderNode fromNode, OrderNode toNode, InstanceCVRPTW instance){
		
		if(fromNode==toNode){
			return null;
		}
		
		if( Catlog.grocery.equals(fromNode.getOrder().getCatlog())
				|| Catlog.salvage.equals(toNode.getOrder().getCatlog())
				|| Catlog.refrigerated.equals(fromNode.getOrder().getCatlog()) && !Catlog.grocery.equals(toNode.getOrder().getCatlog())
				|| Catlog.frozen.equals(fromNode.getOrder().getCatlog()) && Catlog.frozen.equals(toNode.getOrder().getCatlog()) 
				){
			return new OrderOrderArc(fromNode, toNode, instance);
		}
		
		return null;
	}
	
	/**
	 * given the sourceNode and the order node, create source arc
	 * @param warehouse
	 * @param toNode
	 * @return
	 */
	public static SourceOrderArc getArc(SourceNode fromNode, OrderNode toNode, InstanceCVRPTW instance){
		return new SourceOrderArc(fromNode, toNode, instance);
	}

	/**
	 * given the order node and the sink node, create sink arc
	 * @param warehouse
	 * @param toNode
	 * @return
	 */
	public static OrderSinkArc getArc(OrderNode fromNode, SinkNode toNode, InstanceCVRPTW instance){
		return new OrderSinkArc(fromNode, toNode, instance);
	}
	
	/**
	 * get the cost associated with the arc
	 * @return
	 */
	public double getCost(){
		return cost;
	}
	
	/**
	 * get the travel time associated with the arc
	 * @return
	 */
	public double getTravelTime(){
		return travelTime;
	}
	
	/**
	 * get the total time associated with the arc
	 * @return
	 */
	public double getTotalTime(){
		return totalTime;
	}
	
	/**
	 * get the Big M associated with the arc
	 * @return
	 */
	public abstract double getBigM();
	
	/**
	 * get the big L value for constraint 1r and 1s
	 * @return
	 */
	public abstract double getBigLIdleTime();
	
	/**
	 * check if the order separation 
	 * @return
	 */
	public abstract boolean isCatlogSeparation();

	/**
	 * check if the store separation 
	 * @return
	 */
	public abstract boolean isStoreSeparation();
	
	/**
	 * get the volume required for separation
	 * @return
	 */
	public abstract double getTotalSeparationVolume();
	
	/**
	 * check if the time is within the time window of the 
	 * @param time
	 * @return
	 */
	public abstract boolean isOutsideEndNodeTW(double time);
	
	/**
	 * check if the arc moves within the same store, this is important when deciding the TW
	 * @return
	 */
	public abstract boolean isWithinSameStore();
}
