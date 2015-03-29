package edu.utexas.orie.cvrptw.network;

import edu.utexas.orie.cvrptw.instance.InstanceCVRPTW;
import edu.utexas.orie.cvrptw.instance.Parameter;
import edu.utexas.orie.cvrptw.output.TimeProcessor;

public class SourceOrderArc extends Arc {
	
	SourceNode fromNode;
	OrderNode toNode;
	
	public SourceOrderArc(SourceNode fromNode, OrderNode toNode, InstanceCVRPTW instance) {
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		
		cost = instance.getDistance(fromNode.getWarehouse(), toNode.getOrder().getStore())*Parameter.getPerMileCost() + Parameter.getPerStopCost();
		travelTime = instance.getTravelTimeHour(fromNode.getWarehouse(), toNode.getOrder().getStore());
		totalTime = Parameter.getLoadSetUpTime() + travelTime;
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
		return "(Source." + toNode + ")";
	}

	@Override
	public double getTotalSeparationVolume() {
		return 0;
	}

	@Override
	public boolean isOutsideEndNodeTW(double time) {
		return time > getToNode().getEndTime();
	}

	@Override
	public boolean isWithinSameStore() {
		return false;
	}
	
	@Override
	public double getBigM() {
		double candidate = TimeProcessor.getDifferenceHour(fromNode.getWarehouse().getPullTimeEnd(), toNode.getOrder().getStore().getEarlyTime()) 
				+getTotalTime();
		return Math.max(0., candidate);
	}
	
	@Override
	public double getBigLIdleTime() {
		double candidate = TimeProcessor.getDifferenceHour(toNode.getOrder().getStore().getEndTime(), fromNode.getWarehouse().getPullTimeStart()) 
				-getTotalTime();
		return Math.max(0., candidate);
	}

}
