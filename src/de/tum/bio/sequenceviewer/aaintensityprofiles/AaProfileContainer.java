package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.Tools;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class AaProfileContainer {
	
	private String sequence;
	private Map<Integer, Map<String, Long>> profileMap;
	
	public AaProfileContainer() {
		// empty
	}
	
	public AaProfileContainer(String sequence) {
		this.sequence = sequence;
	}
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public void addProfileMap(Map<Integer, Map<String, Long>> profileMap) {
		this.profileMap = profileMap;
	}
	
	public Map<Integer, Map<String, Long>> getProfileMap() {
		return profileMap;
	}
	
	public void removeProfile(int position) {
		profileMap.remove(position);
	}
	
	public Map<String, Long> getProfileByPosition(int position) {
		return profileMap.get(position);
	}
	
	public XYChart.Series<String, Double> getXYChartSeriesByPosition(int position) {
		XYChart.Series<String, Double> series = new XYChart.Series<>();
		
		if (profileMap.containsKey(position)) {
			for (Entry<String, Long> entry : profileMap.get(position).entrySet()) {
				if (!Double.valueOf(Tools.log(entry.getValue(), 2)).isNaN()) {
					XYChart.Data<String, Double> dataPoint = new XYChart.Data<String, Double>(entry.getKey(), Double.valueOf(Tools.log(entry.getValue(), 2)));
					Label label = new Label(String.valueOf(position));
					label.toBack();
					dataPoint.setNode(label);
					series.getData().add(dataPoint);
				}
			}
		}
		
		return series;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeries() {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			seriesList.add(getXYChartSeriesByPosition(entry.getKey()));
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeriesByResidue(List<Character> residues) {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			if (residues.contains(sequence.charAt(entry.getKey() - 1))) {
				seriesList.add(getXYChartSeriesByPosition(entry.getKey()));
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
}