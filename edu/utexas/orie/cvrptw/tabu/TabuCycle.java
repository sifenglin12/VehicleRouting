package edu.utexas.orie.cvrptw.tabu;

import java.util.ArrayList;

import edu.utexas.orie.cvrptw.network.*;

public class TabuCycle {
	
	ArrayList<OrderNode> nodes;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TabuCycle other = (TabuCycle) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	public TabuCycle(ArrayList<OrderNode> nodes) {
		super();
		this.nodes = nodes;
	}

	/**
	 * @return the nodes
	 */
	public ArrayList<OrderNode> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(ArrayList<OrderNode> nodes) {
		this.nodes = nodes;
	}
	
	
}
