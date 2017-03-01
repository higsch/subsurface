package de.tum.bio.proteomics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.tum.bio.analysis.AnalysisComponent;

public class RawFile implements AnalysisComponent {
	
	private String name;
	private String experiment;
	private List<String> variableModifications;
	
	public RawFile(Map<SummaryTableHeaders, String> properties) {
		name = properties.get(SummaryTableHeaders.RAW_FILE);
		experiment = properties.get(SummaryTableHeaders.EXPERIMENT);
		variableModifications = Arrays.asList(properties.get(SummaryTableHeaders.VARIABLE_MODIFICATIONS).split(";"));
	}
	
	public String getName() {
		return name;
	}
	
	public String getExperimentName() {
		return experiment;
	}
	
	public List<String> getVariableModifications() {
		return variableModifications;
	}

	@Override
	public int getId() {
		return -1;
	}
}
