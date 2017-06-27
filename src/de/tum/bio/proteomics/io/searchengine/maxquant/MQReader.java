package de.tum.bio.proteomics.io.searchengine.maxquant;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.TableHeaders;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface MQReader {
	public boolean fileExists(String txtDirectory, String prefix);
	public <E extends Enum<E> & TableHeaders> List<AnalysisComponent> read(String txtDirectory, Map<E, String> headerMap) throws IOException;
	public ReadOnlyDoubleProperty getProgressProperty();
	public ReadOnlyStringProperty getStatusProperty();
}