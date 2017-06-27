package de.tum.bio.proteomics.io.searchengine.maxquant;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tum.bio.proteomics.headers.EvidenceTableHeaders;
import de.tum.bio.proteomics.headers.ModificationsTableHeaders;
import de.tum.bio.proteomics.headers.PeptidesTableHeaders;
import de.tum.bio.proteomics.headers.ProteinGroupsTableHeaders;
import de.tum.bio.proteomics.headers.SummaryTableHeaders;

/**
 * Constants to read MaxQuant files
 *
 * @author Matthias Stahl
 */

public abstract class MQTablesIO {
	
	protected static final String ARRAY_SEPARATOR = ";";
	
	protected static final String FILENAME_EVIDENCE = "evidence.txt";
	protected static final String[] STANDARD_EVIDENCE_HEADERS = {
			"id",
			"Sequence",
			"Intensity",
			"Experiment",
			"Number of data points",
			"Peptide ID",
			"Mod. peptide ID"
		};
	
	protected static final String FILENAME_PEPTIDES = "peptides.txt";
	protected static final String[] STANDARD_PEPTIDES_HEADERS = {
			"id",
			"Sequence",
			"N-term cleavage window",
			"C-term cleavage window",
			"Start position",
			"End position",
			"Evidence IDs",
			"MS/MS IDs",
			"MS/MS Count",
			"Protein group IDs",
			"Evidence IDs",
			"Intensity"
		};
	protected static final String FILENAME_PROTEINGROUPS = "proteinGroups.txt";
	protected static final String[] STANDARD_PROTEINGROUPS_HEADERS = {
			"id"
		};
	
	protected static final String FILENAME_SUMMARY = "summary.txt";
	protected static final String[] STANDARD_SUMMARY_HEADERS = {
			"Raw file",
			"Experiment",
			"Variable modifications"
		};
	
	protected static final String FILEAPPENDIX_MODIFICATIONS = "Sites.txt";
	protected static final String[] STANDARD_MODIFICATIONS_HEADERS = {
			"id",
			"Localization prob",
			"Score diff",
			"PEP",
			"Score",
			"Delta score",
			"Score for localization",
			"Position in peptide",
			"Peptide IDs",
			"Probabilities"
		};
	
	@SuppressWarnings("serial")
	protected static final Map<EvidenceTableHeaders, String> EVIDENCE_HEADERS_UNI = new HashMap<EvidenceTableHeaders, String>() {{
			put(EvidenceTableHeaders.ID, "id");
			put(EvidenceTableHeaders.SEQUENCE, "Sequence");
			put(EvidenceTableHeaders.INTENSITY, "Intensity");
			put(EvidenceTableHeaders.EXPERIMENT, "Experiment");
			put(EvidenceTableHeaders.NUMBER_OF_DATA_POINTS, "Number of data points");
			put(EvidenceTableHeaders.PEPTIDE_ID, "Peptide ID");
			put(EvidenceTableHeaders.MOD_PEPTIDE_ID, "Mod. peptide ID");
		}};
	protected static BiMap<EvidenceTableHeaders, String> EvidenceHeadersMap;
	
	@SuppressWarnings("serial")
	protected static final Map<PeptidesTableHeaders, String> PEPTIDES_HEADERS_UNI = new HashMap<PeptidesTableHeaders, String>() {{
			put(PeptidesTableHeaders.ID, "id");
			put(PeptidesTableHeaders.SEQUENCE, "Sequence");
			put(PeptidesTableHeaders.N_TERM_CLEAVAGE_WINDOW, "N-term cleavage window");
			put(PeptidesTableHeaders.C_TERM_CLEAVAGE_WINDOW, "C-term cleavage window");
			put(PeptidesTableHeaders.START_POSITION, "Start position");
			put(PeptidesTableHeaders.END_POSITION, "End position");
			put(PeptidesTableHeaders.MSMSCOUNTS, "MS/MS Count");
			put(PeptidesTableHeaders.PROTEINGROUPS_IDS, "Protein group IDs");
			put(PeptidesTableHeaders.EVIDENCE_IDS, "Evidence IDs");
			put(PeptidesTableHeaders.INTENSITY, "Intensity");
		}};
	protected static BiMap<PeptidesTableHeaders, String> PeptidesHeadersMap;
	
	@SuppressWarnings("serial")
	protected static final Map<ProteinGroupsTableHeaders, String> PROTEINGROUPS_HEADERS_UNI = new HashMap<ProteinGroupsTableHeaders, String>() {{
			put(ProteinGroupsTableHeaders.ID, "id");
			put(ProteinGroupsTableHeaders.DATABASE_ID, "Protein IDs");
			put(ProteinGroupsTableHeaders.NAMES, "Protein names");
			put(ProteinGroupsTableHeaders.GENE_NAMES, "Gene names");
			put(ProteinGroupsTableHeaders.SEQUENCE_COVERAGE, "Sequence coverage [%]");
		}};
	protected static BiMap<ProteinGroupsTableHeaders, String> ProteinGroupsHeadersMap;
	
	@SuppressWarnings("serial")
	protected static final Map<SummaryTableHeaders, String> SUMMARY_HEADERS_UNI = new HashMap<SummaryTableHeaders, String>() {{
			put(SummaryTableHeaders.RAW_FILE, "Raw file");
			put(SummaryTableHeaders.EXPERIMENT, "Experiment");
			put(SummaryTableHeaders.VARIABLE_MODIFICATIONS, "Variable modifications");
		}};
	protected static BiMap<SummaryTableHeaders, String> SummaryHeadersMap;
	
	@SuppressWarnings("serial")
	protected static final Map<ModificationsTableHeaders, String> MODIFICATIONS_HEADERS_UNI = new HashMap<ModificationsTableHeaders, String>() {{
			put(ModificationsTableHeaders.ID, "id");
			put(ModificationsTableHeaders.LOCALIZATION_PROBABILITY, "Localization prob");
			put(ModificationsTableHeaders.SCORE_DIFF, "Score diff");
			put(ModificationsTableHeaders.PEP, "PEP");
			put(ModificationsTableHeaders.SCORE, "Score");
			put(ModificationsTableHeaders.DELTA_SCORE, "Delta score");
			put(ModificationsTableHeaders.SCORE_FOR_LOCALIZATION, "Score for localization");
			put(ModificationsTableHeaders.POSITION_IN_PEPTIDE, "Position in peptide");
			put(ModificationsTableHeaders.PEPTIDE_IDS, "Peptide IDs");
			put(ModificationsTableHeaders.SEQUENCE_WINDOW, "Probabilities"); // TODO: is this correct?
		}};
	protected static BiMap<ModificationsTableHeaders, String> ModificationsHeadersMap;
	
	
	protected void initBiMaps() {
		PeptidesHeadersMap = HashBiMap.create(PEPTIDES_HEADERS_UNI);
		ProteinGroupsHeadersMap = HashBiMap.create(PROTEINGROUPS_HEADERS_UNI);
		EvidenceHeadersMap = HashBiMap.create(EVIDENCE_HEADERS_UNI);
		SummaryHeadersMap = HashBiMap.create(SUMMARY_HEADERS_UNI);
		ModificationsHeadersMap = HashBiMap.create(MODIFICATIONS_HEADERS_UNI);
	}
}