package edu.utexas.orie.cvrptw.network;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import edu.utexas.orie.cvrptw.output.StringProcessor;

public abstract class Node {
	
	LinkedHashSet<Arc> preArcs;
	LinkedHashSet<Arc> postArcs;

	LinkedHashMap<Node, Arc> preNodes;
	LinkedHashMap<Node, Arc> postNodes;
	
	double timeWindowStart;
	double timeWindowEnd;
	
	public Node() {
		super();
		this.preArcs = new LinkedHashSet<>();
		this.postArcs = new LinkedHashSet<>();
		preNodes = new LinkedHashMap<Node, Arc>();
		postNodes = new LinkedHashMap<Node, Arc>();
	}
	
	/**
	 * add a node out of <node>
	 */
	public void addPostArc(Arc arc){
		postArcs.add(arc);
	}
	
	/**
	 * add a node into <node>
	 */
	public void addPreArc(Arc arc){
		preArcs.add(arc);
	}

	/**
	 * add a node out of <node>
	 */
	public void addPostNodeArc(Node node, Arc arc){
		postNodes.put(node, arc);
	}

	/**
	 * add a node into <node>
	 */
	public void addPreNodeArc(Node node, Arc arc){
		preNodes.put(node, arc);
	}

	/**
	 * @return the preArcs
	 */
	public LinkedHashSet<Arc> getPreArcs() {
		return preArcs;
	}

	/**
	 * @return the postArcs
	 */
	public LinkedHashSet<Arc> getPostArcs() {
		return postArcs;
	}

	/**
	 * @param preArcs the preArcs to set
	 */
	public void setPreArcs(LinkedHashSet<Arc> preArcs) {
		this.preArcs = preArcs;
	}

	/**
	 * @param postArcs the postArcs to set
	 */
	public void setPostArcs(LinkedHashSet<Arc> postArcs) {
		this.postArcs = postArcs;
	}
	
	/**
	 * check if a node is a delivery node
	 * @return
	 */
	public abstract boolean isDeliveryNode();

	/**
	 * check if a node is a delivery node
	 * @return
	 */
	public abstract boolean isPickUpNode();
	
	/**
	 * get the weight associated with the node
	 * @return
	 */
	public abstract double getWeight();
	
	/**
	 * get the volume associated with the nodes
	 * @return
	 */
	public abstract double getCube();
	
	
	public double getStartTime() {
		return timeWindowStart;
	}

	public double getEndTime() {
		return timeWindowEnd;
	}
	
	/**
	 * check if <node> is a eligible successor of this
	 * @return
	 */
	public abstract boolean isElibileSuccessorOf(Node node);

	/**
	 * check if <node> is a eligible predecessor of this
	 * @return
	 */
	public abstract boolean isElibilePredecessorOf(Node node);
	
	/**
	 * check if <this> is a precedence of <node>
	 * @param node
	 * @return
	 */
	public boolean isPredecessorOf(Node node){
		return node.preNodes.containsKey(this);
	}

	/**
	 * check if <this> is a precedence of <node>
	 * @param node
	 * @return
	 */
	public boolean isSuccessorOf(Node node){
		return node.postNodes.containsKey(this);
	}
	
	/**
	 * get the previous arc, given the from node, that is get the arc (<node>, <this>)
	 * @param node
	 * @return
	 */
	public Arc getPreArc(Node node){
		return preNodes.get(node);
	}

	/**
	 * get the postArc, given the from node, that is get the arc (<this>,<node>)
	 * @param node
	 * @return
	 */
	public Arc getPostArc(Node node){
		return postNodes.get(node);
	}
	
	/**
	 * boolean check if the entering time is eligible
	 * @return
	 */
	public boolean isEnterTimeEligible(double time){
		return time<=timeWindowEnd && time>=timeWindowStart;
	}
	
	/**
	 * get the string for time window
	 * @return
	 */
	public String getTWString(){
		return toString()+"["+ StringProcessor.getString(timeWindowStart, 5) + "," + StringProcessor.getString(timeWindowEnd, 5) + "]";
	}
}
