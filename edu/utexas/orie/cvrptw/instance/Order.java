package edu.utexas.orie.cvrptw.instance;

public class Order {
	
	protected int index;
	protected Store store;
	protected Catlog catlog;
	protected double cube;	
	protected double weight;
	protected String routeGroup;
	
	public Order(int index, Store store, Catlog catlog, double cube, double weight,
			String routeGroup) {
		super();
		this.store = store;
		this.catlog = catlog;
		this.cube = cube;
		this.weight = weight;
		this.routeGroup = routeGroup;
	}

	/**
	 * @return the store
	 */
	public Store getStore() {
		return store;
	}
	
	/**
	 * @return the catlog
	 */
	public Catlog getCatlog() {
		return catlog;
	}
	/**
	 * @return the cube
	 */
	public double getCube() {
		return cube;
	}
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * @return the routeGroup
	 */
	public String getRouteGroup() {
		return routeGroup;
	}
	/**
	 * @param store the store to set
	 */
	public void setStore(Store store) {
		this.store = store;
	}
	/**
	 * @param catlog the catlog to set
	 */
	public void setCatlog(Catlog catlog) {
		this.catlog = catlog;
	}
	/**
	 * @param cube the cube to set
	 */
	public void setCube(double cube) {
		this.cube = cube;
	}
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	/**
	 * @param routeGroup the routeGroup to set
	 */
	public void setRouteGroup(String routeGroup) {
		this.routeGroup = routeGroup;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String cat = "-";
		if(Catlog.grocery.equals(catlog)){
			cat += "GRO";
		}
		else if(Catlog.refrigerated.equals(catlog)){
			cat += "REF";
		}
		else if(Catlog.frozen.equals(catlog)){
			cat += "FRO";
		}
		else{
			cat += "SAL";
		}
		
		return store.getName() + cat;
	}		
	
}
