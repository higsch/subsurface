package de.tum.bio.proteomics.io;

import java.io.IOException;

import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.io.FastaFileReader.DatabaseType;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface FastaReader {
	public FastaFile read(String filePath, DatabaseType databaseType) throws IOException;
	public ReadOnlyDoubleProperty getProgressProperty();
	public ReadOnlyStringProperty getStatusProperty();
}