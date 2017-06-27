package de.tum.bio.proteomics.analysis;

import java.util.HashMap;
import java.util.Set;

import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.StatisticsFile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class Analysis implements AnalysisComponent {
	
	private int id;
	private String name;
	
	private ObservableMap<Integer, PeptideId> peptideIdentifications = FXCollections.observableMap(new HashMap<Integer, PeptideId>());
	private ObservableMap<Integer, StatisticsFile> statisticsFiles = FXCollections.observableMap(new HashMap<Integer, StatisticsFile>());
	private ObservableMap<Integer, FastaFile> fastaFiles = FXCollections.observableMap(new HashMap<Integer, FastaFile>());
	
	private BooleanProperty dataAssignedProperty = new SimpleBooleanProperty(false);
	
	private IntegerProperty selectedPeptideIdId = new SimpleIntegerProperty(-1);
	
	public Analysis(int id) {
		this.id = id;
		this.name = "Analysis_" + Integer.toString(id);
	}
	
	public Analysis(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addPeptideId(PeptideId peptideId) {
		int id = getNextId(peptideIdentifications.keySet());
		if (peptideId != null) {
			peptideId.setId(id);
			peptideIdentifications.put(id, peptideId);
		}
	}
	
	public void addStatisticsFile(StatisticsFile statisticsFile) {
		int id = getNextId(statisticsFiles.keySet());
		if (statisticsFile != null) {
			statisticsFile.setId(id);
			statisticsFiles.put(id, statisticsFile);
		}
	}
	
	public void addFastaFile(FastaFile fastaFile) {
		int id = getNextId(fastaFiles.keySet());
		if (fastaFile != null) {
			fastaFile.setId(id);
			fastaFiles.put(id, fastaFile);
		}
	}
	
	public void removePeptideId(int id) {
		peptideIdentifications.remove(id);
	}
	
	public void removeStatisticsFile(int id) {
		statisticsFiles.remove(id);
	}
	
	public void removeFastaFile(int id) {
		fastaFiles.remove(id);
	}
	
	public ObservableMap<Integer, PeptideId> getPeptideIds() {
		return peptideIdentifications;
	}
	
	public PeptideId getPeptideId(int id) {
		return peptideIdentifications.get(id);
	}
	
	public PeptideId getPeptideId() {
		return peptideIdentifications.get(getSelectedPeptideIdId());
	}
	
	public ObservableMap<Integer, StatisticsFile> getStatisticsFiles() {
		return statisticsFiles;
	}
	
	public StatisticsFile getStatisticsFile(int id) {
		return statisticsFiles.get(id);
	}
	
	public ObservableMap<Integer, FastaFile> getFastaFiles() {
		return fastaFiles;
	}
	
	public FastaFile getFastaFile(int id) {
		return fastaFiles.get(id);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public void setDataAssigned(boolean value) {
		dataAssignedProperty.set(value);
	}
	
	public boolean getDataAssigned() {
		return dataAssignedProperty.get();
	}
	
	public BooleanProperty dataAssignedProperty() {
		return dataAssignedProperty;
	}
	
	public void setSelectedPeptideIdId(int id) {
		this.selectedPeptideIdId.set(id);
	}
	
	public int getSelectedPeptideIdId() {
		return selectedPeptideIdId.get();
	}
	
	public IntegerProperty selectedPeptideIdIdProperty() {
		return selectedPeptideIdId;
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
