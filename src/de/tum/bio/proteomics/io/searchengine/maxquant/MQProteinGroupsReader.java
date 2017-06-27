package de.tum.bio.proteomics.io.searchengine.maxquant;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.ProteinGroupsTableHeaders;
import de.tum.bio.proteomics.headers.TableHeaders;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Reader for MaxQuant proteinGroups files.
 *
 * @author Matthias Stahl
 */

public class MQProteinGroupsReader extends MQTablesIO implements MQReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	public MQProteinGroupsReader() {
		// empty
	}
	
	@Override
	public boolean fileExists(String txtDirectory, String prefix) {
		return Files.exists(getPath(txtDirectory, prefix));
	}
	
	/**
	 * Fetches data of new protein groups and builds a list of ProteinGroup objects.
	 * @param txtDirectory
	 * @param headerMap
	 * @return list of ProteinGroup
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E> & TableHeaders> List<AnalysisComponent> read(String txtDirectory, Map<E, String> headerMap) throws IOException {
		initBiMaps();
		List<HashMap<String, String>> contentList = null;
		List<AnalysisComponent> proteinGroupsList = null;
		if (headerMap == null) {
			headerMap = (Map<E, String>) initStandardHeaders();
		}
		try {
			Path path = getPath(txtDirectory, "");
			SeparatedTextReader textReader = new SeparatedTextReader();
			progressProperty.bind(textReader.getProgressProperty());
			statusProperty.bind(textReader.getStatusProperty());
			contentList = textReader.readFile(path, headerMap, "\t");
			progressProperty.unbind();
			statusProperty.unbind();
			proteinGroupsList = createListOfProteinGroups(contentList);
		} catch (IOException ioe) {
			System.out.println(ioe);
			throw ioe;
		}
		setProgressProperty(0.0);
		setStatusProperty("");
		return proteinGroupsList;
	}
	
	private Path getPath(String txtDirectory, String prefix) {
		return Paths.get(txtDirectory + FileSystems.getDefault().getSeparator() + prefix + FILENAME_PROTEINGROUPS);
	}

	/**
	 * Creates list of new ProteinGroups.
	 * @param contentList
	 * @return list of ProteinGroups
	 */
	private List<AnalysisComponent> createListOfProteinGroups(List<HashMap<String, String>> contentList) {
		List<AnalysisComponent> proteinList = new ArrayList<>();
		// Go through each protein hashmap and create new ProteinGroup object
		long numberOfEntries = contentList.size();
		long index = 1;
		setStatusProperty("Parse protein groups...");
		for (Map<String, String> properties : contentList) {
			setProgressProperty((double) index/numberOfEntries);
			Map<ProteinGroupsTableHeaders, String> standardizedProperties = standardizeProperties(properties);
			proteinList.add(Integer.parseInt(standardizedProperties.get(ProteinGroupsTableHeaders.ID)), new ProteinGroup(standardizedProperties));
			index++;
		}
		return proteinList;
	}
	
	/**
	 * Standardizes the MaxQuant table headers to general enum headers.
	 * @param properties
	 * @return standardized properties
	 */
	private Map<ProteinGroupsTableHeaders, String> standardizeProperties(Map<String, String> properties) {
		Map<ProteinGroupsTableHeaders, String> standardizedContentList = new HashMap<ProteinGroupsTableHeaders, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			if (ProteinGroupsHeadersMap.containsValue(entry.getKey())) {
				standardizedContentList.put(ProteinGroupsHeadersMap.inverse().get(entry.getKey()), (String) entry.getValue());
			}
		}
		return standardizedContentList;
	}
	
	/**
	 * Gets standard table headers.
	 * @return map of standard table headers
	 */
	private Map<ProteinGroupsTableHeaders, String> initStandardHeaders() {
		return PROTEINGROUPS_HEADERS_UNI;
	}
	
	@Override
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progressProperty;
	}
	
	private void setProgressProperty(double value) {
		progressProperty.set(value);
	}
	
	@Override
	public ReadOnlyStringProperty getStatusProperty() {
		return statusProperty;
	}
	
	private void setStatusProperty(String status) {
		statusProperty.set(status);
	}
}
