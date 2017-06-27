package de.tum.bio.proteomics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.ModificationsTableHeaders;

/**
 * This class represents an amino acid modification.
 * @author Matthias Stahl
 *
 */

public class Modification implements AnalysisComponent {
	private int id;
	private String name;
	private double localizationProbability;
	private double scoreDiff;
	private double pep;
	private double deltaScore;
	private double scoreForLocalization;
	private int positionInPeptide;
	private List<String> peptideIds;
	
	private String sequenceWindow;
	
	private String abbreviation;
	
	
	public Modification(String name, Map<ModificationsTableHeaders, String> properties) {
		this.id = Integer.parseInt(properties.get(ModificationsTableHeaders.ID));
		this.name = name;
		this.localizationProbability = Double.parseDouble(properties.get(ModificationsTableHeaders.LOCALIZATION_PROBABILITY));
		this.scoreDiff = Double.parseDouble(properties.get(ModificationsTableHeaders.SCORE_DIFF));
		this.pep = Double.parseDouble(properties.get(ModificationsTableHeaders.PEP));
		this.deltaScore = Double.parseDouble(properties.get(ModificationsTableHeaders.DELTA_SCORE));
		this.scoreForLocalization = Double.parseDouble(properties.get(ModificationsTableHeaders.SCORE_FOR_LOCALIZATION));
		this.positionInPeptide = Integer.parseInt(properties.get(ModificationsTableHeaders.POSITION_IN_PEPTIDE));
		this.peptideIds = Arrays.asList(properties.get(ModificationsTableHeaders.PEPTIDE_IDS).split(";"));
		
		String tmp = properties.get(ModificationsTableHeaders.SEQUENCE_WINDOW);
		this.sequenceWindow = tmp.replaceAll("[^a-zA-Z]", "").toUpperCase();
		
		if (this.name.length() > 3) {
			this.abbreviation = this.name.substring(0, 3);
		} else {
			this.abbreviation = this.name;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public double getLocalizationProbability() {
		return localizationProbability;
	}
	
	public double getScoreDiff() {
		return scoreDiff;
	}
	
	public double getPep() {
		return pep;
	}
	
	public double getDeltaScore() {
		return deltaScore;
	}
	
	public double getScoreForLocalization() {
		return scoreForLocalization;
	}
	
	public void setPositionInPeptide(int position) {
		this.positionInPeptide = position;
	}
	
	public int getPositionInPeptide() {
		return positionInPeptide;
	}
	
	public List<String> getPeptideIds() {
		return peptideIds;
	}
	
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	public String getAbbreviation() {
		return abbreviation;
	}
	
	public String getSequenceWindow() {
		return sequenceWindow;
	}

	@Override
	public int getId() {
		return id;
	}
}