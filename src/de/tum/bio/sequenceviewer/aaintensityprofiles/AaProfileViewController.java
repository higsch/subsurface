package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tum.bio.proteomics.Toolbox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AaProfileViewController {

	private Stage stage;
	private AaProfiler aaProfiler;
	
	final double SCALE_DELTA = 1.1;
	
	@FXML
	Label labelProteinId;
	@FXML
	ChoiceBox<String> choiceAminoAcid;
	@FXML
	Spinner<Integer> spinnerOffset;
	@FXML
	TextField numberOfClusters;
	
	@FXML
	TabPane tabPane;
	
	@FXML
	LineChart<String, Double> chart;
	@FXML
	CategoryAxis xAxis;
	@FXML
	NumberAxis yAxis;
	@FXML
	LineChart<String, Double> chartNormalized;
	@FXML
	CategoryAxis xAxisNormalized;
	@FXML
	NumberAxis yAxisNormalized;
	
	
	@FXML
	VBox vBoxContent;
	@FXML
	VBox vBoxCorrelationMatrix;
	@FXML
	LineChart<Integer, Double> chartRankedCorrelations;
	@FXML
	NumberAxis xAxisRankedCorrelation;
	@FXML
	NumberAxis yAxisRankedCorrelation;
	
	public void init(Stage stage) {
		this.stage = stage;
		
		xAxis.setLabel("Experiment");
		yAxis.setLabel("log2(intensity)");
		xAxisNormalized.setLabel("Experiment");
		yAxisNormalized.setLabel("Normalized log2(intensity)");
		xAxisRankedCorrelation.setLabel("Rank");
		yAxisRankedCorrelation.setLabel("Correlation");
		
		LinkedList<String> residues = new LinkedList<>(Arrays.asList(Toolbox.aminoAcidsSingleLetter()));
		residues.addFirst("All");
		
		choiceAminoAcid.setItems(FXCollections.observableArrayList(residues));
		choiceAminoAcid.getSelectionModel().select(0);
		
		spinnerOffset.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 1));
		spinnerOffset.getValueFactory().setValue(0);
		
		choiceAminoAcid.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
			if (n == "All") {
				update(null, 0);
			} else {
				update(getAaListFromString(n), spinnerOffset.getValue());
			}
		});
		
		spinnerOffset.valueProperty().addListener((obs, o, n) -> {
			if (choiceAminoAcid.getSelectionModel().getSelectedItem() == "All") {
				update(null, 0);
			} else {
				update(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), n);
			}
		});
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void setAaProfiler(AaProfiler aaProfiler) {
		this.aaProfiler = aaProfiler;
		
		choiceAminoAcid.getSelectionModel().select("K");
		update(getAaListFromString("K"), spinnerOffset.getValue());
		
		labelProteinId.setText(aaProfiler.getProteinIds());
	}
	
	private void showCorrelationMatrix() {
		
	}
	
	public void cluster() {
		
	}
	
	private void update(List<Character> residues, int offset) {
		yAxis.setForceZeroInRange(false);
		yAxisNormalized.setForceZeroInRange(false);
		if (residues == null) {
			chart.setData(aaProfiler.getAllXYChartSeries());
			chartNormalized.setData(aaProfiler.getAllNormalizedXYChartSeries());
			chartRankedCorrelations.getData().clear();
		} else {
			chart.setData(aaProfiler.getAllXYChartSeriesByResidue(residues, offset));
			chartNormalized.setData(aaProfiler.getAllNormalizedXYChartSeriesByResidue(residues, offset));
			showCorrelationMatrix();
			showRankedCorrelations();
		}
	}
	
	private void showRankedCorrelations() {
		chartRankedCorrelations.setData(aaProfiler.getAllRankedCorrelationSeriesByResidue(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), spinnerOffset.getValue()));
	}
	
	private List<Character> getAaListFromString(String string) {
		List<Character> list = new ArrayList<>();
		
		list.add(string.charAt(0));
		
		return list;
	}
	
}