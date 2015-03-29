package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.instance.Parameter;

public class OrderSinkArc extends Arc {
	OrderNode fromNode;
	SinkNode toNode;
	
	public OrderSinkArc(OrderNode fromNode, SinkNode toNode, InstanceCVRPTW instance) {
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		
		cost = instance.getDistance(fromNode.getOrder().getStore(), toNode.getWarehouse())*Parameter.getPerMileCost() ;
		travelTime = instance.getTravelTimeHour(fromNode.getOrder().getStore(), toNode.getWarehouse());
		totalTime = travelTime + fromNode.getOrder().getCube()/Parameter.getLoadRate() ; 
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
		return getTotalTime();
	}

	@Override
	public boolean isCatlogSeparation() {
		return false;
	}

	@Override
	public boolean isStoreSeparation() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + fromNode + ".Sink)";
	}

	@Override
	public double getTotalSeparationVolume() {
		return 0;
	}

	@Override
	public boolean isOutsideEndNodeTW(double time) {
		return false;
	}

	@Override
	public boolean isWithinSameStore() {
		return false;
	}

	@Override
	public double getBigLIdleTime() {
		return 0;
	}
	
}
