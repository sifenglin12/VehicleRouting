package edu.utexas.orie.cvrptw.network;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import edu.utexas.orie.cvrptw.instance.Order;

public class Network {
	SourceNode source;
	SinkNode sink;
	ArrayList<OrderNode> orderNodes;
	ArrayList<Arc> arcs;
	
	/**
	 * a map taht maps order to its order node
	 */
	LinkedHashMap<Order, OrderNode> orderToNodeMap;  
	
	public Network(SourceNode source, SinkNode sink,
			ArrayList<OrderNode> orderNodes, ArrayList<Arc> arcs) {
		super();
		this.source = source;
		this.sink = sink;
		this.orderNodes = orderNodes;
		this.arcs = arcs;
		
		for(Arc arc : arcs){
			arc.getFromNode().addPostArc(arc);
			arc.getFromNode().addPostNodeArc(arc.getToNode(), arc);
			arc.getToNode().addPreArc(arc);
			arc.getToNode().addPreNodeArc(arc.getFromNode(), arc);
		}
		
		orderToNodeMap = new LinkedHashMap<Order, OrderNode>();
		for(OrderNode node : orderNodes){
			orderToNodeMap.put(node.getOrder(), node);
		}
	}
	/**
	 * @return the source
	 */
	public SourceNode getSource() {
		return source;
	}
	/**
	 * @return the sink
	 */
	public SinkNode getSink() {
		return sink;
	}
	/**
	 * @return the orderNodes
	 */
	public ArrayList<OrderNode> getOrderNodes() {
		return orderNodes;
	}
	/**
	 * @return the arcs
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(SourceNode source) {
		this.source = source;
	}
	/**
	 * @param sink the sink to set
	 */
	public void setSink(SinkNode sink) {
		this.sink = sink;
	}
	/**
	 * @param orderNodes the orderNodes to set
	 */
	public void setOrderNodes(ArrayList<OrderNode> orderNodes) {
		this.orderNodes = orderNodes;
	}
	/**
	 * @param arcs the arcs to set
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
	}
	
	/**
	 * get the order node for a spcific order
	 * @param order
	 * @return
	 */
	public OrderNode getOrderNodeFromOrder(Order order){
		return orderToNodeMap.get(order);
	}

}
