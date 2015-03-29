package edu.utexas.orie.cvrptw.test;

import java.util.ArrayList;

import edu.utexas.orie.cvrptw.instance.Catlog;

public class Trial {

	public static void main(String[] args) {
		System.out.println(Catlog.grocery.getValue() > Catlog.frozen.getValue());
		
		String s = "s   d   f";
		for(String result : s.split("[ ]+")){
			System.out.println("["+result+"]");
		}
	}

}
