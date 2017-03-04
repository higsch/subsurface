package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class AaProfileViewController {

	private AaProfileContainer aaProfileContainer;
	
	@FXML
	LineChart<String, Double> chart;
	@FXML
	NumberAxis yAxis;
	
	public void setAaProfileContainer(AaProfileContainer aaProfileContainer) {
		this.aaProfileContainer = aaProfileContainer;
		
		updateChart();
	}
	
	private void updateChart() {
		List<Character> residues = new ArrayList<>();
		residues.add("K".charAt(0));
		yAxis.setForceZeroInRange(false);
		chart.setData(aaProfileContainer.getAllXYChartSeriesByResidue(residues));
	}
	
}