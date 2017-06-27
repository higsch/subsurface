package de.tum.bio.proteomics.io.perseus;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.StatisticsFile;
import de.tum.bio.proteomics.headers.StatisticsTableHeaders;
import de.tum.bio.proteomics.io.StatisticsReader;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class reads a statistical analysis.
 * @author Matthias Stahl
 *
 */

public final class PerseusFileReader extends PerseusTablesIO implements StatisticsReader {
	
	DoubleProperty progressProperty = new SimpleDoubleProperty();
	StringProperty statusProperty = new SimpleStringProperty();
	
	public PerseusFileReader() {
		// empty
	}
	
	/**
	 * Reads statistics file and generate StatisticsFile object.
	 * @param filePath
	 * @param headerMap
	 * @param id
	 * @return StatisticsFile
	 * @throws IOException
	 */
	@Override
	public StatisticsFile read(String filePath, Map<StatisticsTableHeaders, String> headerMap) throws IOException {
		super.initBiMaps();
		StatisticsFile statisticsFile = null;
		try {
			Path path = Paths.get(filePath);
			SeparatedTextReader textReader = new SeparatedTextReader();
			progressProperty.bind(textReader.getProgressProperty());
			statusProperty.bind(textReader.getStatusProperty());
			List<HashMap<String, String>> contentList = textReader.readFile(path, headerMap, "\t");
			progressProperty.unbind();
			statusProperty.unbind();
			setStatusProperty("Parse Perseus file...");
			statisticsFile = new StatisticsFile(-1, resolveProteinIds(contentList, headerMap), path.getFileName().toString());
		} catch (IOException e) {
			throw e;
		}
		return statisticsFile;
	}
	
	/**
	 * Changes the data structure of the initial tab text reader result to an understandable version for StatisticsFile generation.
	 * @param contentList
	 * @return map
	 */
	private static Map<String, Map<StatisticsTableHeaders, Double>> resolveProteinIds(List<HashMap<String, String>> contentList, Map<StatisticsTableHeaders, String> headerMap) {
		Map<String, Map<StatisticsTableHeaders, Double>> resolvedMap = new HashMap<>();
		for (Map<String, String> listEntry : contentList) {
			String name = null;
			Map<StatisticsTableHeaders, Double> tmp = new HashMap<>(2);
			for (Entry<String, String> mapEntry : listEntry.entrySet()) {
				if (mapEntry.getKey().equals(headerMap.get(StatisticsTableHeaders.NAME))) {
					name = mapEntry.getValue();
				}
				if (mapEntry.getKey().equals(headerMap.get(StatisticsTableHeaders.LOG2_ENRICHMENT))) {
					tmp.put(StatisticsTableHeaders.LOG2_ENRICHMENT, Double.parseDouble(mapEntry.getValue()));
				}
				if (mapEntry.getKey().equals(headerMap.get(StatisticsTableHeaders.MINUS_LOG10_PVALUE))) {
					tmp.put(StatisticsTableHeaders.MINUS_LOG10_PVALUE, Double.parseDouble(mapEntry.getValue()));
				}
			}
			resolvedMap.put(name, new HashMap<>(tmp));
		}
		return resolvedMap;
	}
	
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progressProperty;
	}
	
	@SuppressWarnings("unused")
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