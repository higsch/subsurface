	package de.tum.bio.proteomics.io.statistics.perseus;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tum.bio.proteomics.headers.StatisticsTableHeaders;

public abstract class PerseusTablesIO {
	
	@SuppressWarnings("serial")
	private static final Map<StatisticsTableHeaders, String> STATISTICS_HEADERS = new HashMap<StatisticsTableHeaders, String>() {{
			put(StatisticsTableHeaders.NAME, "Protein IDs");
			put(StatisticsTableHeaders.LOG2_ENRICHMENT, "log2 Student's t-test difference");
			put(StatisticsTableHeaders.MINUS_LOG10_PVALUE, "-log Student's t-test p-value");
		}};
	protected static BiMap<StatisticsTableHeaders, String> StatisticsHeadersMap;
	
	protected void initBiMaps() {
		StatisticsHeadersMap = HashBiMap.create(STATISTICS_HEADERS);
	}
}