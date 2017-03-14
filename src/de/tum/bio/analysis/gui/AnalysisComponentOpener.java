package de.tum.bio.analysis.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.Main;
import de.tum.bio.analysis.Analysis;
import de.tum.bio.analysis.AnalysisComponent;
import de.tum.bio.analysis.AnalysisComponentType;
import de.tum.bio.proteomics.AnalysisSummary;
import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.FastaFileReader;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.StatisticsFile;
import de.tum.bio.proteomics.StatisticsReader;
import de.tum.bio.proteomics.StatisticsTableHeaders;
import de.tum.bio.proteomics.maxquant.MQEvidenceMatcher;
import de.tum.bio.proteomics.maxquant.MQModificationsReader;
import de.tum.bio.proteomics.maxquant.MQPeptidesReader;
import de.tum.bio.proteomics.maxquant.MQProteinGroupsReader;
import de.tum.bio.proteomics.maxquant.MQReader;
import de.tum.bio.proteomics.maxquant.MQSummaryReader;
import de.tum.bio.proteomics.perseus.PerseusFileReader;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class AnalysisComponentOpener {
	
	private AnalysisComponentOpener() {
		// empty
	}
	
	public static void getMQCollection(Main mainApp, Analysis analysis) {
		String txtDirectory = getDirectory(mainApp.getStage());
		if (txtDirectory != null) {
			Task<PeptideId> readTask = new Task<PeptideId>() {
				@Override
				protected PeptideId call() throws Exception {
					PeptideId peptideId = null;
					
						Map<AnalysisComponentType, List<AnalysisComponent>> tMap = new HashMap<>();
						List<AnalysisComponent> proteinGroupsList = null;
						MQReader proteinGroupsReader = new MQProteinGroupsReader();
						proteinGroupsReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
						proteinGroupsReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
						try {
							proteinGroupsList = proteinGroupsReader.read(txtDirectory, null);
						} catch (IOException e) {
							Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
							alert.showAndWait();
						}
						List<AnalysisComponent> peptidesList = null;
						MQReader peptidesReader = new MQPeptidesReader();
						peptidesReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
						peptidesReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
						try {
							peptidesList = peptidesReader.read(txtDirectory, null);
						} catch (IOException e) {
							Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
							alert.showAndWait();
						}
						tMap.put(AnalysisComponentType.MaxQuant_ProteinGroups, proteinGroupsList);
						tMap.put(AnalysisComponentType.MaxQuant_Peptides, peptidesList);
						peptideId = new PeptideId(-1, tMap, Paths.get(txtDirectory).getFileName().toString());
						
						MQReader summaryReader = new MQSummaryReader();
						summaryReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
						summaryReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
						peptideId.setSummary((AnalysisSummary) summaryReader.read(txtDirectory, null).get(0));
						
						// Read modifications
						MQReader modificationsReader;
						for (String variableModification : peptideId.getSummary().getVariableModifications()) {
							modificationsReader = new MQModificationsReader(variableModification);
							if (modificationsReader.fileExists(txtDirectory, variableModification)) {
								modificationsReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
								modificationsReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
								peptideId.setModifications(variableModification, modificationsReader.read(txtDirectory, null));
							}
						}
						
						// Match detailed peptide information
						MQEvidenceMatcher evidenceMatcher = new MQEvidenceMatcher();
						evidenceMatcher.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
						evidenceMatcher.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
						evidenceMatcher.match(peptideId, txtDirectory, null);
						
						updateMessage("");
					
					return peptideId;
				}
				
				@Override
				protected void failed() {
					super.failed();
					updateMessage("Cancelled.");
					updateProgress(0.0, 1.0);
				}
			};
			readTask.setOnSucceeded(workerStateEvent -> {
				analysis.addPeptideId(readTask.getValue());
				analysis.setDataAssigned(true);
	        });
			readTask.setOnFailed(workerStateEvent -> {
				Alert alert = new Alert(AlertType.ERROR, workerStateEvent.getEventType().toString(), ButtonType.OK);
				alert.showAndWait();
			});
			readTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
				if(newValue != null) {
					Exception e = (Exception) newValue;
				    e.printStackTrace();
				}
			});
			mainApp.getProgressBar().progressProperty().bind(readTask.progressProperty());
			mainApp.getStatusLabel().textProperty().bind(readTask.messageProperty());
			
			Thread t = new Thread(readTask);
			t.start();
		}
	}
	
	public static void getPerseusCollection(Main mainApp, Analysis analysis) {
		String filePath = getFile(mainApp.getStage());
		if (filePath != null) {
			StatisticsFileHeaderAssigner headerAssigner = new StatisticsFileHeaderAssigner(filePath, mainApp.getStage());
			Map<StatisticsTableHeaders, String> headerMap = headerAssigner.getSelections();
			if (headerMap != null) {
				Task<StatisticsFile> readTask = new Task<StatisticsFile>() {
					@Override
					protected StatisticsFile call() throws Exception {
						StatisticsFile statisticsFile = null;
						StatisticsReader perseusReader = new PerseusFileReader();
						try {
							perseusReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
							perseusReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
							statisticsFile = perseusReader.read(filePath, headerMap);
						} catch (IOException e) {
							Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
							alert.showAndWait();
						}
						updateMessage("");
						return statisticsFile;
					}
					
					@Override
					protected void failed() {
						super.failed();
						updateMessage("Cancelled.");
						updateProgress(0.0, 1.0);
					}
				};
				readTask.setOnSucceeded(workerStateEvent -> {
					analysis.addStatisticsFile(readTask.getValue());
					analysis.setDataAssigned(true);
		        });
				readTask.setOnFailed(workerStateEvent -> {
					Alert alert = new Alert(AlertType.ERROR, workerStateEvent.getEventType().toString(), ButtonType.OK);
					alert.showAndWait();
				});
				mainApp.getProgressBar().progressProperty().bind(readTask.progressProperty());
				mainApp.getStatusLabel().textProperty().bind(readTask.messageProperty());
				new Thread(readTask).start();
			}
		}
	}
	
	public static void getFastaCollection(Main mainApp, Analysis analysis) {
		String filePath = getFile(mainApp.getStage());
		if (filePath != null) {
			Task<FastaFile> readTask = new Task<FastaFile>() {
				@Override
				public FastaFile call() throws IOException {
					FastaFile fastaFile = null;
					if (filePath != null) {
						try {
							FastaFileReader fastaFileReader = new FastaFileReader();
							fastaFileReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress((double) newProgress, 1.0));
							//fastaFileReader.getProgressProperty().addListener((obs, oldProgress, newProgress) -> System.out.println(newProgress));
							fastaFileReader.getStatusProperty().addListener((obs, oldStatus, newStatus) -> updateMessage(newStatus));
							// Todo: Select database type
							fastaFile = fastaFileReader.read(filePath, null);
						} catch (IOException e) {
							Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
							alert.showAndWait();
						}
					}
					updateMessage("");
					return fastaFile;
				}
				
				@Override
				protected void failed() {
					super.failed();
					updateMessage("Cancelled.");
					updateProgress(0.0, 1.0);
				}
			};
			readTask.setOnSucceeded(workerStateEvent -> {
				analysis.addFastaFile(readTask.getValue());
				analysis.setDataAssigned(true);
	        });
			readTask.setOnFailed(workerStateEvent -> {
				Alert alert = new Alert(AlertType.ERROR, workerStateEvent.getEventType().toString(), ButtonType.OK);
				alert.showAndWait();
			});
			mainApp.getProgressBar().progressProperty().bind(readTask.progressProperty());
			mainApp.getStatusLabel().textProperty().bind(readTask.messageProperty());
			new Thread(readTask).start();
		}
	}
	
	private static String getDirectory(Stage stage) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Open directory");
		File directory = chooser.showDialog(stage);
		if (directory == null) {
			return null;
		} else {
			return directory.getAbsolutePath();
		}
	}
	
	private static String getFile(Stage stage) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open file");
		File directory = chooser.showOpenDialog(stage);
		if (directory == null) {
			return null;
		} else {
			return directory.getAbsolutePath();
		}
	}
}	