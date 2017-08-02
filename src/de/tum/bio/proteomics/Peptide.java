package de.tum.bio.proteomics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.PeptidesTableHeaders;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a peptide.
 *
 * @author Matthias Stahl
 */

public class Peptide extends AminoAcidSequence implements AnalysisComponent {

	private String id;
	private IntegerProperty startPosition = new SimpleIntegerProperty();
	private IntegerProperty endPosition = new SimpleIntegerProperty();
	private IntegerProperty msmsCount = new SimpleIntegerProperty();
	private StringProperty msmsIds = new SimpleStringProperty();
	private String evidenceIds;
	private String proteinGroupsIds;
	private LongProperty totalIntensity = new SimpleLongProperty();
	
	private boolean isInSilico = false;
	
	private Map<String, List<Feature>> featureMap;
	private Map<String, Long> experimentIntensities;
	private long maxTotalIntensity;
	private long minTotalIntensity;
	private long maxExperimentIntensity;
	private long minExperimentIntensity;
	
	public Peptide(String id, String sequence, int startPosition, int endPosition, int msmsCount) {
		super(sequence);
		this.id = id;
		setStartPosition(startPosition);
		setEndPosition(endPosition);
		setMsmsCount(msmsCount);
	}
	
	public Peptide(Map<PeptidesTableHeaders, String> properties) {
		super(properties.get(PeptidesTableHeaders.SEQUENCE));
		id = properties.get(PeptidesTableHeaders.ID);
		if (properties.get(PeptidesTableHeaders.START_POSITION).length() > 0) {
			setStartPosition(Integer.parseInt(properties.get(PeptidesTableHeaders.START_POSITION)));
		}
		if (properties.get(PeptidesTableHeaders.END_POSITION).length() > 0) {
			setEndPosition(Integer.parseInt(properties.get(PeptidesTableHeaders.END_POSITION)));
		}
		if (properties.get(PeptidesTableHeaders.MSMSCOUNTS).length() > 0) {
			setMsmsCount(Integer.parseInt(properties.get(PeptidesTableHeaders.MSMSCOUNTS)));
		}
		if (properties.get(PeptidesTableHeaders.MSMSIDS).length() > 0) {
			setMsmsIds(properties.get(PeptidesTableHeaders.MSMSIDS));
		}
		setProteinGroupsIds(properties.get(PeptidesTableHeaders.PROTEINGROUPS_IDS));
		setEvidenceIds(properties.get(PeptidesTableHeaders.EVIDENCE_IDS));
		if (properties.get(PeptidesTableHeaders.INTENSITY).length() > 0) {
			setTotalIntensity(Long.parseLong(properties.get(PeptidesTableHeaders.INTENSITY)));
		}
	}
	
	public String getId() {
		return id;
	}
	
	public void setStartPosition(int startPosition) {
		this.startPosition.set(startPosition);
	}
	
	public int getStartPosition() {
		return startPosition.get();
	}
	
	public IntegerProperty startPositionProperty() {
		return startPosition;
	}
	
	public void setEndPosition(int endPosition) {
		this.endPosition.set(endPosition);
	}
	
	public int getEndPosition() {
		return endPosition.get();
	}
	
	public IntegerProperty endPositionProperty() {
		return endPosition;
	}
	
	public void setMsmsCount(int msmsCount) {
		this.msmsCount.set(msmsCount);
	}
	
	public int getMsmsCount() {
		return msmsCount.get();
	}
	
	public IntegerProperty msmsCountProperty() {
		return msmsCount;
	}
	
	public void setMsmsIds(String msmsIds) {
		this.msmsIds.set(msmsIds);
	}
	
	public String getMsmsIds() {
		return msmsIds.get();
	}
	
	public StringProperty msmsIdsProperty() {
		return msmsIds;
	}
	
	public void setEvidenceIds(String evidenceIds) {
		this.evidenceIds = evidenceIds;
	}
	
	public String getEvidenceIds() {
		return evidenceIds;
	}
	
	public void setProteinGroupsIds(String proteinGroupsIds) {
		this.proteinGroupsIds = proteinGroupsIds;
	}
	
	public String getProteinGroupsIds() {
		return proteinGroupsIds;
	}
	
	public void setFeatureMap(Map<String, List<Feature>> featureMap) {
		this.featureMap = featureMap;
	}
	
	public Map<String, List<Feature>> getFeatureMap() {
		return featureMap;
	}
	
	public List<Feature> getFeaturesByExperiment(String experiment) {
		return featureMap.get(experiment);
	}
	
	public void setExperimentIntensities(Map<String, Long> experimentIntensities) {
		this.experimentIntensities = experimentIntensities;
		setMaxExperimentIntensity(calculateMaxExperimentIntensity(experimentIntensities));
	}
	
	public Map<String, Long> getExperimentIntensities() {
		return experimentIntensities;
	}
	
	public long getTotalFeatureIntensityByExperiment(String experiment) {
		return experimentIntensities.get(experiment);
	}
	
	public void setTotalIntensity(long totalIntensity) {
		this.totalIntensity.set(totalIntensity);
	}
	
	public long getTotalIntensity() {
		return totalIntensity.get();
	}
	
	public LongProperty totalIntensityProperty() {
		return totalIntensity;
	}
	
	public void isInSilico(boolean value) {
		this.isInSilico = value;
	}
	
	public boolean isInSilico() {
		return isInSilico;
	}
	
	public void setMaxTotalIntensity(long maxTotalIntensity) {
		this.maxTotalIntensity = maxTotalIntensity;
	}
	
	public long getMaxTotalIntensity() {
		return maxTotalIntensity;
	}
	
	public void setMinTotalIntensity(long minTotalIntensity) {
		this.minTotalIntensity = minTotalIntensity;
	}
	
	public long getMinTotalIntensity() {
		return minTotalIntensity;
	}
	
	public void setMaxExperimentIntensity(long maxExperimentIntensity) {
		this.maxExperimentIntensity = maxExperimentIntensity;
	}
	
	public long getMaxExperimentIntensity() {
		return maxExperimentIntensity;
	}
	
	public void setMinExperimentIntensity(long minExperimentIntensity) {
		this.minExperimentIntensity = minExperimentIntensity;
	}
	
	public long getMinExperimentIntensity() {
		return minExperimentIntensity;
	}
	
	public void addModification(Modification modification) {
		super.assignModificationToAminoAcid(modification, modification.getPositionInPeptide());
	}
	
	public void removeModification(Modification modification, int position) {
		super.removeModificationFromAminoAcid(modification, position);
	}
	
	public Map<Integer, Modification> getModifications() {
		return super.getModifications();
	}
	
	private long calculateMaxExperimentIntensity(Map<String, Long> experimentIntensities) {
		long max = 0;
		for (Entry<String, Long> entry : experimentIntensities.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
			}
		}
		return max;
	}
}