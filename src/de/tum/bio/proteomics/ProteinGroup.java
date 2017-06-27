package de.tum.bio.proteomics;

import java.util.Map;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.ProteinGroupsTableHeaders;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a protein.
 *
 * @author Matthias Stahl
 */

public class ProteinGroup extends AminoAcidSequence implements AnalysisComponent {
	
	private int id;
	private StringProperty databaseIds = new SimpleStringProperty();
	private StringProperty names = new SimpleStringProperty();
	private StringProperty geneNames = new SimpleStringProperty();
	private DoubleProperty sequenceCoverage = new SimpleDoubleProperty();
	
	private DoubleProperty log2Enrichment = new SimpleDoubleProperty(Double.NaN);
	private DoubleProperty minusLog10PValue = new SimpleDoubleProperty(Double.NaN);
	
	public ProteinGroup(int id) {
		super();
		this.id = id;
	}
	
	public ProteinGroup(int id, String sequence) {
		super(sequence);
		this.id = id;
	}
	
	public ProteinGroup(Map<ProteinGroupsTableHeaders, String> properties) {
		super();
		this.id = Integer.parseInt(properties.get(ProteinGroupsTableHeaders.ID));
		setDatabaseIds(properties.get(ProteinGroupsTableHeaders.DATABASE_ID));
		setNames(properties.get(ProteinGroupsTableHeaders.NAMES));
		setGeneNames(properties.get(ProteinGroupsTableHeaders.GENE_NAMES));
		setSequenceCoverage(Double.parseDouble(properties.get(ProteinGroupsTableHeaders.SEQUENCE_COVERAGE)));
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	public void setDatabaseIds(String databaseIds) {
		this.databaseIds.set(databaseIds);
	}
	
	public String getDatabaseIds() {
		return databaseIds.get();
	}
	
	public StringProperty databaseIdsProperty() {
		return databaseIds;
	}
	
	public void setSequence(String sequence) {
		super.setSequenceString(sequence);
	}
	
	public void setNames(String names) {
		this.names.set(names);
	}

	public String getNames() {
		return names.get();
	}
	
	public StringProperty namesProperty() {
		return names;
	}
	
	public void setGeneNames(String geneNames) {
		this.geneNames.set(geneNames);
	}

	public String getGeneNames() {
		return geneNames.get();
	}
	
	public StringProperty geneNamesProperty() {
		return geneNames;
	}

	public void setSequenceCoverage(double sequenceCoverage) {
		this.sequenceCoverage.set(sequenceCoverage);
	}

	public double getSequenceCoverage() {
		return sequenceCoverage.get();
	}
	
	public DoubleProperty sequenceCoverageProperty() {
		return sequenceCoverage;
	}
	
	public void setLog2Enrichment(double log2Enrichment) {
		this.log2Enrichment.set(log2Enrichment);
	}

	public double getLog2Enrichment() {
		return log2Enrichment.get();
	}
	
	public DoubleProperty log2EnrichmentProperty() {
		return log2Enrichment;
	}
	
	public void setMinusLog10PValue(double minusLog10PValue) {
		this.minusLog10PValue.set(minusLog10PValue);
	}

	public double getMinusLog10PValue() {
		return minusLog10PValue.get();
	}
	
	public DoubleProperty minusLog10PValueProperty() {
		return minusLog10PValue;
	}
}