package de.tum.bio.proteomics;

import java.io.IOException;
import java.util.Map;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface StatisticsReader {
	public StatisticsFile read(String filePath, Map<StatisticsTableHeaders, String> headerMap) throws IOException;
	public ReadOnlyDoubleProperty getProgressProperty();
	public ReadOnlyStringProperty getStatusProperty();
}