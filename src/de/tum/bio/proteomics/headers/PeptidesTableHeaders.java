package de.tum.bio.proteomics.headers;

public enum PeptidesTableHeaders implements TableHeaders {
		ID,
		SEQUENCE,
		N_TERM_CLEAVAGE_WINDOW,
		C_TERM_CLEAVAGE_WINDOW,
		START_POSITION,
		END_POSITION,
		MSMSCOUNTS,
		MSMSIDS,
		SCORE,
		PROTEINGROUPS_IDS,
		EVIDENCE_IDS,
		INTENSITY;
}