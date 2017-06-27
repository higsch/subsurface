package de.tum.bio.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.headers.TableHeaders;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class is able to read diverse separated text files and to transform the content to nested hashmaps.
 * @author Matthias Stahl
 *
 */

public final class SeparatedTextReader {
	
	DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
	StringProperty statusProperty = new SimpleStringProperty("");
	
	public SeparatedTextReader() {
		// empty
	}
	
	/**
	 * Reads the first line of a file.
	 * @param filePath
	 * @return Array of first line entries.
	 * @throws IOException
	 */
	public static String[] readFirstLine(String filePath) throws IOException {
		String[] firstLineArray = null;
		Path path = Paths.get(filePath);
		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(path);
			firstLineArray = reader.readLine().split("\t");
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
		return firstLineArray;
	}
	
	/**
	 * Alternative reader taking a header map as input.
	 * @param filePath
	 * @param headerMap
	 * @param separator
	 * @return nested ArrayList-HashMap
	 * @throws IOException
	 */
	public <T extends Enum<T> & TableHeaders> List<HashMap<String, String>> readFile(Path path, Map<T, String> headerMap, String separator) throws IOException {
		List<String> tmpList = new ArrayList<>(headerMap.values());
		return readFile(path, tmpList, separator);
	}
	
	/**
	 * Reads the specified columns of a file.
	 * @param filePath
	 * @param headers
	 * @return nested ArrayList-HashMap
	 * @throws IOException
	 */
	public List<HashMap<String, String>> readFile(Path path, List<String> headerList, String separator) throws IOException {
		List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
		// Create path and check if respective file exists
		BufferedReader reader = null;
		try {
			// Determine number of lines
			setStatusProperty("Open file " + path.getFileName() + "...");
			long numberOfLines = Files.lines(path).count();
			reader = Files.newBufferedReader(path);
			String line = null;
			boolean firstRow = true;
			Map<Integer, String> fileHeaderMap = new HashMap<>();
			int[] headerNumberArray = null;
			// Go through each line in the text file
			long lineCount = 1;
			setStatusProperty("Read file " + path.getFileName() + "...");
			while ((line = reader.readLine()) != null) {
				String[] lineArray = line.split(separator, -1);
				if (firstRow) {
					// It's the first row, so look for the headers
					fileHeaderMap = generateHeaderIdMap(lineArray);
					headerNumberArray = generateHeaderArray(fileHeaderMap, headerList);
					firstRow = false;
				} else {
					HashMap<String, String> currentEntryMap = new HashMap<>();
					// Now, go through each header and grab content from line array
					for (int index : headerNumberArray) {
						currentEntryMap.put(fileHeaderMap.get(index), lineArray[index]);
					}
					resultList.add(currentEntryMap);
				}
				setProgressProperty((double) lineCount/numberOfLines);
				lineCount++;
			}
		} catch (IOException e) {
			throw e;
		} finally {
			setProgressProperty(0.0);
			setStatusProperty("Done.");
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}
		return resultList;
	}
	
	/**
	 * Generates a map of the headers and their positions in the file.
	 * @param lineArray
	 * @param headers
	 * @return map
	 */
	private Map<Integer, String> generateHeaderIdMap(String[] lineArray) {
		Map<Integer, String> headerMap = new HashMap<>();
		for (int i = 0; i < lineArray.length; i++) {
			headerMap.put(i, lineArray[i]);
		}
		return headerMap;
	}
	
	/**
	 * Generates a header array from a header map for better search performance.
	 * @param headerMap
	 * @return array
	 */
	private int[] generateHeaderArray(Map<Integer, String> fileHeaderMap, List<String> headerList) {
		int[] headerArray = new int[headerList.size()];
		int headerCounter = 0;
		int arrayCounter = 0;
		for (Entry<Integer, String> entry : fileHeaderMap.entrySet()) {
			if (headerList.contains(entry.getValue())) {
				headerArray[arrayCounter] = headerCounter;
				arrayCounter++;
			}
			headerCounter++;
		}
		return headerArray;
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
