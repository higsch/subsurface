package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.compomics.util.experiment.biology.Enzyme;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.analysis.AnalysisComponentType;
import de.tum.bio.proteomics.headers.StatisticsTableHeaders;
import de.tum.bio.proteomics.tools.Toolbox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class PeptideId implements AnalysisComponent {
	
	private int id;
	private final String name;
	
	private BooleanProperty sequencesAdded = new SimpleBooleanProperty(false);
	private BooleanProperty statisticsAdded = new SimpleBooleanProperty(false);
	
	private StringProperty selectedProteinGroupId = new SimpleStringProperty(null);
	private StringProperty selectedPeptideId = new SimpleStringProperty(null);
	
	private ObservableMap<String, ProteinGroup> proteins = FXCollections.observableHashMap();
	private ObservableMap<String, Peptide> peptides = FXCollections.observableHashMap();
	
	private Map<Enzyme, Map<String, List<Peptide>>> digestionAssays = new HashMap<>();
	
	private AnalysisSummary summary;
	
	public PeptideId(int id, Map<AnalysisComponentType, List<AnalysisComponent>> data, String name) {
		this.id = id;

		for (AnalysisComponent proteinGroup : data.get(AnalysisComponentType.MaxQuant_ProteinGroups)) {
			proteins.put(((ProteinGroup) proteinGroup).getId(), (ProteinGroup) proteinGroup);
		}
		for (AnalysisComponent peptide : data.get(AnalysisComponentType.MaxQuant_Peptides)) {
			peptides.put(((Peptide) peptide).getId(), (Peptide) peptide);
		}
		
		this.name = "Peptide Identification: " + name;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public ProteinGroup getProteinGroupById(String string) {
		return proteins.get(string);
	}
	
	public Peptide getPeptideById(String id) {
		return peptides.get(id);
	}
	
	public ObservableMap<String, ProteinGroup> getAllProteinGroups() {
		return proteins;
	}
	
	public ObservableMap<String, Peptide> getAllPeptides() {
		return peptides;
	}
	
	public ObservableList<Peptide> getPeptidesByProteinGroupsId(String proteinGroupId) {
		List<Peptide> returnPeptides = new ArrayList<>();
		for (Entry<String, Peptide> entry : peptides.entrySet()) {
			if (Arrays.asList(entry.getValue().getProteinGroupsIds().split(";")).contains(String.valueOf(proteinGroupId))) {
				returnPeptides.add(entry.getValue());
			}
		}
		return FXCollections.observableArrayList(returnPeptides);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean getSequencesAdded() {
		return sequencesAdded.get();
	}
	
	public void setSequencesAdded(boolean sequencesAdded) {
		this.sequencesAdded.set(sequencesAdded);
	}
	
	public BooleanProperty sequencesAddedProperty() {
		return sequencesAdded;
	}
	
	public boolean getStatisticsAdded() {
		return statisticsAdded.get();
	}
	
	public void setStatisticsAdded(boolean statisticsAdded) {
		this.statisticsAdded.set(statisticsAdded);
	}
	
	public BooleanProperty statisticsAddedProperty() {
		return statisticsAdded;
	}
	
	public void setSelectedProteinGroupId(String proteinGroupId) {
		this.selectedProteinGroupId.set(proteinGroupId);
	}
	
	public String getSelectedProteinGroupId() {
		return selectedProteinGroupId.get();
	}
	
	public ProteinGroup getSelectedProteinGroup() {
		return proteins.get(getSelectedProteinGroupId());
	}
	
	public StringProperty selectedProteinGroupIdProperty() {
		return selectedProteinGroupId;
	}
	
	public void setSelectedPeptideId(String id) {
		this.selectedPeptideId.set(id);
	}
	
	public String getSelectedPeptideId() {
		return selectedPeptideId.get();
	}
	
	public Peptide getSelectedPeptide() {
		return peptides.get(getSelectedPeptideId());
	}
	
	public StringProperty selectedPeptideIdProperty() {
		return selectedPeptideId;
	}
	
	public Map<String, Map<StatisticsTableHeaders, Number>> getStatisticsData() {
		Map<String, Map<StatisticsTableHeaders, Number>> dataMap = new HashMap<>();
		for (ProteinGroup proteinGroup : proteins.values()) {
			if (!Double.isNaN(proteinGroup.getLog2Enrichment())) {
				if (!Double.isNaN(proteinGroup.getMinusLog10PValue())) {
					Map<StatisticsTableHeaders, Number> dataPair = new HashMap<>();
					dataPair.put(StatisticsTableHeaders.LOG2_ENRICHMENT, proteinGroup.getLog2Enrichment());
					dataPair.put(StatisticsTableHeaders.MINUS_LOG10_PVALUE, proteinGroup.getMinusLog10PValue());
					dataMap.put(proteinGroup.getId(), dataPair);
				}
			}
		}
		return dataMap;
	}
	
	public void addPeptides(List<String> sequences, String proteinGroupId, Enzyme enzyme) {
		if (!digestionAssays.containsKey(enzyme)) {
			digestionAssays.put(enzyme, new HashMap<String, List<Peptide>>());
		}
		if (!digestionAssays.get(enzyme).containsKey(proteinGroupId)) {
			digestionAssays.get(enzyme).put(proteinGroupId, new ArrayList<Peptide>());
		}
		
		ProteinGroup protein = proteins.get(proteinGroupId); // TODO: optimize storage of digestion assays
		List<Peptide> peptides = new ArrayList<>();
		for (String sequence : sequences) {
			List<List<Integer>> positions = Toolbox.simpleMap(sequence, protein.getSequenceAsString());
			for (List<Integer> position : positions) {
				Peptide peptide = new Peptide(null, sequence, position.get(0), position.get(1), 0);
				peptide.setProteinGroupsIds(String.valueOf(proteinGroupId));
				digestionAssays.get(enzyme).get(proteinGroupId).add(peptide);
			}
		}
		digestionAssays.get(enzyme).get(proteinGroupId).addAll(peptides);
	}
	
	public boolean hasDigestionAssay(Enzyme enzyme, String proteinGroupId) {
		boolean result = false;
		if (digestionAssays.containsKey(enzyme)) {
			if (digestionAssays.get(enzyme).containsKey(proteinGroupId)) {
				if (digestionAssays.get(enzyme).get(proteinGroupId).size() > 0) {
					result = true;
				}
			}
		}
		return result;
	}
	
	public List<Peptide> getInSilicoPeptides(Enzyme enzyme, String proteinGroupId) {
		List<Peptide> peptides = new ArrayList<>();
		if (digestionAssays.containsKey(enzyme)) {
			if (digestionAssays.get(enzyme).containsKey(proteinGroupId)) {
				peptides = digestionAssays.get(enzyme).get(proteinGroupId);
			}
		}
		return peptides;
	}
	
	public void setSummary(AnalysisSummary summary) {
		this.summary = summary;
	}
	
	public AnalysisSummary getSummary() {
		return summary;
	}
	
	public void setModifications(String name, List<AnalysisComponent> modifications) {
		for (AnalysisComponent modification : modifications) {
			if (modification instanceof Modification) {
				for (String peptideId : ((Modification) modification).getPeptideIds()) {
					Peptide peptide = getPeptideById(peptideId);
					if (peptide != null) {
						peptide.addModification((Modification) modification);
					}
				}
			}
		}
	}
}