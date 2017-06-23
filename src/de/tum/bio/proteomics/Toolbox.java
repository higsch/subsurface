package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import application.Main;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Core tools for ProteomeDiver.
 * @author Matthias Stahl
 *
 */

public final class Toolbox {
	
	public static String[] aminoAcidsSingleLetter() {
		return new String[]{"A", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "Y"};
	}
	
	public static String[] aminoAcidsTripleLetter() {
		return new String[]{"Ala", "Cys", "Asp", "Glu", "Phe", "Gly", "His", "Ile", "Lys", "Leu", "Met", "Asn", "Pro", "Gln", "Arg", "Ser", "Thr", "Val", "Tyr"};
	}
	
	public static void combineSequencesAndProteinGroups(FastaFile fastaFile, ObservableMap<Integer, ProteinGroup> proteinGroupsMap, Main mainApp) {
		if (fastaFile != null && proteinGroupsMap != null) {
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					long numberOfProteinGroups = proteinGroupsMap.size();
					long index = 0;
					updateMessage("Combining information...");
					// Go through each protein
					for (ProteinGroup proteinGroup : proteinGroupsMap.values()) {
						updateProgress(index, numberOfProteinGroups);
						String leadingDatabaseIdList = proteinGroup.getDatabaseIds().split(";")[0];
						String sequence = fastaFile.getSequenceById(leadingDatabaseIdList);
						if (sequence != null) {
							proteinGroup.setSequence(sequence);
							updateMessage(proteinGroup.getNames());
						}
						index++;
					}
					return null;
				}
				
				@Override
				protected void succeeded() {
					super.succeeded();
					updateMessage("Done.");
					updateProgress(0.0, 1.0);
				}
				
				@Override
				protected void failed() {
					super.failed();
					updateMessage("Cancelled.");
					updateProgress(0.0, 1.0);
				}
			};
			if (mainApp != null) {
				mainApp.getProgressBar().progressProperty().bind(task.progressProperty());
				mainApp.getStatusLabel().textProperty().bind(task.messageProperty());
				task.setOnFailed(workerStateEvent -> {
					mainApp.getProgressBar().progressProperty().unbind();
					mainApp.getStatusLabel().textProperty().unbind();
					mainApp.getProgressBar().setProgress(0.0);
					mainApp.getStatusLabel().setText("Cancelled.");
					Alert alert = new Alert(AlertType.ERROR, workerStateEvent.getEventType().toString(), ButtonType.OK);
					alert.showAndWait();
				});
			}
			new Thread(task).start();
		}
	}
	
	public static void combineStatisticsAndProteinGroups(StatisticsFile statisticsFile, PeptideId peptideId, Main mainApp) {
		Map<Integer, ProteinGroup> proteinGroupsMap = peptideId.getAllProteinGroups();
		if (statisticsFile != null && proteinGroupsMap != null) {
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					long numberOfProteinGroups = proteinGroupsMap.size();
					long index = 0;
					String id = null;
					updateMessage("Combining information...");
					// Go through each protein
					for (Entry<Integer, ProteinGroup> proteinGroup : proteinGroupsMap.entrySet()) {
						updateProgress(index, numberOfProteinGroups);
						updateMessage(proteinGroup.getValue().getNames());
						id = proteinGroup.getValue().getDatabaseIds();
						proteinGroup.getValue().setLog2Enrichment(statisticsFile.getEnrichmentByName(id));
						proteinGroup.getValue().setMinusLog10PValue(statisticsFile.getPValueByName(id));
						index++;
					}
					updateMessage("Done.");
					updateProgress(0.0, 1.0);
					return null;
				}
				
				@Override
				protected void succeeded() {
					super.succeeded();
					updateMessage("Done.");
					updateProgress(0.0, 1.0);
				}
				
				@Override
				protected void failed() {
					super.failed();
					updateMessage("Cancelled.");
					updateProgress(0.0, 1.0);
				}
			};
			if (mainApp != null) {
				mainApp.getProgressBar().progressProperty().bind(task.progressProperty());
				mainApp.getStatusLabel().textProperty().bind(task.messageProperty());
				task.setOnFailed(workerStateEvent -> {
					mainApp.getProgressBar().progressProperty().unbind();
					mainApp.getStatusLabel().textProperty().unbind();
					mainApp.getProgressBar().setProgress(0.0);
					mainApp.getStatusLabel().setText("Cancelled.");
					Alert alert = new Alert(AlertType.ERROR, workerStateEvent.getEventType().toString(), ButtonType.OK);
					alert.showAndWait();
				});
				task.setOnSucceeded(workerStateEvent -> {
					peptideId.setStatisticsAdded(true);
				});
			}
			new Thread(task).start();
		}
	}
	
	public static double log(Number x, int base) {
		if (x.doubleValue() == 0) {
			return Double.NaN;
		} else {
			return Math.log(x.doubleValue()) / Math.log(base);
		}
	}
	
	public static List<List<Integer>> simpleMap(String sequenceFrom, String sequenceTo) {
		List<Integer> result = new ArrayList<>();
		List<List<Integer>> resultList = new ArrayList<>();
	
		int start = 0;
		
		while (sequenceTo.indexOf(sequenceFrom, start) > -1) {
			// Empty former results
			result.clear();
			// Add start position
			start = sequenceTo.indexOf(sequenceFrom, start) + 1;
			result.add(start);
			// Add end position to results
			result.add(start + sequenceFrom.length() - 1);
			// Add all results to global list
			resultList.add(result);
		}
		
		return resultList;
	}
	
	public static double fishersZ(double r) {
		double z;
		z = 0.5 * Math.log((1+r)/(1-r));
		return z;
	}
	
	public static double fishersZinverse(double z) {
		double r;
		r = (Math.exp(2 * z) - 1)/(Math.exp(2 * z) + 1);
		return r;
	}
	
	public static <T extends Number> int getNumberOfNonZeroValues(List<T> list) {
		int n = 0;
		for (T number : list) {
			if ((double) number != 0.0) {
				n++;
			}
		}
		return n;
	}
	
	public static <T extends Number> int getNumberOfNonNullValues(List<T> list) {
		int n = 0;
		for (T number : list) {
			if (number != null) {
				n++;
			}
		}
		return n;
	}
	
	public static int getNumberOfNonNaNValues(List<Double> list) {
		int n = 0;
		for (Double number : list) {
			if (!number.isNaN()) {
				n++;
			}
		}
		return n;
	}
	
	public static void printDoubleArray(double[] array) {
		StringBuilder output = new StringBuilder();
		output.append("Content of array " + array.toString() + ": ");
		for (double entry : array) {
			output.append(String.valueOf(entry) + ", ");
		}
		output.replace(output.length() - 3, output.length() - 1, "");
		System.out.println(output.toString());
	}
	
	public static <T extends Number & Comparable<T>> long getMinFromMapValues(Map<?, T> map) {
		// select start value
		long min = 0;
		for (Entry<?, T> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				min = (long) entry.getValue();
				break;
			}
		}
		if (min == 0) {
			return 0;
		}
		for (Entry<?, T> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				if ((long) entry.getValue() < min) {
					min = (long) entry.getValue();
				}
			}
		}
		return min;
	}
	
	public static <T extends Number & Comparable<T>> double getMinFromListValues(List<T> list) {
		// select start value
		double min = 0d;
		for (T entry : list) {
			if (entry != null) {
				min = (double) entry;
				break;
			}
		}
		if (min == 0) {
			return 0d;
		}
		for (T entry : list) {
			if (entry != null) {
				if ((double) entry < min) {
					min = (double) entry;
				}
			}
		}
		return min;
	}
	
	public static <T extends Number & Comparable<T>> long getMaxFromMapValues(Map<?, T> map) {
		// select star value
		long max = 0;
		for (Entry<?, T> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				max = (long) entry.getValue();
				break;
			}
		}
		if (max == 0) {
			return 0;
		}
		for (Entry<?, T> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				if ((long) entry.getValue() > (long) max) {
					max = (long) entry.getValue();
				}
			}
		}
		return max;
	}
	
	public static <T extends Number & Comparable<T>> double getMaxFromListValues(List<T> list) {
		// select star value
		double max = 0d;
		for (T entry : list) {
			if (entry != null) {
				max = (double) entry;
				break;
			}
		}
		if (max == 0) {
			return 0d;
		}
		for (T entry : list) {
			if (entry != null) {
				if ((double) entry > (double) max) {
					max = (double) entry;
				}
			}
		}
		return max;
	}
	
	public static double normalize(Number value, Number min, Number max) {
		if (value == null) {
			return 0d;
		}
		if (max.longValue() - min.longValue() == 0) {
			return 0d;
		} else {
			return ((double) ((value.longValue() - min.longValue()))/((double) (max.longValue() - min.longValue())));
		}
	}
	
	public static double[] convertToDoubleArray(Object[] objectArray) {
		double[] doubleArray = new double[objectArray.length];
		
		for (int i = 0; i < objectArray.length; i++) {
			doubleArray[i] = (double) objectArray[i];
		}
		
		return doubleArray;
	}
}