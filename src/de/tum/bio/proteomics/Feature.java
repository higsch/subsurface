package de.tum.bio.proteomics;

import java.util.Map;

import de.tum.bio.analysis.AnalysisComponent;

public class Feature implements AnalysisComponent {
	
	private int id;
	private long intensity = 0;
	private String experiment;
	private int numberOfDataPoints = 0;
	private int peptideId;
	private int modPeptideId;
	

	public Feature(Map<EvidenceTableHeaders, String> evidences) {
		id = Integer.parseInt(evidences.get(EvidenceTableHeaders.ID));
		if (evidences.get(EvidenceTableHeaders.INTENSITY).length() > 0) {
			intensity = Long.parseLong(evidences.get(EvidenceTableHeaders.INTENSITY));
		}
		experiment = evidences.get(EvidenceTableHeaders.EXPERIMENT);
		if (evidences.get(EvidenceTableHeaders.NUMBER_OF_DATA_POINTS).length() > 0) {
			numberOfDataPoints = Integer.parseInt(evidences.get(EvidenceTableHeaders.NUMBER_OF_DATA_POINTS));
		}
		if (evidences.get(EvidenceTableHeaders.PEPTIDE_ID).length() > 0) {
			peptideId = Integer.parseInt(evidences.get(EvidenceTableHeaders.PEPTIDE_ID));
		}
		if (evidences.get(EvidenceTableHeaders.MOD_PEPTIDE_ID).length() > 0) {
			modPeptideId = Integer.parseInt(evidences.get(EvidenceTableHeaders.MOD_PEPTIDE_ID));
		}
	}

	@Override
	public int getId() {
		return id;
	}
	
	public long getIntensity() {
		return intensity;
	}
	
	public String getExperiment() {
		return experiment;
	}
	
	public int getNumberOfDataPoints() {
		return numberOfDataPoints;
	}
	
	public int getPeptideId() {
		return peptideId;
	}
	
	public int getModPeptideId() {
		return modPeptideId;
	}
	
}
