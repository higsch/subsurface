package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.tum.bio.analysis.AnalysisComponent;
import de.tum.bio.utils.AlphanumComparator;

public class AnalysisSummary implements AnalysisComponent {
	
	private List<RawFile> rawFiles = new ArrayList<>();
	private List<String> variableModifications = null;
	
	public AnalysisSummary(List<Map<SummaryTableHeaders, String>> propertiesList) {
		for (Map<SummaryTableHeaders, String> properties : propertiesList) {
			if (properties.get(SummaryTableHeaders.EXPERIMENT).length() > 0) {
				rawFiles.add(new RawFile(properties));
			}
		}
		
		Collections.sort(rawFiles, new AlphanumComparator<RawFile>());
	}
	
	public List<String> getRawFileNames() {
		List<String> rawFileNames = new ArrayList<>();
		
		for (RawFile rawFile : rawFiles) {
			rawFileNames.add(rawFile.getName());
		}
		
		return rawFileNames;
	}
	
	public List<String> getExperimentNames() {
		List<String> experiments = new ArrayList<>();
		
		for (RawFile rawFile : rawFiles) {
			if (!experiments.contains(rawFile.getExperimentName())) {
				experiments.add(rawFile.getExperimentName());
			}
		}
		
		return experiments;
	}
	
	public List<String> getVariableModifications() {
		if (variableModifications == null) {
			variableModifications = new ArrayList<>();
			for (RawFile rawFile : rawFiles) {
				for (String variableModification : rawFile.getVariableModifications()) {
					if (!variableModifications.contains(variableModification)) {
						variableModifications.add(variableModification);
					}
				}
			}
		}
		
		return variableModifications;
	}

	@Override
	public int getId() {
		return -1;
	}

}