package de.tum.bio.utils;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

public class Correlator {
	
	public Correlator() {
		// empty
	}
	
	public double correlateOverlapPearson(double[] xArray, double[] yArray) {
		PearsonsCorrelation cor = new PearsonsCorrelation();
		return cor.correlation(xArray, yArray);
	}
	
	public double correlateOverlapSpearman(double[] xArray, double[] yArray) {
		SpearmansCorrelation cor = new SpearmansCorrelation();
		return cor.correlation(xArray, yArray);
	}
	
}