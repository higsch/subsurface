package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.Toolbox;
import de.tum.bio.utils.AlphanumComparator;
import de.tum.bio.utils.Correlator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class AaProfiler extends Profiler {
	private BooleanProperty ready = new SimpleBooleanProperty(false);
	private DoubleProperty progress = new SimpleDoubleProperty(0.0);
	private StringProperty status = new SimpleStringProperty("");
	
	private ProteinGroup protein;
	private Map<Integer, Map<String, Long>> profileMap;
	private ObservableList<String> experiments = FXCollections.observableArrayList();
	private Map<String, Map<String, Long>> reducedProfileMap = new HashMap<>();
	
	private Map<String, XYChart.Series<String, Double>> seriesMap;
	private Map<String, XYChart.Series<String, Double>> normalizedSeriesMap;
	private Map<String, List<Double>> doubleProfileMap;
	private Map<String, Map<String, Double>> correlationMap;
	
	private ObservableList<XYChart.Series<String, Double>> chartSeries = FXCollections.observableArrayList();
	private ObservableList<XYChart.Series<String, Double>> chartNormalizedSeries = FXCollections.observableArrayList();
	private ObservableList<XYChart.Series<Integer, Double>> chartCorrelations = FXCollections.observableArrayList();
	
	private ObservableList<String> selectedExperiments = FXCollections.observableArrayList();
	
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
	
	public double getProgress() {
		return progress.get();
	}
	
	public String getStatus() {
		return status.get();
	}
	
	public boolean isReady() {
		return ready.get();
	}
	
	public BooleanProperty readyProperty() {
		return ready;
	}
	
	public DoubleProperty progressProperty() {
		return progress;
	}
	
	public StringProperty statusProperty() {
		return status;
	}
	
	public ObservableList<String> getSelectedExperiments() {
		return selectedExperiments;
	}
	
	public void setSelectedExperiments(ObservableList<String> selectedExperiments) {
		this.selectedExperiments = selectedExperiments;
	}
	
	public void addProfileMap(Map<Integer, Map<String, Long>> profileMap) {
		this.profileMap = profileMap;
		if (profileMap.size() > 0) {
			for (String experiment : profileMap.get(1).keySet()) {
				experiments.add(experiment);
			}
			Collections.sort(experiments, new AlphanumComparator<>());;
		}
	}
	
	public ObservableList<String> getExperiments() {
		return experiments;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getSeries() {
		return chartSeries;
	}
	
	public ObservableList<XYChart.Series<String, Double>> getNormalizedSeries() {
		return chartNormalizedSeries;
	}
	
	public ObservableList<XYChart.Series<Integer, Double>> getCorrelations() {
		return chartCorrelations;
	}
	
	public void init() {
		progress.set(-1.0);
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage("Parse profiles...");
				reduceProfileMap();
				
				updateMessage("Generate series for charts...");
				seriesMap = generateSeriesMap(false);
				normalizedSeriesMap = generateSeriesMap(true);
				
				updateMessage("Calculate correlation coefficients...");
				doubleProfileMap = getDoubleProfileMap(reducedProfileMap, selectedExperiments);
				correlationMap = getCorrelationMap();
				
				updateMessage("");
				return null;
			}
		};
		task.setOnSucceeded(workerStateEvent -> {
			progress.set(0.0);
			ready.set(true);
		});
		task.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
			if(newValue != null) {
				Exception e = (Exception) newValue;
			    e.printStackTrace();
			}
		});
		status.bind(task.messageProperty());
		Thread t = new Thread(task);
		t.start();
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
			if (profileMap.containsKey(position + 1)) {
				if (profileMap.get(position).equals(profileMap.get(position + 1))) {
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
						Tooltip.install(label, new Tooltip(getLegendEntryFromKeyString(position)));
						dataPoint.setNode(label);
						series.getData().add(dataPoint);
					}
				}
			}
			series.setName(getLegendEntryFromKeyString(position));
			map.put(position, series);
		}

		return map;
	}
	
	public void getAllXYChartSeries() {
		chartSeries.clear();
		chartSeries.addAll(seriesMap.values());
	}
	
	public void getAllNormalizedXYChartSeries() {
		chartNormalizedSeries.clear();
		chartNormalizedSeries.addAll(normalizedSeriesMap.values());
	}
	
	public void calculateAllXYChartSeriesByResidue(List<Character> residues, int offset) {
		Task<List<XYChart.Series<String, Double>>> task = new Task<List<XYChart.Series<String, Double>>>() {
			@Override
			protected List<XYChart.Series<String, Double>> call() throws Exception {
				updateMessage("Update profile chart...");
				List<XYChart.Series<String, Double>> chartSeriesTmp = new ArrayList<>();
				List<String> addedSeries = new ArrayList<>();
				for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
					updateProgress((long) position, (long) protein.getSequenceAsString().length()); 
					String keyString = getListKey(position + offset, seriesMap.keySet());
					if (addedSeries.contains(keyString)) {
						continue;
					}
					if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
						if (seriesMap.containsKey(keyString)) {
							chartSeriesTmp.add(seriesMap.get(keyString));
							addedSeries.add(keyString);
						}
					}
				}
				return chartSeriesTmp;
			}
		};
		task.setOnSucceeded(workerStateEvent -> {
			chartSeries.clear();
			chartSeries.addAll(task.getValue());
		});
		task.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
			if(newValue != null) {
				Exception e = (Exception) newValue;
			    e.printStackTrace();
			}
		});
		Thread t = new Thread(task);
		t.start();
	}
	
	public void calculateAllNormalizedXYChartSeriesByResidue(List<Character> residues, int offset) {
		Task<List<XYChart.Series<String, Double>>> task = new Task<List<XYChart.Series<String, Double>>>() {
			@Override
			protected List<XYChart.Series<String, Double>> call() throws Exception {
				updateMessage("Update normalized profile chart...");
				List<XYChart.Series<String, Double>> chartNormalizedSeriesTmp = new ArrayList<>();
				List<String> addedSeries = new ArrayList<>();
				for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
					updateProgress((long) position, protein.getSequenceAsString().length());
					String keyString = getListKey(position + offset, normalizedSeriesMap.keySet());
					if (addedSeries.contains(keyString)) {
						continue;
					}
					if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
						if (normalizedSeriesMap.containsKey(keyString)) {
							chartNormalizedSeriesTmp.add(normalizedSeriesMap.get(keyString));
							addedSeries.add(keyString);
						}
					}
				}
				return chartNormalizedSeriesTmp;
			}
		};
		task.setOnSucceeded(workerStateEvent -> {
			chartNormalizedSeries.clear();
			chartNormalizedSeries.addAll(task.getValue());
		});
		task.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
			if(newValue != null) {
				Exception e = (Exception) newValue;
			    e.printStackTrace();
			}
		});
		Thread t = new Thread(task);
		t.start();
	}
	
	private Map<String, Map<String, Double>> getCorrelationMap() {
		
		// This map contains a map for each profile (position)
		// Each submap comprises the Pearson Correlation to each other profile
		Map<String, Map<String, Double>> map = new HashMap<>();
		Correlator cor = new Correlator();

		for (Entry<String, List<Double>> profile : doubleProfileMap.entrySet()) {
			if (profile.getValue().size() > 1) {
				if (!map.containsKey(profile.getKey())) {
					map.put(profile.getKey(), new HashMap<String, Double>());
				}
				
				for (Entry<String, List<Double>> compProfile : doubleProfileMap.entrySet()) {
					Double coeff = cor.correlateOverlap(Toolbox.convertToDoubleArray(profile.getValue().toArray()), Toolbox.convertToDoubleArray(compProfile.getValue().toArray()));
					map.get(profile.getKey()).put(compProfile.getKey(), (double) coeff);
				}
			}
		}

		return map;
	}

	public void calculateAllRankedCorrelationSeriesByResidue(List<Character> residues, int offset) {
		Task<List<XYChart.Series<Integer, Double>>> task = new Task<List<XYChart.Series<Integer, Double>>>() {
			@Override
			protected List<XYChart.Series<Integer, Double>> call() throws Exception {
				updateMessage("Update correlations...");
				List<XYChart.Series<Integer, Double>> chartCorrelationsTmp = new ArrayList<>();
				List<String> addedSeries = new ArrayList<>();
				for (int position = 1; position <= protein.getSequenceAsString().length(); position++) {
					updateProgress((long) position, (long) protein.getSequenceAsString().length()); 
					if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
						String keyString = getListKey((position + offset), correlationMap.keySet());
						if (!addedSeries.contains(keyString)) {
							if (correlationMap.containsKey(keyString)) {
								XYChart.Series<Integer, Double> series = new XYChart.Series<>();
								List<Double> correlationsList = new ArrayList<>();
								for (Entry<String, Double> subEntry : correlationMap.get(keyString).entrySet()) {
									if (residues.contains(protein.getSequenceAsString().charAt(position - 1))) {
										if (subEntry.getValue().isNaN()) {
											correlationsList.add(0d);
										} else {
											correlationsList.add(subEntry.getValue());
										}
										addedSeries.add(keyString);
									}
								}
								Collections.sort(correlationsList);
								for (int i = 0; i < correlationsList.size(); i++) {
									XYChart.Data<Integer, Double> dataPoint = new XYChart.Data<>();
									dataPoint.setXValue(i);
									dataPoint.setYValue(correlationsList.get(i));
									Label label = new Label("");
									label.setPrefSize(12.0, 12.0);
									Tooltip.install(label, new Tooltip(getLegendEntryFromKeyString(keyString)));
									dataPoint.setNode(label);
									series.getData().add(dataPoint);
								}
								series.setName(getLegendEntryFromKeyString(keyString));
								chartCorrelationsTmp.add(series);
							}
						}
					}
				}
				updateProgress(0.0, 1.0);
				updateMessage("");
				return chartCorrelationsTmp;
			}
		};
		task.setOnSucceeded(workerStateEvent -> {
			chartCorrelations.clear();
			chartCorrelations.addAll(task.getValue());
		});
		task.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
			if(newValue != null) {
				Exception e = (Exception) newValue;
			    e.printStackTrace();
			}
		});
		progress.bind(task.progressProperty());
		status.bind(task.messageProperty());
		Thread t = new Thread(task);
		t.start();
	}
	
	private String getLegendEntryFromKeyString(String keyString) {
		StringBuilder tmp = new StringBuilder();
		List<String> keyList = Arrays.asList(keyString.split(","));
		if (keyList.size() > 0) {
			tmp.append(keyList.get(0).replace("[", "").replace("]", "").trim());
			if (keyList.size() > 1) {
				tmp.append(" - " + keyList.get(keyList.size() - 1).replace("[", "").replace("]", "").trim());
			}
		}
		return tmp.toString();
	}
	
	public void updateCorrelationsByExperiments(List<Character> residues, int offset) {
		Task<List<XYChart.Series<Integer, Double>>> task = new Task<List<XYChart.Series<Integer, Double>>>() {
			@Override
			protected List<XYChart.Series<Integer, Double>> call() throws Exception {
				updateProgress(-1.0, 1.0);
				updateMessage("Calculate values...");
				doubleProfileMap = getDoubleProfileMap(reducedProfileMap, selectedExperiments);
				correlationMap = getCorrelationMap();
				updateProgress(0.0, 1.0);
				updateMessage("");
				return null;
			}
		};
		task.setOnSucceeded(workerStateEvent -> {
			calculateAllRankedCorrelationSeriesByResidue(residues, offset);
		});
		task.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
			if(newValue != null) {
				Exception e = (Exception) newValue;
			    e.printStackTrace();
			}
		});
		progress.bind(task.progressProperty());
		status.bind(task.messageProperty());
		Thread t = new Thread(task);
		t.start();
	}
}