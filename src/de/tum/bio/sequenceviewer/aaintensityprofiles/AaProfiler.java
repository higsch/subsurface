package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.Toolbox;
import de.tum.bio.utils.Correlator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class AaProfiler {
	
	private ProteinGroup protein;
	private Map<Integer, Map<String, Long>> profileMap;
	private Map<String, Map<String, Long>> reducedProfileMap = new HashMap<>();
	
	private Map<String, XYChart.Series<String, Double>> seriesMap;
	private Map<String, XYChart.Series<String, Double>> normalizedSeriesMap;
	private Map<String, List<Double>> doubleProfileMap;
	private Map<String, Map<String, Double>> correlationMap;
	
	public AaProfiler() {
		// empty
	}
	
	public AaProfiler(ProteinGroup protein) {
		this.protein = protein;
	}
	
	public String getSequence() {
		return protein.getSequenceAsString();
	}
	
	public String getProteinIds() {
		return protein.getDatabaseIds();
	}
	
	public void addProfileMap(Map<Integer, Map<String, Long>> profileMap) {
		this.profileMap = profileMap;
		reduceProfileMap();
		seriesMap = generateSeriesMap(false);
		normalizedSeriesMap = generateSeriesMap(true);
		doubleProfileMap = getDoubleProfileMap();
		correlationMap = getCorrelationMap();
	}
	
	public Map<Integer, Map<String, Long>> getProfileMap() {
		return profileMap;
	}
	
	public void removeProfile(int position) {
		profileMap.remove(position);
	}
	
	private void reduceProfileMap() {
		List<Integer> positionList = new ArrayList<>();
		for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
			positionList.add(position);
			if (profileMap.containsKey(position - 1)) {
				if (profileMap.get(position).toString().equals(profileMap.get(position - 1).toString())) {
					continue;
				}
			}
			reducedProfileMap.put(positionList.toString(), profileMap.get(position));
			positionList.clear();
		}
	}
	
	private Map<String, XYChart.Series<String, Double>> generateSeriesMap(boolean normalize) {
		Map<String, XYChart.Series<String, Double>> map = new HashMap<>();

		long min = 0;
		long max = 0;

		for (String position : reducedProfileMap.keySet()) {
			XYChart.Series<String, Double> series = new XYChart.Series<>();
			// Calculate min/max in case of normalization
			if (normalize) {
				min = Toolbox.getMinFromMapValues(reducedProfileMap.get(position));
				max = Toolbox.getMaxFromMapValues(reducedProfileMap.get(position));
			}
			
			for (Entry<String, Long> entry : reducedProfileMap.get(position).entrySet()) {
				if (entry.getValue() != null) {
					XYChart.Data<String, Double> dataPoint = null;
					if (normalize) {
						dataPoint = new XYChart.Data<String, Double>(entry.getKey(), Toolbox.normalize(entry.getValue(), min, max));
					} else {
						dataPoint = new XYChart.Data<String, Double>(entry.getKey(), Double.valueOf(Toolbox.log(entry.getValue(), 2)));
					}
					
					if (dataPoint != null) {
						Label label = new Label("");
						label.setPrefSize(12.0, 12.0);
						Tooltip.install(label, new Tooltip(position));
						dataPoint.setNode(label);
						series.getData().add(dataPoint);
					}
				}
			}
			series.setName(position);
			map.put(position, series);
		}

		return map;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeries() {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		seriesList.addAll(seriesMap.values());
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllNormalizedXYChartSeries() {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		seriesList.addAll(normalizedSeriesMap.values());
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		List<String> addedSeries = new ArrayList<>();
		for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
			if (addedSeries.contains(getListKey(position + offset, seriesMap.keySet()))) {
				continue;
			}
			if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
				if (seriesMap.containsKey(getListKey(position + offset, seriesMap.keySet()))) {
					seriesList.add(seriesMap.get(getListKey(position + offset, seriesMap.keySet())));
					addedSeries.add(getListKey(position + offset, seriesMap.keySet()));
				}
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	private String getListKey(int position, Set<String> keyList) {
		for (String key : keyList) {
			if ((key.contains(" " + String.valueOf(position) + ", ")) || (key.contains("[" + String.valueOf(position) + ", ")) || (key.contains(" " + String.valueOf(position) + "]"))) {
				return key;
			}
		}
		return null;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllNormalizedXYChartSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		List<String> addedSeries = new ArrayList<>();
		for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
			if (addedSeries.contains(getListKey(position + offset, seriesMap.keySet()))) {
				continue;
			}
			if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
				if (normalizedSeriesMap.containsKey(getListKey(position + offset, seriesMap.keySet()))) {
					seriesList.add(normalizedSeriesMap.get(getListKey(position + offset, seriesMap.keySet())));
					addedSeries.add(getListKey(position + offset, seriesMap.keySet()));
				}
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	private Map<String, List<Double>> getDoubleProfileMap() {
		Map<String, List<Double>> map = new HashMap<>();
		
		// Create map with double profile values
		for (Entry<String, Map<String, Long>> entry : reducedProfileMap.entrySet()) {
			for (Entry<String, Long> dataPoint : entry.getValue().entrySet()) {
				if (!map.containsKey(entry.getKey())) {
					map.put(entry.getKey(), new ArrayList<Double>());
				}
				double intensity;
				if (dataPoint.getValue() == null) {
					intensity = 0d; //Double.NaN;
				} else {
					intensity = (double) dataPoint.getValue();
				}
				map.get(entry.getKey()).add(intensity);
			}	
		}
		
		return map;
	}
	
	private Map<String, Map<String, Double>> getCorrelationMap() {
		
		// This map contains a map for each profile (position)
		// Each submap comprises the Pearson Correlation to each other profile
		Map<String, Map<String, Double>> map = new HashMap<>();
		Correlator cor = new Correlator();

		for (Entry<String, List<Double>> profile : doubleProfileMap.entrySet()) {
			if (!map.containsKey(profile.getKey())) {
				map.put(profile.getKey(), new HashMap<String, Double>());
			}
			
			for (Entry<String, List<Double>> compProfile : doubleProfileMap.entrySet()) {
				Double coeff = cor.correlateOverlap(Toolbox.convertToDoubleArray(profile.getValue().toArray()), Toolbox.convertToDoubleArray(compProfile.getValue().toArray()));
				map.get(profile.getKey()).put(compProfile.getKey(), (double) coeff);
			}
		}

		return map;
	}
	//check
	public ObservableList<XYChart.Series<Integer, Double>> getAllRankedCorrelationSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<Integer, Double>> seriesList = new ArrayList<>();
		
		for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
			if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
				if (correlationMap.containsKey(getListKey((position + offset), correlationMap.keySet()))) {
					XYChart.Series<Integer, Double> series = new XYChart.Series<>();
					List<Double> correlations = new ArrayList<>();
					for (Entry<String, Double> subEntry : correlationMap.get(getListKey((position + offset), correlationMap.keySet())).entrySet()) {
						if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
							if (subEntry.getValue().isNaN()) {
								correlations.add(0d);
							} else {
								correlations.add(subEntry.getValue());
							}
						}
					}
					Collections.sort(correlations);
					for (int i = 0; i < correlations.size(); i++) {
						XYChart.Data<Integer, Double> dataPoint = new XYChart.Data<>();
						dataPoint.setXValue(i);
						dataPoint.setYValue(correlations.get(i));
						Label label = new Label("");
						label.setPrefSize(12.0, 12.0);
						Tooltip.install(label, new Tooltip(String.valueOf(position + offset)));
						dataPoint.setNode(label);
						series.getData().add(dataPoint);
					}
					series.setName(String.valueOf(position + offset));
					seriesList.add(series);
				}
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
}