package de.tum.bio.proteomics.io.statistics;

import java.io.IOException;
import java.util.Map;

import de.tum.bio.proteomics.StatisticsFile;
import de.tum.bio.proteomics.headers.StatisticsTableHeaders;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface StatisticsReader {
	public StatisticsFile read(String filePath, Map<StatisticsTableHeaders, String> headerMap) throws IOException;
	public ReadOnlyDoubleProperty getProgressProperty();
	public ReadOnlyStringProperty getStatusProperty();
}