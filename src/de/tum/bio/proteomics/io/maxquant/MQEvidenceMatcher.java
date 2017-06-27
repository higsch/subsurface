package de.tum.bio.proteomics.io.maxquant;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.Feature;
import de.tum.bio.proteomics.Peptide;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.headers.EvidenceTableHeaders;
import de.tum.bio.proteomics.headers.TableHeaders;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MQEvidenceMatcher extends MQTablesIO {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	public MQEvidenceMatcher() {
		// empty
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E> & TableHeaders> void match(PeptideId peptideId, String txtDirectory, Map<E, String> headerMap) throws IOException {
		Map<Integer, Peptide> peptides = peptideId.getAllPeptides();
		
		if (headerMap == null) {
			headerMap = (Map<E, String>) initStandardHeaders();
		}
		List<Map<EvidenceTableHeaders, String>> evidenceList = read(txtDirectory, headerMap);
		if (evidenceList != null) {
			Map<String, List<Feature>> featureMap = new HashMap<>();
			long index = 0;
			long numberOfPeptides = peptides.size();
			setStatusProperty("Reading feature information...");
			for (Peptide peptide : peptides.values()) {
				setProgressProperty((double) index/numberOfPeptides);
				if (peptide.getEvidenceIds().length() > 0) {
					String[] evidenceIds = peptide.getEvidenceIds().split(";");
					List<Feature> featureList = new ArrayList<>();
					for (String evidenceId : evidenceIds) {
						featureList.add(new Feature(evidenceList.get(Integer.parseInt(evidenceId))));
					}
					Map<String, Long> experimentIntensities = new HashMap<>();
					for (Feature feature : featureList) {
						if (!featureMap.containsKey(feature.getExperiment())) {
							featureMap.put(feature.getExperiment(), new ArrayList<Feature>());
						}
						if (!experimentIntensities.containsKey(feature.getExperiment())) {
							experimentIntensities.put(feature.getExperiment(), feature.getIntensity());
						} else {
							long previousIntensity = experimentIntensities.get(feature.getExperiment());
							experimentIntensities.put(feature.getExperiment(), previousIntensity + feature.getIntensity());
						}
						featureMap.get(feature.getExperiment()).add(feature);
					}
					peptide.setFeatureMap(featureMap);
					peptide.setExperimentIntensities(experimentIntensities);
				} else {
					System.out.println("No evidences found for peptide sequence: " + peptide.getSequenceAsString());
				}
				index++;
			}
			setProgressProperty(0.0);
		}
	}

	private <E extends Enum<E> & TableHeaders> List<Map<EvidenceTableHeaders, String>> read(String txtDirectory, Map<E, String> headerMap) throws IOException {
		List<HashMap<String, String>> contentList = null;
		Path path = Paths.get(txtDirectory + FileSystems.getDefault().getSeparator() + FILENAME_EVIDENCE);
		SeparatedTextReader textReader = new SeparatedTextReader();
		progressProperty.bind(textReader.getProgressProperty());
		statusProperty.bind(textReader.getStatusProperty());
		try {
			contentList = textReader.readFile(path, headerMap, "\t");
		} catch (IOException ioe) {
			throw ioe;
		}
		progressProperty.unbind();
		statusProperty.unbind();
		List<Map<EvidenceTableHeaders, String>> evidenceList = new ArrayList<>();
		for (Map<String, String> properties : contentList) {
			Map<EvidenceTableHeaders, String> standardizedProperties = standardizeProperties(properties);
			evidenceList.add(Integer.parseInt(standardizedProperties.get(EvidenceTableHeaders.ID)), standardizedProperties);
		}
		return evidenceList;
	}
	
	private Map<EvidenceTableHeaders, String> standardizeProperties(Map<String, String> properties) {
		Map<EvidenceTableHeaders, String> standardizedContentList = new HashMap<EvidenceTableHeaders, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			if (EvidenceHeadersMap.containsValue(entry.getKey())) {
				standardizedContentList.put(EvidenceHeadersMap.inverse().get(entry.getKey()), (String) entry.getValue());
			}
		}
		return standardizedContentList;
	}
	
	private Map<EvidenceTableHeaders, String> initStandardHeaders() {
		return EVIDENCE_HEADERS_UNI;
	}
	
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progressProperty;
	}
	
	private void setProgressProperty(double value) {
		progressProperty.set(value);
	}
	
	public ReadOnlyStringProperty getStatusProperty() {
		return statusProperty;
	}
	
	private void setStatusProperty(String status) {
		statusProperty.set(status);
	}
	
}
