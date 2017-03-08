package de.tum.bio.utils;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

public class Correlator extends PearsonsCorrelation {
	
	public Correlator() {
		// empty
	}
	
	public double correlateOverlap(double[] xArray, double[] yArray) {
		
		
		return super.correlation(xArray, yArray);
	}
	
}