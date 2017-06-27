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

import de.tum.bio.proteomics.Peptide;
import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.PeptidesTableHeaders;
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

public class MQPeptidesReader extends MQTablesIO implements MQReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	public MQPeptidesReader() {
		// empty
	}
	
	@Override
	public boolean fileExists(String txtDirectory, String prefix) {
		return Files.exists(getPath(txtDirectory, prefix));
	};

	/**
	 * Fetches list with mapped header value pairs.
	 * @param directory
	 * @return list of Peptides
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E> & TableHeaders> List<AnalysisComponent> read(String txtDirectory, Map<E, String> headerMap) throws IOException {
		List<HashMap<String, String>> contentList = null;
		List<AnalysisComponent> peptidesList = null;
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
			peptidesList = createListOfPeptides(contentList);
		} catch (IOException e) {
			throw e;
		}
		setProgressProperty(0.0);
		setStatusProperty("");
		return peptidesList;
	}
	
	private Path getPath(String txtDirectory, String prefix) {
		return Paths.get(txtDirectory + FileSystems.getDefault().getSeparator() + prefix + FILENAME_PEPTIDES);
	}

	/**
	 * Creates list of new Peptides.
	 * @param contentList
	 * @return list of Peptides
	 */
	private List<AnalysisComponent> createListOfPeptides(List<HashMap<String, String>> contentList) {
		List<AnalysisComponent> peptideList = new ArrayList<>();
		// Go through each protein hashmap and create new ProteinGroup object
		long numberOfEntries = contentList.size();
		long index = 1;
		setStatusProperty("Parse peptides...");
		for (Map<String, String> properties : contentList) {
			setProgressProperty((double) index/numberOfEntries);
			Map<PeptidesTableHeaders, String> standardizedProperties = standardizeProperties(properties);
			peptideList.add(Integer.parseInt(standardizedProperties.get(PeptidesTableHeaders.ID)), new Peptide(standardizedProperties));
			index++;
		}
		return peptideList;
	}
	
	/**
	 * Standardizes the MaxQuant table headers to general enum headers.
	 * @param properties
	 * @return standardized properties
	 */
	private Map<PeptidesTableHeaders, String> standardizeProperties(Map<String, String> properties) {
		Map<PeptidesTableHeaders, String> standardizedContentList = new HashMap<PeptidesTableHeaders, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			standardizedContentList.put(PeptidesHeadersMap.inverse().get(entry.getKey()), (String) entry.getValue());
		}
		return standardizedContentList;
	}
	
	/**
	 * Gets standard table headers.
	 * @return map of standard table headers
	 */
	private Map<PeptidesTableHeaders, String> initStandardHeaders() {
		return PEPTIDES_HEADERS_UNI;
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
