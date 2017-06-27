package de.tum.bio.proteomics;

import java.util.Map;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.io.FastaFileReader.DatabaseType;

/**
 * This class represents a parsed fasta file.
 * @author Matthias Stahl
 *
 */

public class FastaFile implements AnalysisComponent {
	
	private int id;
	private String name;
	private Map<String, String> idsAndSequences;
	private Map<String, String> headersAndSequences;
	private DatabaseType databaseType;
	
	public FastaFile(int id) {
		this(null, null, null, id, null);
	}
	
	public FastaFile(Map<String, String> idsAndSequences, DatabaseType databaseType, int id, String name) {
		this(idsAndSequences, null, databaseType, id, name);
	}
	
	public FastaFile(Map<String, String> idsAndSequences, Map<String, String> headersAndSequences, DatabaseType databaseType, int id, String name) {
		this.idsAndSequences = idsAndSequences;
		this.headersAndSequences = headersAndSequences;
		this.databaseType = databaseType;
		this.id = id;
		this.name = "Fasta: " + name;
	}

	@Override
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getSequenceById(String id) {
		return idsAndSequences.get(id);
	}
	
	public String getSequenceByHeader(String header) {
		return headersAndSequences.get(header);
	}
	
	public DatabaseType getDatabaseType() {
		return databaseType;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
