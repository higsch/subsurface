package de.tum.bio.proteomics.analysis;

import java.util.HashMap;
import java.util.Set;

import application.Main;
import application.MainController;
import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.StatisticsFile;
import de.tum.bio.proteomics.analysis.gui.AnalysisComponentOpener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * This class handles a set different analyses components.
 * @author Matthias Stahl
 *
 */

public final class AnalysisHandler {
	
	private static final AnalysisHandler analysisHandler = new AnalysisHandler();
	
	private ObservableMap<Integer, Analysis> analysisCollection = FXCollections.observableMap(new HashMap<Integer, Analysis>());
	private MainController controller;
	
	private IntegerProperty selectedAnalysisId = new SimpleIntegerProperty(-1);
	
	private AnalysisHandler() {
		// empty
	}
	
	public static AnalysisHandler getInstance() {
		return analysisHandler;
	}
	
	public void setController(MainController controller) {
		this.controller = controller;
		analysisCollection.addListener(new MapChangeListener<Integer, Analysis>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends Integer, ? extends Analysis> change) {
				controller.buildTreeView();
			}
		});
	}
	
	public void addItem(int analysisId, AnalysisComponentType analysisComponentType, Main mainApp) {
		Analysis analysis;
		boolean newAnalysis = false;
		
		if (analysisId < 0) {
			analysisId = getNextId(analysisCollection.keySet());
		}
		
		if (!analysisCollection.containsKey(analysisId)) {
			analysis = new Analysis(analysisId);
			analysis.getPeptideIds().addListener(new MapChangeListener<Integer, PeptideId>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends PeptideId> change) {
					controller.buildTreeView();
				}
			});
			analysis.getStatisticsFiles().addListener(new MapChangeListener<Integer, StatisticsFile>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends StatisticsFile> change) {
					controller.buildTreeView();
				}
			});
			analysis.getFastaFiles().addListener(new MapChangeListener<Integer, FastaFile>() {
				@Override
				public void onChanged(MapChangeListener.Change<? extends Integer, ? extends FastaFile> change) {
					controller.buildTreeView();
				}
			});
			newAnalysis = true;
		} else {
			analysis = analysisCollection.get(analysisId);
		}
		
		if (analysisComponentType == null) {
			// Only new empty analysis should be created
			analysisCollection.put(analysisId, analysis);
		} else {
			analysis.setDataAssigned(false);
			if (newAnalysis) {
				analysis.dataAssignedProperty().addListener((c, o, n) -> {
					analysisCollection.put(analysis.getId(), analysis);
				});
			}
			
			switch (analysisComponentType) {
				case MaxQuant:
					AnalysisComponentOpener.getMQCollection(mainApp, analysis);
					break;
				case Perseus:
					AnalysisComponentOpener.getPerseusCollection(mainApp, analysis);
					break;
				case Fasta:
					AnalysisComponentOpener.getFastaCollection(mainApp, analysis);
					break;
				case MzIdentML:
					AnalysisComponentOpener.getMzIdentMLCollection(mainApp, analysis);
					break;
				default:
					break;
			}
		}
	}
	
	public void newAnalysis() {
		addItem(-1, null, null);
	}
	
	public void removeItem(int analysisId, int itemId, AnalysisComponentType analysisComponentType) {
		switch (analysisComponentType) {
			case Analysis:
				analysisCollection.remove(analysisId);
				break;
			case PeptideId:
				analysisCollection.get(analysisId).removePeptideId(itemId);
				break;
			case Statistics:
				analysisCollection.get(analysisId).removeStatisticsFile(itemId);
				break;
			case Fasta:
				analysisCollection.get(analysisId).removeFastaFile(itemId);
				break;
			default:
				break;
		}
	}
	
	public ObservableMap<Integer, Analysis> getAllAnalyses() {
		return analysisCollection;
	}
	
	public Analysis getAnalysis(int id) {
		return analysisCollection.get(id);
	}
	
	public Analysis getAnalysis() {
		return analysisCollection.get(selectedAnalysisId.get());
	}
	
	public boolean isEmpty() {
		return analysisCollection.isEmpty();
	}
	
	public void setSelectedAnalysisId(int id) {
		this.selectedAnalysisId.set(id);
	}
	
	public int getSelectedAnalysisId() {
		return selectedAnalysisId.get();
	}
	
	public IntegerProperty selectedAnalysisIdProperty() {
		return selectedAnalysisId;
	}
	
	private int getNextId(Set<Integer> keys) {
		if (keys == null) {
			return 0;
		}
		if (keys.isEmpty()) {
			return 0;
		}
		Integer prevKey = -1;
		for (Integer key : keys) {
			if (key - prevKey != 1) {
				return key - 1;
			}
			prevKey = key;
		}
		return prevKey + 1;
 	}
}