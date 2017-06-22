package de.tum.bio.proteomics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Deserializes FASTA files.
 *
 * @author Matthias Stahl
 */

public class FastaFileReader implements FastaReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	public static enum DatabaseType {
			UniProt(">.*?\\|((?:[a-z][a-z]*[0-9]+[a-z0-9]*))\\|.*?", ">");
		
			String regExHeader;
			String rowStarter;
			private DatabaseType(String regExHeader, String rowStarter) {
				this.regExHeader = regExHeader;
				this.rowStarter = rowStarter;
			}
			
			private String getRegExHeader() {
				return regExHeader;
			}
			
			private String getRowStarter() {
				return rowStarter;
			}
		};
	
	
	public FastaFile read(String filePath, DatabaseType databaseType) throws IOException {
		Path path = Paths.get(filePath);
		String fastaString = null;
		BufferedReader reader = null;
		try {
			setStatusProperty("Open fasta...");
			setProgressProperty(-1.0);
			StringBuilder tmp = new StringBuilder();
			String line = null;
			reader = Files.newBufferedReader(path);
			long numberOfLines = Files.lines(path).count();
			long lineCount = 1;
			setStatusProperty("Read fasta...");
			while ((line = reader.readLine()) != null) {
				setProgressProperty(((double) lineCount/numberOfLines));
				tmp.append(line + "\n");
				lineCount++;
			}
			fastaString = tmp.toString();
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw e;
			    }
			}
		}		
		if (fastaString != null) {
			setStatusProperty("Read headers...");
			setProgressProperty(-1.0);
			Map<String, String> headersAndSequences = getHeadersAndSequences(fastaString, databaseType);
			setStatusProperty("Identify IDs...");
			Map<String, String> idsAndSequences = getIdsAndSequences(headersAndSequences, databaseType);
			setStatusProperty("Done.");
			setProgressProperty(0.0);
			return new FastaFile(idsAndSequences, headersAndSequences, databaseType, -1, path.getFileName().toString());
		} else {
			return null;
		}
	}
	
	private Map<String, String> getHeadersAndSequences(String fastaString, DatabaseType databaseType) {
		Map<String, String> result = new HashMap<String, String>();
		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder sequenceBuilder = new StringBuilder();
		
		if (!fastaString.isEmpty()) {
			if (databaseType == null) {
				databaseType = DatabaseType.UniProt;
			}
			String[] fastaArray = fastaString.split("\n", -1);
			for (String entry : fastaArray) {
				// Go through each entry and collect information
				if (entry.startsWith(databaseType.getRowStarter())) {
					// Check if there are old entries and save them to map
					if (headerBuilder.length() > 0) {
						result.put(headerBuilder.toString(), sequenceBuilder.toString());
						headerBuilder.setLength(0);
						sequenceBuilder.setLength(0);
					}
					// It's a header
					headerBuilder.append(entry.trim());
				} else {
					// It's part of the sequence
					sequenceBuilder.append(entry.trim());
				}
			}
			// Save last entry
			result.put(headerBuilder.toString(), sequenceBuilder.toString());
		}
		return result;
	}
	
	private Map<String, String> getIdsAndSequences(Map<String, String> headersAndSequences, DatabaseType databaseType) {
		Map<String, String> result = new HashMap<String, String>();
		
		if (databaseType == null) {
			databaseType = DatabaseType.UniProt;
		}
		
		// Create a new ID search pattern dependent on database type
		Pattern p = Pattern.compile(databaseType.getRegExHeader(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		// Go through each fasta entry and parse header, then create new hashmap for ids and sequences
		for (Entry<String, String> entry : headersAndSequences.entrySet()) {
			Matcher m = p.matcher(entry.getKey());
		    if (m.find()) {
		        String id = m.group(1);
		        result.put(id, entry.getValue());
		    }
		}
		return result;
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
