package de.tum.bio.proteomics;

import java.util.Map;

import de.tum.bio.analysis.AnalysisComponent;

/**
 * This interface represents a statistics file.
 * @author Matthias Stahl
 *
 */

public class StatisticsFile implements AnalysisComponent {
	
	private int id;
	private String name;
	private Map<String, Map<StatisticsTableHeaders, Double>> enrichmentsAndPValues;
	
	public StatisticsFile(int id, String name) {
		this(id, null, name);
	}
	
	public StatisticsFile(int id, Map<String, Map<StatisticsTableHeaders, Double>> enrichmentsAndPValues, String name) {
		this.id = id;
		this.enrichmentsAndPValues = enrichmentsAndPValues;
		this.name = "Statistics: " + name;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setEnrichmentsAndPValues (Map<String, Map<StatisticsTableHeaders, Double>> enrichmentsAndPValues) {
		this.enrichmentsAndPValues = enrichmentsAndPValues;
	}
	
	public Map<String, Map<StatisticsTableHeaders, Double>> getEnrichmentsAndPValues() {
		return enrichmentsAndPValues;
	}
	
	public Map<StatisticsTableHeaders, Double> getEnrichmentAndPvalueByName(String name) {
		return enrichmentsAndPValues.get(name);
	}
	
	public double getEnrichmentByName(String name) {
		double result = Double.NaN;
		if (enrichmentsAndPValues.containsKey(name)) {
			result = enrichmentsAndPValues.get(name).get(StatisticsTableHeaders.LOG2_ENRICHMENT);
		}
		return result;
	}
	
	public double getPValueByName(String name) {
		double result = Double.NaN;
		if (enrichmentsAndPValues.containsKey(name)) {
			result = enrichmentsAndPValues.get(name).get(StatisticsTableHeaders.MINUS_LOG10_PVALUE);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
