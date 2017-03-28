package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public abstract class Profiler {
	
	protected Map<String, List<Double>> getDoubleProfileMap(Map<String, Map<String, Long>> profileMap, List<String> selectedExperiments) {
		Map<String, List<Double>> map = new HashMap<>();
		
		// Create map with double profile values
		for (Entry<String, Map<String, Long>> entry : profileMap.entrySet()) {
			for (Entry<String, Long> dataPoint : entry.getValue().entrySet()) {
				if (selectedExperiments.contains(dataPoint.getKey())) {
					if (!map.containsKey(entry.getKey())) {
						map.put(entry.getKey(), new ArrayList<Double>());
					}
					double intensity;
					if (dataPoint.getValue() == null) {
						intensity = 0d; //Double.NaN;
					} else {
						intensity = (double) dataPoint.getValue();
					}
					map.get(entry.getKey()).add(intensity);
				}
			}
		}
		
		return map;
	}
	
	protected String getListKey(int position, Set<String> keyList) {
		for (String key : keyList) {
			if ((key.contains(String.valueOf(position)) || key.contains(" " + String.valueOf(position) + ", ")) || (key.contains("[" + String.valueOf(position) + ", ")) || (key.contains(" " + String.valueOf(position) + "]"))) {
				return key;
			}
		}
		return null;
	}
	
}
