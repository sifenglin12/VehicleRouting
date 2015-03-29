package edu.utexas.orie.cvrptw.output;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import edu.utexas.orie.cvrptw.grasp.Route;
import edu.utexas.orie.cvrptw.instance.*;
import edu.utexas.orie.cvrptw.network.OrderNode;

public class Diagram {
	static final int WIDTH = 6000;
	static final int HEIGHT = 6000;
	static final int LEFT_MOST = 50;
	static final int RIGHT_MOST = WIDTH - 50;
	static final int DOWN_MOST = HEIGHT - 50;
	static final int UP_MOST = 50;
	static final int STORE_SIZE = 5;
	static final int FONT_SIZE = 20;
	
	private static double maxLat, minLat, maxLon, minLon;
	
	public static void plotSolution(String outputFile, Collection<Store> stores, Collection<Route> routes, Warehouse warehouse) throws IOException{
		BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		//draw the axis
		
		maxLat = getMaxLat(stores);
		minLat = getMinLat(stores);

		maxLon = getMaxLon(stores);
		minLon = getMinLon(stores);

		plotStore(stores, ig2, warehouse);
		
		plotRoutes(routes, ig2);
		
		ImageIO.write(bi, "PNG", new File(outputFile + ".PNG"));
	}
	
	public static void plotStore(String outputFile, Collection<Store> stores, Warehouse warehouse) throws IOException{
		BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		//draw the axis
		
		maxLat = getMaxLat(stores);
		minLat = getMinLat(stores);

		maxLon = getMaxLon(stores);
		minLon = getMinLon(stores);

		plotStore(stores, ig2, warehouse);
		
		ImageIO.write(bi, "PNG", new File(outputFile + ".PNG"));
	}
	
	private static void plotStore(Collection<Store> stores, Graphics2D ig2, Warehouse warehouse){
		ig2.setFont(new Font("TimesRoman", Font.PLAIN, FONT_SIZE));
		for(Store store : stores){
			double lat = store.getLatitude();
			double lon = store.getLongitude();
			if(lat!=0 && lon!=0){
				int x = getX(lat);
				int y = getY(lon);
				ig2.setColor(Color.blue);
				ig2.drawRect(x, y, STORE_SIZE, STORE_SIZE);
				ig2.setColor(Color.BLACK);
				ig2.drawString(store.getName(), x-10+2, y+STORE_SIZE+2);
			}
			
		}
		
		ig2.setColor(Color.red);
		ig2.fillRect(getX(warehouse.getLatitude()), getY(warehouse.getLongitude()), STORE_SIZE*4, STORE_SIZE*4);
		ig2.setColor(Color.BLACK);
	}
	
	
	private static void plotRoutes(Collection<Route> routes, Graphics2D ig2){
		for(Route route: routes){
			ArrayList<OrderNode> nodes = route.getOrderNodes();
			for(int i=0; i<nodes.size() - 1; i++){
				Store storeOne = nodes.get(i).getOrder().getStore();
				Store storeTwo = nodes.get(i+1).getOrder().getStore();
				
				double latOne = storeOne.getLatitude();
				double latTwo = storeTwo.getLatitude();
				double lonOne = storeOne.getLongitude();
				double lonTwo = storeTwo.getLongitude();
				
				if(latOne==0 || latTwo==0 || lonOne==0 || lonTwo==0){
					continue;
				}
				
				int xOne = getX(latOne);
				int xTwo = getX(latTwo);
				int yOne = getY(lonOne);
				int yTwo = getY(lonTwo);
				
				ig2.drawLine(xOne, yOne, xTwo, yTwo);
			}
		}
	}
	
	/**
	 * get the x axis value, using the latitude values
	 * @return
	 */
	private static int getY(double lon){
		return DOWN_MOST - (int) ((lon - minLon)/(maxLon - minLon) * (DOWN_MOST - UP_MOST));
	}
	/**
	 * get the x axis value, using the latitude values
	 * @return
	 */
	private static int getX(double lat){
		return LEFT_MOST + (int) ((lat - minLat)/(maxLat - minLat) * (RIGHT_MOST - LEFT_MOST));
	}
	
	/**
	 * get the maximal longitude of all stores in the instance
	 * @param instance
	 * @return
	 */
	private static double getMaxLon(Collection<Store> stores){
		double maxLon = -10000000;
		for(Store store: stores){
			if(store.getLongitude()!=0){
				maxLon = Math.max(store.getLongitude(), maxLon);
			}
		}
		
		return maxLon;
	}
	
	/**
	 * get the minimal latitude of all stores in the instance
	 * @param instance
	 * @return
	 */
	private static double getMinLon(Collection<Store> stores){
		double minLon = Double.MAX_VALUE;
		for(Store store: stores){
			if(store.getLongitude()!=0){
				minLon = Math.min(store.getLongitude(), minLon);
			}
		}
		
		return minLon;
	}
	/**
	 * get the maximal latitude of all stores in the instance
	 * @param instance
	 * @return
	 */
	private static double getMaxLat(Collection<Store> stores){
		double maxLat = -1000000;
		for(Store store: stores){
			maxLat = Math.max(store.getLatitude(), maxLat);
		}
		
		return maxLat;
	}
	
	/**
	 * get the minimal latitude of all stores in the instance
	 * @param instance
	 * @return
	 */
	private static double getMinLat(Collection<Store> stores){
		double minLat = Double.MAX_VALUE;
		for(Store store: stores){
			if(store.getLatitude()!=0){
				minLat = Math.min(store.getLatitude(), minLat);
			}
		}
		
		return minLat;
	}
	
}
