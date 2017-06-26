package de.tum.bio.proteomics.maxquant;

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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tum.bio.analysis.AnalysisComponent;
import de.tum.bio.proteomics.Modification;
import de.tum.bio.proteomics.ModificationsTableHeaders;
import de.tum.bio.proteomics.TableHeaders;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Reader for MaxQuant modifications files.
 *
 * @author Matthias Stahl
 */

public class MQModificationsReader extends MQTablesIO implements MQReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	private String modification;
	
	private BiMap<ModificationsTableHeaders, String> tmpBiMap;
	
	public MQModificationsReader(String modification) {
		this.modification = modification;
	}
	
	public boolean fileExists(String txtDirectory, String appendix) {
		return Files.exists(getPath(txtDirectory, modification));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E> & TableHeaders> List<AnalysisComponent> read(String txtDirectory, Map<E, String> headerMap) throws IOException {
		List<HashMap<String, String>> contentList = null;
		List<AnalysisComponent> modificationsList = null;
		if (headerMap == null) {
			headerMap = new HashMap<>((HashMap<E, String>) getStandardHeaders());
		}
		
		// Update sequence window column name
		headerMap.replace((E) ModificationsTableHeaders.SEQUENCE_WINDOW, modification + " " + getStandardHeaders().get(ModificationsTableHeaders.SEQUENCE_WINDOW));
		tmpBiMap = HashBiMap.create(getStandardHeaders());
		tmpBiMap.replace(ModificationsTableHeaders.SEQUENCE_WINDOW, modification + " " + getStandardHeaders().get(ModificationsTableHeaders.SEQUENCE_WINDOW));
		
		try {
			Path path = getPath(txtDirectory, modification);
			SeparatedTextReader textReader = new SeparatedTextReader();
			progressProperty.bind(textReader.getProgressProperty());
			statusProperty.bind(textReader.getStatusProperty());
			contentList = textReader.readFile(path, headerMap, "\t");
			progressProperty.unbind();
			statusProperty.unbind();
			modificationsList = createListOfModifications(contentList);
		} catch (IOException e) {
			throw e;
		}
		setProgressProperty(0.0);
		setStatusProperty("");
		return modificationsList;
	}
	
	private Path getPath(String txtDirectory, String modification) {
		Path path = Paths.get(txtDirectory + FileSystems.getDefault().getSeparator() + modification + FILEAPPENDIX_MODIFICATIONS);
		return path;
	}

	private List<AnalysisComponent> createListOfModifications(List<HashMap<String, String>> contentList) {
		List<AnalysisComponent> modificationsList = new ArrayList<>();
		// Go through each protein hashmap and create new object
		long numberOfEntries = contentList.size();
		long index = 1;
		setStatusProperty("Parse modifications...");
		for (Map<String, String> properties : contentList) {
			setProgressProperty((double) index/numberOfEntries);
			Map<ModificationsTableHeaders, String> standardizedProperties = standardizeProperties(properties);
			modificationsList.add(Integer.parseInt(standardizedProperties.get(ModificationsTableHeaders.ID)), new Modification(modification, standardizedProperties));
			index++;
		}
		return modificationsList;
	}
	
	private Map<ModificationsTableHeaders, String> standardizeProperties(Map<String, String> properties) {
		Map<ModificationsTableHeaders, String> standardizedContentList = new HashMap<ModificationsTableHeaders, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			standardizedContentList.put(tmpBiMap.inverse().get(entry.getKey()), entry.getValue());
		}
		return standardizedContentList;
	}
	
	private Map<ModificationsTableHeaders, String> getStandardHeaders() {
		return MODIFICATIONS_HEADERS_UNI;
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
	
	public String getModificationName() {
		return modification;
	}
}