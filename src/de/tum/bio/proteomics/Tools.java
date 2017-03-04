package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import application.Main;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Core tools for ProteomeDiver.
 * @author Matthias Stahl
 *
 */

public final class Tools {
	
	public static void combineSequencesAndProteinGroups(FastaFile fastaFile, Map<Integer, ProteinGroup> proteinGroupsMap, Main mainApp) {
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
	
	public static double log(long x, int base) {
		if (x == 0) {
			return Double.NaN;
		} else {
			return Math.log(x) / Math.log(base);
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
}
