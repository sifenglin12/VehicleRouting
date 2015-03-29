package edu.utexas.orie.cvrptw.grasp;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class BinPacking {
	
	/**
	 * get the lower bound on the number of bins
	 * @return
	 */
	public static int getLowerBound(Collection<Double> items, double binSize){
		int bestBound = (int) Math.ceil(1D*getSum(items)/binSize);
		double maxK = 1D*binSize/2;
		
		LinkedHashSet<Double> kValues = new LinkedHashSet<Double>();
		for(double v : items){
			if(v<=maxK){
				kValues.add(v);
			}
		}
		
		for(double k : kValues){
			LinkedList<Double> NOne = new LinkedList<Double>();
			LinkedList<Double> NTwo = new LinkedList<Double>();
			LinkedList<Double> NThree = new LinkedList<Double>();
			
			for(double v : items){
				if(v > binSize - k){
					NOne.add(v);
				}
				else if(v > maxK){
					NTwo.add(v);
				}
				else if(v >= k){
					NThree.add(v);
				}
			}
			
			double fractional = getSum(NThree) - NTwo.size()*binSize + getSum(NTwo);
			fractional = 1D*fractional/binSize;
			
			int ceilFractional = (int) Math.ceil(fractional);
			
			int curBound = NOne.size() + NTwo.size() + Math.max(0, ceilFractional);
			bestBound = Math.max(curBound, bestBound);
		}
		
		return bestBound;
	}
	
	/**
	 * 
	 * @param values
	 * @return
	 */
	protected static int getSum(Collection<Double> values){
		int total = 0;
		for(double v : values){
			total += v;
		}
		
		return total;
	}
	
}
