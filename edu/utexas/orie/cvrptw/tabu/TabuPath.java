package edu.utexas.orie.cvrptw.tabu;

import java.util.ArrayList;

import edu.utexas.orie.cvrptw.network.*;

public class TabuPath extends TabuCycle {
	
	Arc arc; //arc that the node is inserted
	
	public TabuPath(ArrayList<OrderNode> nodes, Arc arc) {
		super(nodes);
		this.arc = arc;
	}
	/**
	 * @return the nodes
	 */
	public ArrayList<OrderNode> getNodes() {
		return nodes;
	}
	/**
	 * @return the arc
	 */
	public Arc getArc() {
		return arc;
	}
	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(ArrayList<OrderNode> nodes) {
		this.nodes = nodes;
	}
	/**
	 * @param arc the arc to set
	 */
	public void setArc(Arc arc) {
		this.arc = arc;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((arc == null) ? 0 : arc.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TabuPath other = (TabuPath) obj;
		if (arc == null) {
			if (other.arc != null)
				return false;
		} else if (!arc.equals(other.arc))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
	
}
