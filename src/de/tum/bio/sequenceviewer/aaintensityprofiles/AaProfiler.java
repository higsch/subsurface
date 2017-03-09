package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.Toolbox;
import de.tum.bio.utils.Correlator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class AaProfiler {
	
	private ProteinGroup protein;
	private Map<Integer, Map<String, Long>> profileMap;
	
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
				if (entry.getValue() != null) {
					XYChart.Data<String, Double> dataPoint = new XYChart.Data<String, Double>(entry.getKey(), Double.valueOf(Toolbox.log(entry.getValue(), 2)));
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
			if (residues.contains(protein.getSequenceAsString().charAt(entry.getKey() - 1))) {
				seriesList.add(getXYChartSeriesByPosition(entry.getKey()));
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public Map<String, Map<String, Double>> getCorrelationMatrix(List<Character> residues) {
		Map<Integer, List<Double>> profiles = new HashMap<>();
		
		// Create map with double profile values
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			if (residues.contains(protein.getSequenceAsString().charAt(entry.getKey() - 1))) {
				for (Entry<String, Long> dataPoint : entry.getValue().entrySet()) {
					if (!profiles.containsKey(entry.getKey())) {
						profiles.put(entry.getKey(), new ArrayList<Double>());
					}
					double intensity;
					if (dataPoint.getValue() == null) {
						intensity = Double.NaN;
					} else {
						intensity = (double) dataPoint.getValue();
					}
					profiles.get(entry.getKey()).add(intensity);
				}
			}
		}
		
		// This map contains a map for each profile (position)
		// Each submap comprises the Pearson Correlation to each other profile
		Map<String, Map<String, Double>> correlationMap = new HashMap<>();
		Correlator cor = new Correlator();
		// for each profile
		for (Entry<Integer, List<Double>> profile : profiles.entrySet()) {
			if (!correlationMap.containsKey(String.valueOf(profile.getKey()))) {
				correlationMap.put(String.valueOf(profile.getKey()), new HashMap<String, Double>());
			}
			double sum = 0d;
			for (Entry<Integer, List<Double>> compProfile : profiles.entrySet()) {
				Double coeff = cor.correlateOverlap(convertToDoubleArray(profile.getValue().toArray()), convertToDoubleArray(compProfile.getValue().toArray()));
				correlationMap.get(String.valueOf(profile.getKey())).put(String.valueOf(compProfile.getKey()), (double) coeff);
				if (!coeff.isNaN() && coeff.doubleValue() != 1.0) {
					sum += Toolbox.fishersZ(coeff.doubleValue());
				}
			}
			int nonZeroValues = Toolbox.getNumberOfNonNaNValues(profile.getValue());
			if (nonZeroValues > 0) {
				correlationMap.get(String.valueOf(profile.getKey())).put("Sum of z", sum/nonZeroValues);
			} else {
				correlationMap.get(String.valueOf(profile.getKey())).put("Sum of z", 0.0);
			}
		}
		
		return correlationMap;
	}
	
	public String getCorrelationMatrixAsHTML(List<Character> residues) {
		Map<String, Map<String, Double>> correlationMap = getCorrelationMatrix(residues);
		StringBuilder builder = new StringBuilder();
		
		builder.append("<html><head></head><body><table>");
		// Heading line
		builder.append("<tr><td></td>");
		for (Entry<String, Map<String, Double>> row : correlationMap.entrySet()) {
			for (Entry<String, Double> column : row.getValue().entrySet()) {
				builder.append("<td>");
				builder.append(column.getKey());
				builder.append("</td>");
			}
			break;
		}
		builder.append("</tr>");
		
		// Content
		for (Entry<String, Map<String, Double>> row : correlationMap.entrySet()) {
			builder.append("<tr>");
			builder.append("<td>");
			builder.append(row.getKey());
			builder.append("</td>");
			for (Entry<String, Double> column : row.getValue().entrySet()) {
				builder.append("<td>");
				builder.append((double) Math.round(column.getValue() * 100d) / 100d);
				builder.append("</td>");
			}
			builder.append("</tr>");
		}
		builder.append("</table></body></html>");
		
		return builder.toString();
	}
	
	private double[] convertToDoubleArray(Object[] objectArray) {
		double[] doubleArray = new double[objectArray.length];
		
		for (int i = 0; i < objectArray.length; i++) {
			doubleArray[i] = (double) objectArray[i];
		}
		
		return doubleArray;
	}
}