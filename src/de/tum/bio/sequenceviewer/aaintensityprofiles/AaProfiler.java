package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Collections;
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
import javafx.scene.control.Tooltip;

public class AaProfiler {
	
	private final String SUM_TITLE = "Sum";
	
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
	
	private XYChart.Series<String, Double> getXYChartSeriesByPosition(int position, boolean normalize) {
		XYChart.Series<String, Double> series = new XYChart.Series<>();
		long min = 0;
		long max = 0;
		
		if (profileMap.containsKey(position)) {
			// Calculate min/max in case of normalization
			if (normalize) {
				min = Toolbox.getMinFromMapValues(profileMap.get(position));
				max = Toolbox.getMaxFromMapValues(profileMap.get(position));
			}
			
			for (Entry<String, Long> entry : profileMap.get(position).entrySet()) {
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
						Tooltip.install(label, new Tooltip(String.valueOf(position)));
						dataPoint.setNode(label);
						series.getData().add(dataPoint);
					}
				}
			}
			series.setName(String.valueOf(position));
		}

		return series;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeries() {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			seriesList.add(getXYChartSeriesByPosition(entry.getKey(), false));
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllNormalizedXYChartSeries() {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			seriesList.add(getXYChartSeriesByPosition(entry.getKey(), true));
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllXYChartSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			if (((entry.getKey() - 1 + offset) >= 0) && ((entry.getKey() - 1 + offset) < protein.getSequenceAsString().length())) {
				if (residues.contains(protein.getSequenceAsString().charAt(entry.getKey() - 1))) {
					seriesList.add(getXYChartSeriesByPosition(entry.getKey() + offset, false));
				}
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public ObservableList<XYChart.Series<String, Double>> getAllNormalizedXYChartSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<String, Double>> seriesList = new ArrayList<>();
		
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			if ((entry.getKey() - 1 + offset) >= 0 && ((entry.getKey() - 1 + offset) < protein.getSequenceAsString().length())) {
				if (residues.contains(protein.getSequenceAsString().charAt(entry.getKey() - 1))) {
					seriesList.add(getXYChartSeriesByPosition(entry.getKey() + offset, true));
				}
			}
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
	
	public Map<String, Map<String, Double>> getCorrelationMatrix(List<Character> residues, int offset, boolean normalize) {
		Map<Integer, List<Double>> profiles = new HashMap<>();
		
		// Create map with double profile values
		for (Entry<Integer, Map<String, Long>> entry : profileMap.entrySet()) {
			if (((entry.getKey() - 1 + offset) >= 0) && (protein.getSequenceAsString().length() > (entry.getKey() - 1 + offset))) {
				if (residues.contains(protein.getSequenceAsString().charAt(entry.getKey() - 1))) {
					for (Entry<String, Long> dataPoint : profileMap.get(entry.getKey() + offset).entrySet()) {
						if (!profiles.containsKey(entry.getKey())) {
							profiles.put(entry.getKey(), new ArrayList<Double>());
						}
						double intensity;
						if (dataPoint.getValue() == null) {
							intensity = 0d; //Double.NaN;
						} else {
							intensity = (double) dataPoint.getValue();
						}
						profiles.get(entry.getKey()).add(intensity);
					}
					if (normalize) {
						List<Double> normalizedIntensities = new ArrayList<>();
						double min = Toolbox.getMinFromListValues(profiles.get(entry.getKey()));
						double max = Toolbox.getMaxFromListValues(profiles.get(entry.getKey()));
						for (double value : profiles.get(entry.getKey())) {
							normalizedIntensities.add(Toolbox.normalize(value, min, max));
						}
						profiles.replace(entry.getKey(), normalizedIntensities);
						
					}
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
				Double coeff = cor.correlateOverlap(Toolbox.convertToDoubleArray(profile.getValue().toArray()), Toolbox.convertToDoubleArray(compProfile.getValue().toArray()));
				correlationMap.get(String.valueOf(profile.getKey())).put(String.valueOf(compProfile.getKey()), (double) coeff);
				if (!coeff.isNaN()) {
					sum += coeff;
				}
			}
			
			correlationMap.get(String.valueOf(profile.getKey())).put(SUM_TITLE, sum);
		}
		
		return correlationMap;
	}
	
	public String getCorrelationMatrixAsHTML(List<Character> residues, int offset, boolean normalize) {
		Map<String, Map<String, Double>> correlationMap = getCorrelationMatrix(residues, offset, normalize);
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
	
	public ObservableList<XYChart.Series<Integer, Double>> getAllRankedCorrelationSeriesByResidue(List<Character> residues, int offset) {
		List<XYChart.Series<Integer, Double>> seriesList = new ArrayList<>();
		Map<String, Map<String, Double>> correlationMap = getCorrelationMatrix(residues, offset, false);
		for (Entry<String, Map<String, Double>> entry : correlationMap.entrySet()) {
			XYChart.Series<Integer, Double> series = new XYChart.Series<>();
			List<Double> correlations = new ArrayList<>();
			for (Entry<String, Double> subEntry : entry.getValue().entrySet()) {
				if (!subEntry.getKey().contains(SUM_TITLE)) {
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
				Tooltip.install(label, new Tooltip(entry.getKey()));
				dataPoint.setNode(label);
				series.getData().add(dataPoint);
			}
			series.setName(entry.getKey());
			seriesList.add(series);
		}
		
		return FXCollections.observableArrayList(seriesList);
	}
}